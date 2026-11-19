package admin

import (
	"archive/zip"
	"bytes"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"time"
	"unicode"
	"unicode/utf8"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/apkdownload"
	"ths-service/internal/response"
)

// File size must be strictly below 100 decimal MB.
const maxAPKBytes int64 = 100_000_000 - 1

var errAPKSameSecond = errors.New("APK already published this second")

type APKHandler struct {
	auth    *Handler
	dir     string
	uploads chan struct{}
}

func RegisterAPK(r *gin.Engine, h *Handler) {
	dir := os.Getenv("THS_APK_STORAGE_DIR")
	if dir == "" {
		dir = "data/apks"
	}
	a := &APKHandler{h, dir, make(chan struct{}, 1)}
	api := r.Group("/api/admin/app-package", h.RequireLogin, sameOrigin, noAPKCache)
	api.GET("", a.AdminAPKInfo)
	api.POST("/upload", selectAPK, a.Upload)
	api.PUT("/passcode", selectAPK, a.Passcode)
	public := r.Group("/api/app-download", noAPKCache)
	public.GET("/info", a.Info)
	public.POST("/download", sameOrigin, selectAPK, a.Download)
	for route, name := range map[string]string{"/app-download": "app-download.html", "/app-download.js": "app-download.js"} {
		resource := name
		r.GET(route, func(c *gin.Context) {
			b, e := pages.ReadFile("web/" + resource)
			if e != nil {
				c.Status(404)
				return
			}
			c.Header("Cache-Control", "no-store")
			c.Header("X-Content-Type-Options", "nosniff")
			c.Header("Referrer-Policy", "same-origin")
			c.Header("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; frame-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'")
			contentType := "text/html; charset=utf-8"
			if strings.HasSuffix(resource, ".js") {
				contentType = "text/javascript; charset=utf-8"
			}
			c.Data(200, contentType, b)
		})
	}
}
func noAPKCache(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	c.Header("X-Content-Type-Options", "nosniff")
	c.Header("X-Robots-Tag", "noindex, nofollow")
	c.Next()
}
func (a *APKHandler) state(c *gin.Context) (apkdownload.State, error) {
	var s apkdownload.State
	e := a.auth.db.WithContext(c.Request.Context()).First(&s, c.GetUint64("apkID")).Error
	return s, e
}

// selectAPK rejects missing/unknown slots, so a request cannot replace another app accidentally.
func selectAPK(c *gin.Context) {
	values := c.Request.URL.Query()["app"]
	if len(values) == 1 {
		for id, key := range apkdownload.Apps {
			if values[0] == key {
				c.Set("apkID", id)
				return
			}
		}
	}
	response.Error(c, 400, response.CodeInvalidRequest, "请选择安装包：nanwang、alipay、tax或ths")
	c.Abort()
}

// Info 获取当前安装包公开信息
// @Summary 获取当前APK版本和下载信息
// @Description 无需登录。不包含口令、口令哈希或文件路径。available为true时可提交口令下载；不返回文件直链。
// @Tags App端 · 安装包下载
// @Produce json
// @Success 200 {object} apkdownload.ListResponse
// @Failure 500 {object} response.ErrorEnvelope
// @Param app query string false "可选，指定单个安装包：nanwang/alipay/tax/ths；不传返回四项列表"
// @Router /api/app-download/info [get]
func (a *APKHandler) Info(c *gin.Context) {
	if c.Query("app") != "" {
		selectAPK(c)
		if c.IsAborted() {
			return
		}
		state, err := a.state(c)
		if err != nil {
			a.auth.fail(c, err)
			return
		}
		response.Success(c, apkdownload.View(state))
		return
	}
	var states []apkdownload.State
	if err := a.auth.db.WithContext(c.Request.Context()).Where("id IN ?", []uint64{1, 2, 3, 4}).Order("id").Find(&states).Error; err != nil {
		a.auth.fail(c, err)
		return
	}
	result := make([]apkdownload.Info, 0, 4)
	for _, state := range states {
		result = append(result, apkdownload.View(state))
	}
	response.Success(c, result)
}

// AdminAPKInfo 获取后台安装包信息
// @Summary 获取安装包管理信息
// @Description 管理员Cookie鉴权。只返回版本、大小、SHA256和口令是否已设置，不回显口令。
// @Tags 后台 · App安装包
// @Success 200 {object} apkdownload.ListResponse
// @Param app query string false "指定单个安装包；不传返回四项列表"
// @Router /api/admin/app-package [get]
func (a *APKHandler) AdminAPKInfo(c *gin.Context) { a.Info(c) }

