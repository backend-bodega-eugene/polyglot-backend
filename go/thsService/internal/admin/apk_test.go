package admin

import (
	"archive/zip"
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/apkdownload"
)

func apkFixture(t *testing.T) []byte {
	t.Helper()
	var b bytes.Buffer
	z := zip.NewWriter(&b)
	f, e := z.Create("AndroidManifest.xml")
	if e != nil {
		t.Fatal(e)
	}
	f.Write([]byte{3, 0, 8, 0, 8, 0, 0, 0})
	f, _ = z.Create("classes.dex")
	f.Write([]byte("dex\n035\x00test"))
	if e := z.Close(); e != nil {
		t.Fatal(e)
	}
	return b.Bytes()
}
func TestAPKValidationAndChinesePasscode(t *testing.T) {
	for _, p := range []string{"12345", "中文五字符"} {
		h, e := passcodeHash(p)
		if e != nil || !matchesPasscode(h, p) {
			t.Fatal("five characters should be accepted", e)
		}
	}
	for _, p := range []string{"1234", "     "} {
		if _, e := passcodeHash(p); e == nil {
			t.Fatal("invalid short/blank passcode accepted")
		}
	}
	pass := "中文口令很好用！" + strings.Repeat("字", 30)
	hash, e := passcodeHash(pass)
	if e != nil || !matchesPasscode(hash, pass) || matchesPasscode(hash, pass+"错") {
		t.Fatal("unicode passcode failed", e)
	}
	if _, e := passcodeHash("短口令"); e == nil {
		t.Fatal("short passcode accepted")
	}
	if _, e := passcodeHash("abcdef\n"); e == nil {
		t.Fatal("control character accepted")
	}
	dir := t.TempDir()
	file := filepath.Join(dir, "app.apk")
	os.WriteFile(file, apkFixture(t), 0600)
	if e := validateAPK(file); e != nil {
		t.Fatal(e)
	}
	os.WriteFile(file, []byte("not an apk"), 0600)
	if validateAPK(file) == nil {
		t.Fatal("renamed fake APK accepted")
	}
	var bad bytes.Buffer
	z := zip.NewWriter(&bad)
	f, _ := z.Create("AndroidManifest.xml")
	f.Write([]byte("<manifest></manifest>"))
	z.Close()
	os.WriteFile(file, bad.Bytes(), 0600)
	if validateAPK(file) == nil {
		t.Fatal("plain zip XML accepted")
	}
}

func TestAPKPageKeepsSameOriginFormSubmission(t *testing.T) {
	r := gin.New()
	Register(r, nil, "test-secret", 3600)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, httptest.NewRequest("GET", "/app-download", nil))
	if w.Code != 200 || w.Header().Get("Referrer-Policy") != "same-origin" || !strings.Contains(w.Body.String(), `name="referrer" content="same-origin"`) {
		t.Fatal("download form policy would strip Origin")
	}
	gate := gin.New()
	gate.POST("/download", sameOrigin, func(c *gin.Context) { c.Status(204) })
	for origin, want := range map[string]int{"http://localhost:9527": 204, "null": 403, "https://evil.example": 403} {
		req := httptest.NewRequest("POST", "http://localhost:9527/download", strings.NewReader("passcode=eugenecoming"))
		req.Header.Set("Origin", origin)
		result := httptest.NewRecorder()
		gate.ServeHTTP(result, req)
		if result.Code != want {
			t.Fatalf("Origin %s HTTP%d want%d", origin, result.Code, want)
		}
	}
}

