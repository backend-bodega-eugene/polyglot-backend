package admin

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func TestMySQLOptionQuoting(t *testing.T) {
	if got := mysqlOption("a\"b\\c\n[client]\r\t"); got != "\"a\\\"b\\\\c\\n[client]\\r\\t\"" {
		t.Fatal(got)
	}
	args := dumpCommandArgs("/tmp/client.cnf", "ths")
	if args[0] != "--defaults-extra-file=/tmp/client.cnf" || args[len(args)-1] != "ths" {
		t.Fatal(args)
	}
	for _, flag := range []string{"--single-transaction", "--hex-blob", "--routines", "--events", "--triggers"} {
		if !strings.Contains(strings.Join(args, " "), flag) {
			t.Fatal(flag)
		}
	}
}

func TestBackupPage(t *testing.T) {
	r := gin.New()
	Register(r, nil, "test-secret", 3600)
	for _, path := range []string{"/admin/data-backup.html", "/admin/js/data-backup.js"} {
		w := httptest.NewRecorder()
		r.ServeHTTP(w, httptest.NewRequest("GET", path, nil))
		if w.Code != 200 {
			t.Fatal(path, w.Code)
		}
	}
	files, err := pages.ReadDir("web")
	if err != nil {
		t.Fatal(err)
	}
	for _, f := range files {
		if !strings.HasSuffix(f.Name(), ".html") {
			continue
		}
		html, _ := pages.ReadFile("web/" + f.Name())
		text := string(html)
		if !strings.Contains(text, "</ul></nav>") {
			continue
		}
		last := strings.LastIndex(text, "href=\"data-backup.html\"")
		if last < 0 || strings.Contains(text[last:strings.Index(text, "</ul></nav>")], "href=\"alipay.html") {
			t.Fatal("backup not last menu", f.Name())
		}
	}
}