func passcodeHash(s string) (string, error) {
	if !utf8.ValidString(s) || utf8.RuneCountInString(s) < 5 || utf8.RuneCountInString(s) > 64 || len(s) > 256 || strings.TrimSpace(s) == "" {
		return "", fmt.Errorf("口令须为5至64个字符，最多256字节，支持中文，不能全为空格")
	}
	for _, r := range s {
		if unicode.IsControl(r) {
			return "", fmt.Errorf("口令不能含换行等控制字符")
		}
	}
	// Hash UTF-8 bytes before bcrypt so Chinese passcodes cannot hit bcrypt's 72-byte limit.
	sum := sha256.Sum256([]byte(s))
	b, e := bcrypt.GenerateFromPassword([]byte(hex.EncodeToString(sum[:])), bcrypt.DefaultCost)
	return string(b), e
}
func matchesPasscode(hash, s string) bool {
	sum := sha256.Sum256([]byte(s))
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(hex.EncodeToString(sum[:]))) == nil
}
func readAPKJSON(c *gin.Context, v any) bool {
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 4096)
	d := json.NewDecoder(c.Request.Body)
	d.DisallowUnknownFields()
	return strings.HasPrefix(c.ContentType(), "application/json") && d.Decode(v) == nil && d.Decode(new(any)) == io.EOF
}

// Passcode 修改下载口令
// @Summary 设置或修改APK下载口令
// @Description 管理员Cookie鉴权。5至64个Unicode字符，最多256个UTF8字节，支持中文和符号，不自动去掉空格。只保存SHA256预哈希后的bcrypt哈希，不回显；新下载请求立即使用新口令。保留审计但不记录口令。
// @Tags 后台 · App安装包
// @Accept json
// @Param body body apkdownload.PasscodeRequest true "新的下载口令"
// @Success 200 {object} apkdownload.InfoResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Param app query string true "安装包：nanwang/alipay/tax/ths"
// @Router /api/admin/app-package/passcode [put]
func (a *APKHandler) Passcode(c *gin.Context) {
	var req apkdownload.PasscodeRequest
	if !readAPKJSON(c, &req) {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效口令JSON")
		return
	}
	hash, e := passcodeHash(req.Passcode)
	if e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		return
	}
	var state apkdownload.State
	e = a.auth.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&state, c.GetUint64("apkID")).Error; e != nil {
			return e
		}
		if e := tx.Model(&state).Update("passcode_hash", hash).Error; e != nil {
			return e
		}
		admin, _ := c.Get("administrator")
		return tx.Create(&apkdownload.Audit{AppID: state.ID, AdminID: admin.(Administrator).ID, Action: "passcode_change", VersionName: state.VersionName, SHA256: state.SHA256}).Error
	})
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	response.Success(c, apkdownload.View(state))
}

func validateAPK(filename string) error {
	z, e := zip.OpenReader(filename)
	if e != nil {
		return fmt.Errorf("文件不是有效的APK压缩结构")
	}
	defer z.Close()
	if len(z.File) > 100000 {
		return fmt.Errorf("APK条目过多")
	}
	var manifest *zip.File
	var total uint64
	for _, f := range z.File {
		name := f.Name
		if strings.Contains(name, "\\") || strings.HasPrefix(name, "/") || strings.Contains("/"+name, "/../") || path.Clean(name) == ".." || f.Flags&1 != 0 {
			return fmt.Errorf("APK包含不支持的路径或加密条目")
		}
		if f.UncompressedSize64 > 2<<30 || total > 2<<30-f.UncompressedSize64 {
			return fmt.Errorf("APK解压大小超限")
		}
		total += f.UncompressedSize64
		if name == "AndroidManifest.xml" {
			if manifest != nil {
				return fmt.Errorf("APK清单重复")
			}
			manifest = f
		}
	}
	if manifest == nil || manifest.UncompressedSize64 < 8 || manifest.UncompressedSize64 > 4<<20 {
		return fmt.Errorf("APK缺少有效AndroidManifest.xml")
	}
	reader, e := manifest.Open()
	if e != nil {
		return fmt.Errorf("APK清单不可读取")
	}
	defer reader.Close()
	b, e := io.ReadAll(io.LimitReader(reader, 4<<20+1))
	if e != nil || len(b) > 4<<20 || !bytes.Equal(b[:min(4, len(b))], []byte{3, 0, 8, 0}) {
		return fmt.Errorf("APK清单不是有效的Android二进制XML")
	}
	return nil
}