func testAPKDownload(t *testing.T, db *gorm.DB, r *gin.Engine, cookie *http.Cookie) {
	t.Helper()
	call := func(method, path string, body io.Reader, contentType string, admin bool) *httptest.ResponseRecorder {
		req := httptest.NewRequest(method, path, body)
		req.Header.Set("Content-Type", contentType)
		if contentType == "application/x-www-form-urlencoded" {
			req.Header.Set("Origin", "http://example.com")
		}
		req.RemoteAddr = "192.0.2.100:1234"
		if admin {
			req.AddCookie(cookie)
		}
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		return w
	}
	check := func(w *httptest.ResponseRecorder, status int) {
		t.Helper()
		if w.Code != status {
			t.Fatalf("APK HTTP%d want%d: %s", w.Code, status, w.Body.String())
		}
	}
	info := func() apkdownload.Info {
		w := call("GET", "/api/app-download/info", nil, "", false)
		check(w, 200)
		var v apkdownload.InfoResponse
		json.Unmarshal(w.Body.Bytes(), &v)
		if strings.Contains(w.Body.String(), "passcode_hash") || strings.Contains(w.Body.String(), "stored_name") {
			t.Fatal("private APK fields exposed")
		}
		return v.Data
	}
	if info().Available {
		t.Fatal("empty APK published")
	}
	var initial apkdownload.State
	db.First(&initial, 1)
	if !info().PasscodeConfigured || !matchesPasscode(initial.PasscodeHash, "eugenecoming") {
		t.Fatal("default passcode not initialized")
	}
	for _, path := range []string{"/app-download", "/app-download.js"} {
		check(call("GET", path, nil, "", false), 200)
	}
	check(call("GET", "/api/admin/app-package", nil, "", false), 401)
	change := func(pass string, admin bool) *httptest.ResponseRecorder {
		b, _ := json.Marshal(apkdownload.PasscodeRequest{Passcode: pass})
		return call("PUT", "/api/admin/app-package/passcode", bytes.NewReader(b), "application/json", admin)
	}
	oldPass := "中文下载口令！"
	newPass := "新的口令很容易记！"
	check(change(oldPass, false), 401)
	check(change("123", true), 400)
	check(change(oldPass, true), 200)
	if !info().PasscodeConfigured {
		t.Fatal("saved passcode state not visible to frontend")
	}
	var state apkdownload.State
	db.First(&state, 1)
	if state.PasscodeHash == oldPass || !matchesPasscode(state.PasscodeHash, oldPass) {
		t.Fatal("passcode not hashed")
	}
	apk := apkFixture(t)
	upload := func(data []byte, admin bool) *httptest.ResponseRecorder {
		var b bytes.Buffer
		m := multipart.NewWriter(&b)

		f, _ := m.CreateFormFile("file", "release.apk")
		f.Write(data)
		m.Close()
		return call("POST", "/api/admin/app-package/upload", &b, m.FormDataContentType(), admin)
	}
	check(upload(apk, false), 401)
	check(upload(apk, true), 200)
	current := info()
	if _, err := time.ParseInLocation("20060102150405", current.ReleaseKey, time.FixedZone("CN", 8*60*60)); err != nil {
		t.Fatalf("invalid generated key: %s", current.ReleaseKey)
	}
	sum := sha256.Sum256(apk)
	if !current.Available || len(current.ReleaseKey) != 14 || current.SizeBytes != int64(len(apk)) || current.SHA256 != hex.EncodeToString(sum[:]) {
		t.Fatal(current)
	}
	db.First(&state, 1)
	check(call("GET", "/data/apks/"+state.StoredName, nil, "", false), 404)
	download := func(pass string, form bool) *httptest.ResponseRecorder {
		if form {
			return call("POST", "/api/app-download/download", strings.NewReader(url.Values{"passcode": {pass}}.Encode()), "application/x-www-form-urlencoded", false)
		}
		b, _ := json.Marshal(apkdownload.PasscodeRequest{Passcode: pass})
		return call("POST", "/api/app-download/download", bytes.NewReader(b), "application/json", false)
	}
	check(download("incorrect", false), 401)
	w := download(oldPass, false)
	check(w, 200)
	if !bytes.Equal(w.Body.Bytes(), apk) || !strings.Contains(w.Header().Get("Content-Disposition"), "attachment") || w.Header().Get("Cache-Control") != "no-store" {
		t.Fatal("bad APK download")
	}
	check(upload([]byte("invalid replacement"), true), 400)
	if info().SHA256 != current.SHA256 {
		t.Fatal("failed upload replaced current APK")
	}
	check(change(newPass, true), 200)
	if info().ReleaseKey != current.ReleaseKey {
		t.Fatal("passcode edit changed immutable release key")
	}
	check(call("PUT", "/api/admin/app-package/passcode", strings.NewReader(`{"passcode":"12345","releaseKey":"forged"}`), "application/json", true), 400)
	var forged bytes.Buffer
	fm := multipart.NewWriter(&forged)
	fm.WriteField("releaseKey", "forged")
	ff, _ := fm.CreateFormFile("file", "release.apk")
	ff.Write(apk)
	fm.Close()
	check(call("POST", "/api/admin/app-package/upload", &forged, fm.FormDataContentType(), true), 400)
	if info().ReleaseKey != current.ReleaseKey {
		t.Fatal("client changed release key")
	}
	seed, e := os.ReadFile("../../migrations/000025_apk_defaults.up.sql")
	if e != nil {
		t.Fatal(e)
	}
	for _, stmt := range strings.Split(string(seed), ";") {
		if strings.TrimSpace(stmt) != "" {
			if e := db.Exec(stmt).Error; e != nil {
				t.Fatal(e)
			}
		}
	}
	var preserved apkdownload.State
	db.First(&preserved, 1)
	if !matchesPasscode(preserved.PasscodeHash, newPass) {
		t.Fatal("default migration overwrote custom passcode")
	}
	check(download(oldPass, false), 401)
	check(download(newPass, true), 200)
	// Persisted guards work across separate instances and do not block admin login.
	for i := 0; i < 5; i++ {
		want := 401
		if i == 4 {
			want = 429
		}
		check(download("incorrect", false), want)
	}
	w = download(newPass, false)
	check(w, 429)
	if w.Header().Get("Retry-After") == "" {
		t.Fatal("missing retry")
	}
	var guard loginLimit
	if e := db.Where("key_hash=?", limitKey("apk-download-ip", "192.0.2.100")).First(&guard).Error; e != nil || guard.LockedUntil == nil {
		t.Fatal("freeze not persisted", e)
	}
	retry, e := newLoginGuard(db).gate(t.Context(), "apk-download-ip", "192.0.2.100", func() bool { return true })
	if e != nil || retry <= 0 {
		t.Fatal("freeze lost across handler", e)
	}
	db.Model(&loginLimit{}).Where("key_hash=?", guard.KeyHash).Updates(map[string]any{"locked_until": time.Now().Add(-time.Minute), "attempts": "[]"})
	check(download(newPass, false), 200)
	var audits int64
	db.Model(&apkdownload.Audit{}).Count(&audits)
	if audits != 3 {
		t.Fatalf("audit count %d", audits)
	}
}