func testBackup(t *testing.T, db *gorm.DB, r *gin.Engine, cookie *http.Cookie) {
	t.Helper()
	h := NewHandler(db, "test-secret", 3600)
	var defaultsPath string
	b := &backupHandler{auth: h, active: make(chan struct{}, 1), run: func(ctx context.Context, defaults, database string, out io.Writer) error {
		defaultsPath = defaults
		if !strings.HasPrefix(database, "ths_admin_test_") {
			t.Fatal("wrong database", database)
		}
		settings, err := os.ReadFile(defaults)
		if err != nil || !bytes.Contains(settings, []byte("[client]")) {
			t.Fatal("missing private client settings")
		}
		_, err = io.WriteString(out, "/*M!999999\\- enable the sandbox mode */\nCREATE TABLE sample(id INT);\n")
		return err
	}}
	r.POST("/backup-test", h.RequireLogin, sameOrigin, b.download)
	call := func(path string, cookie *http.Cookie, origin string) *httptest.ResponseRecorder {
		req := httptest.NewRequest("POST", path, nil)
		req.Header.Set("Origin", origin)
		if cookie != nil {
			req.AddCookie(cookie)
		}
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		return w
	}
	if w := call("/api/admin/data-backup/download", nil, ""); w.Code != 401 {
		t.Fatal("anonymous backup", w.Code)
	}
	var asen Administrator
	db.Where("username=?", "Asen").First(&asen)
	token, _ := h.tokens.Issue(asen.ID)
	other := &http.Cookie{Name: cookieName, Value: token}
	if w := call("/api/admin/data-backup/download", other, ""); w.Code != 403 {
		t.Fatal("non-owner backup", w.Code)
	}
	if w := call("/backup-test", cookie, "https://evil.example"); w.Code != 403 {
		t.Fatal("cross origin", w.Code)
	}
	w := call("/backup-test", cookie, "")
	if w.Code != 200 || w.Body.String() != "CREATE TABLE sample(id INT);\n" || w.Header().Get("Cache-Control") != "no-store" || !strings.Contains(w.Header().Get("Content-Disposition"), "thsbackup") {
		t.Fatal("backup response", w.Code, w.Body.String())
	}
	if _, err := os.Stat(filepath.Dir(defaultsPath)); !os.IsNotExist(err) {
		t.Fatal("temporary backup was retained")
	}
	b.active <- struct{}{}
	if w := call("/backup-test", cookie, ""); w.Code != 429 {
		t.Fatal("concurrent backup", w.Code)
	}
	<-b.active
	b.run = func(ctx context.Context, defaults, database string, out io.Writer) error {
		defaultsPath = defaults
		io.WriteString(out, "partial dump")
		return fmt.Errorf("dump failure")
	}
	if w := call("/backup-test", cookie, ""); w.Code != 500 || strings.Contains(w.Body.String(), "partial dump") {
		t.Fatal("partial backup leaked")
	}
	if _, err := os.Stat(filepath.Dir(defaultsPath)); !os.IsNotExist(err) {
		t.Fatal("failed backup not cleaned")
	}
	// Optional actual dump/import test using the same client shipped in our image.
	image := os.Getenv("THS_BACKUP_TEST_IMAGE")
	if image == "" {
		return
	}
	var source string
	db.Raw("SELECT DATABASE()").Scan(&source)
	backupUser := "backup" + strings.TrimPrefix(source, "ths_admin_test_")
	if err := db.Exec("CREATE USER '" + backupUser + "'@'%' IDENTIFIED BY 'localbackuptest'").Error; err != nil {
		t.Fatal(err)
	}
	defer db.Exec("DROP USER '" + backupUser + "'@'%'")
	if err := db.Exec("GRANT ALL PRIVILEGES ON `" + source + "`.* TO '" + backupUser + "'@'%'").Error; err != nil {
		t.Fatal(err)
	}
	b.run = func(ctx context.Context, defaults, database string, out io.Writer) error {
		settings, err := os.ReadFile(defaults)
		if err != nil {
			return err
		}
		settings = bytes.ReplaceAll(settings, []byte(`host="127.0.0.1"`), []byte(`host="host.docker.internal"`))
		settings = bytes.ReplaceAll(settings, []byte(`user="root"`), []byte(`user="`+backupUser+`"`))
		settings = bytes.ReplaceAll(settings, []byte(`password="root"`), []byte(`password="localbackuptest"`))
		if err = os.WriteFile(defaults, settings, 0600); err != nil {
			return err
		}
		args := []string{"run", "--rm", "--entrypoint", "mariadb-dump", "-v", filepath.Dir(defaults) + ":/backup:ro", image}
		args = append(args, dumpCommandArgs("/backup/client.cnf", database)...)
		cmd := exec.CommandContext(ctx, "docker", args...)
		cmd.Stdout = out
		cmd.Stderr = os.Stderr
		return cmd.Run()
	}
	w = call("/backup-test", cookie, "")
	if w.Code != 200 {
		t.Fatal("real dump", w.Code, w.Body.String())
	}
	restored := source + "_restored"
	defer db.Exec("DROP DATABASE IF EXISTS `" + restored + "`")
	dump := bytes.ReplaceAll(w.Body.Bytes(), []byte(source), []byte(restored))
	// Reuse protected settings, recreated because the endpoint already cleaned up.
	options, _, err := b.clientOptions(t.Context())
	if err != nil {
		t.Fatal(err)
	}
	dir := t.TempDir()
	options = strings.ReplaceAll(options, `host="127.0.0.1"`, `host="host.docker.internal"`)
	if err = os.WriteFile(filepath.Join(dir, "client.cnf"), []byte(options), 0600); err != nil {
		t.Fatal(err)
	}
	cmd := exec.CommandContext(t.Context(), "docker", "run", "--rm", "-i", "--entrypoint", "mariadb", "-v", dir+":/backup:ro", image, "--defaults-extra-file=/backup/client.cnf", "--skip-ssl", "--binary-mode")
	cmd.Stdin = bytes.NewReader(dump)
	if out, err := cmd.CombinedOutput(); err != nil {
		t.Fatalf("backup restore failed: %v %s", err, out)
	}
	var tables []string
	db.Raw("SHOW TABLES").Scan(&tables)
	for _, table := range tables {
		var original, recovered int64
		db.Raw("SELECT COUNT(*) FROM `" + source + "`.`" + table + "`").Scan(&original)
		if err := db.Raw("SELECT COUNT(*) FROM `" + restored + "`.`" + table + "`").Scan(&recovered).Error; err != nil || original != recovered {
			t.Fatal("restore count mismatch", table, original, recovered, err)
		}
		type checksum struct {
			Table    string
			Checksum *uint64
		}
		var left, right checksum
		if err := db.Raw("CHECKSUM TABLE `" + source + "`.`" + table + "`").Scan(&left).Error; err != nil {
			t.Fatal(err)
		}
		if err := db.Raw("CHECKSUM TABLE `" + restored + "`.`" + table + "`").Scan(&right).Error; err != nil {
			t.Fatal(err)
		}
		if left.Checksum == nil || right.Checksum == nil || *left.Checksum != *right.Checksum {
			t.Fatal("restored data differs", table)
		}
	}
	t.Logf("Restored complete database with %d tables", len(tables))
}
