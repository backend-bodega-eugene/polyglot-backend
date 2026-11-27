package admin

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"log/slog"
	"net"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	driver "github.com/go-sql-driver/mysql"
	"gorm.io/driver/mysql"
	"ths-service/internal/response"
)

type backupHandler struct {
	auth   *Handler
	active chan struct{}
	run    func(context.Context, string, string, io.Writer) error
}

func registerBackup(r *gin.Engine, h *Handler) {
	b := &backupHandler{auth: h, active: make(chan struct{}, 1), run: runDatabaseDump}
	r.POST("/api/admin/data-backup/download", h.RequireLogin, sameOrigin, b.download)
}

// mysqlOption quotes values for a client option file, never command-line passwords.
func mysqlOption(value string) string {
	r := strings.NewReplacer("\\", "\\\\", "\"", "\\\"", "\n", "\\n", "\r", "\\r", "\t", "\\t")
	return "\"" + r.Replace(value) + "\""
}

func (b *backupHandler) clientOptions(ctx context.Context) (string, string, error) {
	dialector, ok := b.auth.db.Dialector.(*mysql.Dialector)
	if !ok {
		return "", "", fmt.Errorf("backup requires MySQL")
	}
	cfg, err := driver.ParseDSN(dialector.DSN)
	if err != nil {
		return "", "", fmt.Errorf("cannot read database connection")
	}
	// Read the active schema, so it cannot accidentally export another database.
	var name string
	if err := b.auth.db.WithContext(ctx).Raw("SELECT DATABASE()").Scan(&name).Error; err != nil || name == "" {
		return "", "", fmt.Errorf("cannot determine active database")
	}
	if strings.HasPrefix(name, "-") {
		return "", "", fmt.Errorf("unsupported database name")
	}
	options := "[client]\nuser=" + mysqlOption(cfg.User) + "\npassword=" + mysqlOption(cfg.Passwd) + "\ndefault-character-set=utf8mb4\n"
	switch cfg.Net {
	case "tcp":
		host, port, err := net.SplitHostPort(cfg.Addr)
		if err != nil {
			return "", "", fmt.Errorf("unsupported database address")
		}
		options += "protocol=tcp\nhost=" + mysqlOption(host) + "\nport=" + mysqlOption(port) + "\n"
	case "unix":
		options += "protocol=socket\nsocket=" + mysqlOption(cfg.Addr) + "\n"
	default:
		return "", "", fmt.Errorf("unsupported database transport")
	}
	if cfg.TLSConfig != "" && cfg.TLSConfig != "false" {
		return "", "", fmt.Errorf("custom TLS backup connection is not configured")
	}
	return options, name, nil
}

func runDatabaseDump(ctx context.Context, defaults, database string, out io.Writer) error {
	var bin string
	for _, name := range []string{"mariadb-dump", "mysqldump"} {
		if found, err := exec.LookPath(name); err == nil {
			bin = found
			break
		}
	}
	if bin == "" {
		return fmt.Errorf("database dump client unavailable")
	}
	args := dumpCommandArgs(defaults, database)
	if strings.HasPrefix(filepath.Base(bin), "mysqldump") {
		for i, arg := range args {
			if arg == "--skip-ssl" {
				args[i] = "--ssl-mode=DISABLED"
			}
		}
	}
	cmd := exec.CommandContext(ctx, bin, args...)
	cmd.Stdout = out
	// Do not put client stderr into a response or logs: it can contain credentials/data.
	cmd.Stderr = io.Discard
	cmd.WaitDelay = 5 * time.Second
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("database dump failed")
	}
	return nil
}

func dumpCommandArgs(defaults, database string) []string {
	return []string{"--defaults-extra-file=" + defaults, "--skip-ssl", "--single-transaction", "--quick", "--hex-blob", "--skip-lock-tables", "--skip-add-locks", "--skip-comments", "--no-tablespaces", "--routines", "--events", "--triggers", "--databases", database}
}

