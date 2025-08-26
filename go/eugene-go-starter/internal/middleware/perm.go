// internal/middleware/perm.go
package middleware

import (
	"errors"
	"fmt"
	"math"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"time"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"

	"github.com/gin-gonic/gin"
)

type PermOptions struct {
	Repo      repo.MenuRepo
	TTL       time.Duration       // 可选：缓存 TTL，0 表示每次都查库
	Whitelist map[string]struct{} // 可选：不经授权直接放行的路径（支持前缀）
}

// —— 简单的 per-user 内存缓存（可选）
type cachedACL struct {
	exact   map[string]struct{}
	prefix  []string
	expires time.Time
}

// 缓存 key 统一使用 uint64（与 Auth 注入的 uid 类型一致）
var aclCache sync.Map // key:uint64 -> cachedACL

// —— 新增：白名单匹配（支持精确 & 前缀：末尾 '/' 或 '*' 视为前缀）
func isWhitelisted(path string, wl map[string]struct{}) bool {
	if wl == nil {
		return false
	}
	// 精确命中
	if _, ok := wl[path]; ok {
		return true
	}
	// 前缀匹配
	for k := range wl {
		if strings.HasSuffix(k, "*") {
			pre := strings.TrimSuffix(k, "*")
			if strings.HasPrefix(path, pre) {
				return true
			}
		}
		if strings.HasSuffix(k, "/") {
			if strings.HasPrefix(path, k) {
				return true
			}
		}
	}
	return false
}

// 挂在 AuthRequired 后面
// 挂在 AuthRequired 后面（或全局都可）
func ACLGuard(opt PermOptions) gin.HandlerFunc {
	if opt.Whitelist == nil {
		opt.Whitelist = map[string]struct{}{
			"/eugene/login.html": {},
			"/eugene":            {},
			"/favicon.ico":       {},

			// 静态资源前缀（r.Static("/eugene", "./web")）
			"/eugene/assets/": {},
			"/eugene/css/":    {},
			"/eugene/js/":     {},

			// 如果你还有根目录静态
			"/assets/": {},
			"/css/":    {},
			"/js/":     {},

			// API 只走 Auth，不走 ACL
			"/api/": {},
		}
	}

	return func(c *gin.Context) {
		path := c.Request.URL.Path

		// 0) 先判断白名单（支持前缀）
		if isWhite(path, opt.Whitelist) {
			c.Next()
			return
		}

		// 1) 需要已通过 Auth 写入 uid（例如 /api/*）
		uidAny, ok := c.Get("uid")
		if !ok {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
			return
		}
		uid, err := toUint64(uidAny)
		if err != nil || uid == 0 {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid uid"})
			return
		}

		// 2) 取 ACL（缓存或查库）
		exact, prefix, err := getACL(c, opt, uid)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": "load acl failed"})
			return
		}

		// 3) 放到上下文（可选）
		c.Set("acl.exact", exact)
		c.Set("acl.prefix", prefix)

		// 4) 判断是否允许
		if allowed(path, exact, prefix) {
			c.Next()
			return
		}
		c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "forbidden"})
	}
}

// 支持 “/xxx/” 或 “/xxx/*” 作为前缀白名单；精确命中也支持
func isWhite(path string, wl map[string]struct{}) bool {
	if _, ok := wl[path]; ok {
		return true
	}
	for k := range wl {
		if strings.HasSuffix(k, "/*") {
			if strings.HasPrefix(path, strings.TrimSuffix(k, "*")) {
				return true
			}
		} else if strings.HasSuffix(k, "/") {
			if strings.HasPrefix(path, k) {
				return true
			}
		}
	}
	return false
}

func getACL(c *gin.Context, opt PermOptions, uid uint64) (map[string]struct{}, []string, error) {
	if opt.Repo == nil {
		return nil, nil, errors.New("nil repo")
	}

	// 命中缓存
	if opt.TTL > 0 {
		if v, ok := aclCache.Load(uid); ok {
			it := v.(cachedACL)
			if time.Now().Before(it.expires) {
				return it.exact, it.prefix, nil
			}
		}
	}

	// repo 接口是 int64；安全转换
	if uid > math.MaxInt64 {
		return nil, nil, errors.New("uid overflow")
	}
	// 注意：保持你原来调用的类型，不改业务签名
	menus, err := opt.Repo.ListByUser(c.Request.Context(), uint64(uid))
	if err != nil {
		return nil, nil, err
	}

	exact, prefix := buildACL(menus)

	// 写缓存
	if opt.TTL > 0 {
		aclCache.Store(uid, cachedACL{
			exact:   exact,
			prefix:  prefix,
			expires: time.Now().Add(opt.TTL),
		})
	}
	return exact, prefix, nil
}

func buildACL(menus []model.Menu) (map[string]struct{}, []string) {
	exact := make(map[string]struct{})
	var prefix []string
	for _, m := range menus {
		if m.Status != 1 || m.Path == nil || *m.Path == "" {
			continue
		}
		p := *m.Path
		// 约定：以 "/*" 结尾表示前缀授权
		if strings.HasSuffix(p, "/*") {
			prefix = append(prefix, strings.TrimSuffix(p, "*"))
		} else {
			exact[p] = struct{}{}
		}
	}
	return exact, prefix
}

func allowed(p string, exact map[string]struct{}, prefix []string) bool {
	if _, ok := exact[p]; ok {
		return true
	}
	for _, pre := range prefix {
		if strings.HasPrefix(p, pre) {
			return true
		}
	}
	return false
}

// 上下文 uid 转为 uint64（支持 uint64/int64/int/string）
func toUint64(v any) (uint64, error) {
	switch x := v.(type) {
	case uint64:
		return x, nil
	case int64:
		if x < 0 {
			return 0, fmt.Errorf("bad uid value: %d", x)
		}
		return uint64(x), nil
	case int:
		if x < 0 {
			return 0, fmt.Errorf("bad uid value: %d", x)
		}
		return uint64(x), nil
	case string:
		return strconv.ParseUint(strings.TrimSpace(x), 10, 64)
	default:
		return 0, fmt.Errorf("bad uid type: %T", v)
	}
}

// 在保存权限后调用以清缓存（在你的 handler 里调用）
func InvalidateACL(userID uint64) { aclCache.Delete(userID) }