// Upload 上传并发布当前APK
// @Summary 上传并替换当前APK
// @Description 管理员Cookie鉴权，multipart/form-data，小于100MB。releaseKey由服务端按北京时间生成YYYYMMDDHHmmss，不接受客户端修改。默认下载口令eugenecoming，可通过专用接口修改。检查ZIP结构及Android二进制清单，不执行文件，不替代Android签名验证。上传失败保留当前版本，旧文件保留于持久化目录。
// @Tags 后台 · App安装包
// @Accept multipart/form-data
// @Param file formData file true "APK文件，小于100MB"
// @Success 200 {object} apkdownload.InfoResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 413 {object} response.ErrorEnvelope
// @Param app query string true "安装包：nanwang/alipay/tax/ths"
// @Router /api/admin/app-package/upload [post]
func (a *APKHandler) Upload(c *gin.Context) {
	select {
	case a.uploads <- struct{}{}:
		defer func() { <-a.uploads }()
	default:
		response.Error(c, 429, response.CodeTooManyRequests, "另一个安装包正在上传，请稍后再试")
		return
	}
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, maxAPKBytes+(1<<20))
	if e := c.Request.ParseMultipartForm(1 << 20); e != nil {
		response.Error(c, 413, response.CodeInvalidRequest, "上传失败：文件过大或表单无效（小于100MB）")
		return
	}
	defer c.Request.MultipartForm.RemoveAll()
	if _, exists := c.Request.MultipartForm.Value["versionName"]; exists {
		response.Error(c, 400, response.CodeInvalidRequest, "发布编号由服务器生成，不能手工指定")
		return
	}
	if _, exists := c.Request.MultipartForm.Value["releaseKey"]; exists {
		response.Error(c, 400, response.CodeInvalidRequest, "发布编号不可修改")
		return
	}
	if _, exists := c.Request.MultipartForm.Value["passcode"]; exists {
		response.Error(c, 400, response.CodeInvalidRequest, "请在修改口令功能中设置口令")
		return
	}
	header, e := c.FormFile("file")
	if e != nil || !strings.EqualFold(filepath.Ext(header.Filename), ".apk") || header.Size > maxAPKBytes {
		response.Error(c, 400, response.CodeInvalidRequest, "请选择100MB以下的APK文件")
		return
	}
	input, e := header.Open()
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	defer input.Close()
	if e := os.MkdirAll(a.dir, 0700); e != nil {
		a.auth.fail(c, e)
		return
	}
	file, e := os.CreateTemp(a.dir, "upload-*")
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	temporary := file.Name()
	defer os.Remove(temporary)
	digest := sha256.New()
	size, e := io.Copy(io.MultiWriter(file, digest), io.LimitReader(input, maxAPKBytes+1))
	if e == nil {
		e = file.Sync()
	}
	closeErr := file.Close()
	if e != nil || closeErr != nil {
		a.auth.fail(c, errors.Join(e, closeErr))
		return
	}
	if size == 0 || size > maxAPKBytes {
		response.Error(c, 413, response.CodeInvalidRequest, "APK大小超出限制")
		return
	}
	if e := validateAPK(temporary); e != nil {
		response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		return
	}
	name := rand.Text() + ".apk"
	final := filepath.Join(a.dir, name)
	if e := os.Rename(temporary, final); e != nil {
		a.auth.fail(c, e)
		return
	}
	committed := false
	defer func() {
		if !committed {
			os.Remove(final)
		}
	}()
	var state apkdownload.State
	e = a.auth.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&state, c.GetUint64("apkID")).Error; e != nil {
			return e
		}
		if state.PasscodeHash == "" {
			return fmt.Errorf("请先设置下载口令")
		}
		now := time.Now()
		version := now.In(time.FixedZone("Asia/Shanghai", 8*60*60)).Format("20060102150405")
		if version == state.VersionName {
			return errAPKSameSecond
		}
		state.StoredName = name
		state.VersionName = version
		state.ReleaseNotes = ""
		state.SizeBytes = size
		state.SHA256 = hex.EncodeToString(digest.Sum(nil))
		state.UploadedAt = &now
		if e := tx.Save(&state).Error; e != nil {
			return e
		}
		admin, _ := c.Get("administrator")
		return tx.Create(&apkdownload.Audit{AppID: state.ID, AdminID: admin.(Administrator).ID, Action: "upload", VersionName: version, SHA256: state.SHA256}).Error
	})
	if e != nil {
		if errors.Is(e, errAPKSameSecond) {
			response.Error(c, 409, response.CodeInvalidRequest, "同一秒内已发布安装包，请稍后重试")
		} else if e.Error() == "请先设置下载口令" {
			response.Error(c, 400, response.CodeInvalidRequest, e.Error())
		} else {
			a.auth.fail(c, e)
		}
		return
	}
	committed = true
	response.Success(c, apkdownload.View(state))
}