// download 导出全库备份
// @Summary 下载当前数据库完整SQL备份
// @Description 仅eugene且为超级管理员可操作，管理员Cookie鉴权。导出当前服务连接数据库的全部表结构、数据、触发器、事件及存储程序，包含逻辑删除数据和schema_migrations。使用事务快照，不阻塞日常InnoDB写入；备份期间不要执行表结构变更。成功直接下载SQL，不提供公开链接，不包含APK和.env；临时文件在请求完成后删除。同一时间仅允许一个备份，最长10分钟。
// @Tags 后台 · 数据备份
// @Produce application/sql
// @Success 200 {file} file "数据库SQL备份"
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 403 {object} response.ErrorEnvelope
// @Failure 429 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/admin/data-backup/download [post]
func (b *backupHandler) download(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	c.Header("X-Content-Type-Options", "nosniff")
	if !assetReviewer(c) {
		response.Error(c, 403, response.CodeForbidden, "仅eugene超级管理员可以导出数据备份")
		return
	}
	select {
	case b.active <- struct{}{}:
		defer func() { <-b.active }()
	default:
		response.Error(c, 429, response.CodeTooManyRequests, "已有备份正在生成或下载，请稍后再试")
		return
	}
	ctx, cancel := context.WithTimeout(c.Request.Context(), 10*time.Minute)
	defer cancel()
	options, name, err := b.clientOptions(ctx)
	if err != nil {
		b.auth.fail(c, err)
		return
	}
	dir, err := os.MkdirTemp("", "ths-backup-*")
	if err != nil {
		b.auth.fail(c, fmt.Errorf("cannot create backup workspace"))
		return
	}
	defer os.RemoveAll(dir)
	defaults := filepath.Join(dir, "client.cnf")
	if err = os.WriteFile(defaults, []byte(options), 0600); err != nil {
		b.auth.fail(c, fmt.Errorf("cannot prepare backup connection"))
		return
	}
	file, err := os.OpenFile(filepath.Join(dir, "backup.sql"), os.O_CREATE|os.O_RDWR, 0600)
	if err != nil {
		b.auth.fail(c, fmt.Errorf("cannot create backup file"))
		return
	}
	defer file.Close()
	if err = b.run(ctx, defaults, name, file); err != nil {
		b.auth.fail(c, fmt.Errorf("数据库备份失败，请检查数据库客户端及权限"))
		return
	}
	stat, err := file.Stat()
	if err != nil || stat.Size() == 0 {
		b.auth.fail(c, fmt.Errorf("empty database backup"))
		return
	}
	if _, err = file.Seek(0, io.SeekStart); err != nil {
		b.auth.fail(c, fmt.Errorf("cannot read backup"))
		return
	}
	// MariaDB clients prepend a sandbox command that older MySQL clients cannot
	// import. It is a client instruction, not database content; omit only that line.
	reader := bufio.NewReader(file)
	first, err := reader.ReadString('\n')
	if err != nil && err != io.EOF {
		b.auth.fail(c, fmt.Errorf("cannot read backup header"))
		return
	}
	offset := int64(0)
	if strings.HasPrefix(first, "/*") && strings.Contains(first, "enable the sandbox mode") {
		offset = int64(len(first))
	}
	if _, err = file.Seek(offset, io.SeekStart); err != nil {
		b.auth.fail(c, fmt.Errorf("cannot read backup"))
		return
	}
	filename := "thsbackup" + time.Now().In(time.FixedZone("Asia/Shanghai", 8*3600)).Format("20060102150405") + ".sql"
	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, filename))
	c.Header("X-Accel-Buffering", "no")
	slog.Info("database backup download", "database", name, "bytes", stat.Size()-offset, "administrator", "eugene")
	c.DataFromReader(200, stat.Size()-offset, "application/sql; charset=utf-8", file, nil)
}