// Download 验证口令并下载APK
// @Summary 输入口令直接下载当前APK
// @Description 无需会员登录。口令仅通过POST请求体提交，可用JSON或表单passcode字段。成功直接返回APK附件，无文件直链。按IP滚动一分钟错5次冻结10分钟，返回429和Retry-After；成功清空失败次数。限流落库，重启仍生效；修改口令后新请求须使用新口令。
// @Tags App端 · 安装包下载
// @Accept json,application/x-www-form-urlencoded
// @Produce application/vnd.android.package-archive
// @Param body body apkdownload.PasscodeRequest true "下载口令"
// @Success 200 {file} file "APK附件"
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 429 {object} response.ErrorEnvelope
// @Param app query string true "安装包：nanwang/alipay/tax/ths"
// @Router /api/app-download/download [post]
func (a *APKHandler) Download(c *gin.Context) {
	var req apkdownload.PasscodeRequest
	parsed := false
	if c.ContentType() == "application/x-www-form-urlencoded" {
		c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 4096)
		parsed = c.Request.ParseForm() == nil
		values := c.Request.PostForm["passcode"]
		if len(values) == 1 {
			req.Passcode = values[0]
		} else {
			parsed = false
		}
	} else {
		parsed = readAPKJSON(c, &req)
	}
	state, e := a.state(c)
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	valid := false
	retry, e := a.auth.guard.gate(c.Request.Context(), "apk-download-ip", normalizedIP(c.ClientIP()), func() bool {
		hash := state.PasscodeHash
		if hash == "" {
			hash = string(dummyHash)
		}
		valid = parsed && len(req.Passcode) <= 256 && utf8.ValidString(req.Passcode) && matchesPasscode(hash, req.Passcode) && state.PasscodeHash != ""
		return valid
	})
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	if retry > 0 {
		c.Header("Retry-After", strconv.Itoa(retry))
		response.Error(c, 429, response.CodeTooManyRequests, "口令错误次数过多，请10分钟后重试")
		return
	}
	if !valid {
		response.Error(c, 401, response.CodeUnauthorized, "下载口令错误或尚未开放下载")
		return
	}
	latest, e := a.state(c)
	if e != nil {
		a.auth.fail(c, e)
		return
	}
	if latest.PasscodeHash != state.PasscodeHash {
		response.Error(c, 401, response.CodeUnauthorized, "口令已更新，请重新输入")
		return
	}
	state = latest
	if state.StoredName == "" {
		response.Error(c, 404, response.CodeNotFound, "暂未上传安装包")
		return
	}
	if filepath.Base(state.StoredName) != state.StoredName || !strings.HasSuffix(state.StoredName, ".apk") {
		a.auth.fail(c, fmt.Errorf("invalid APK storage name"))
		return
	}
	file, e := os.Open(filepath.Join(a.dir, state.StoredName))
	if e != nil {
		if os.IsNotExist(e) {
			response.Error(c, 404, response.CodeNotFound, "安装包文件暂不可用")
		} else {
			a.auth.fail(c, e)
		}
		return
	}
	defer file.Close()
	stat, e := file.Stat()
	if e != nil || !stat.Mode().IsRegular() || stat.Size() != state.SizeBytes {
		a.auth.fail(c, fmt.Errorf("APK file metadata mismatch"))
		return
	}
	filename := apkdownload.Filename(state)
	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, filename))
	c.DataFromReader(200, stat.Size(), "application/vnd.android.package-archive", file, nil)
}
