package admin

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/portfolio"
)

func TestMemberPasswordPolicy(t *testing.T) {
	for _, password := range []string{"123456", "abcdef", "!@#$%^", "中文密码测试", "😀😀😀😀😀😀", strings.Repeat("a", 72)} {
		if err := auth.ValidateMemberPassword(password); err != nil {
			t.Errorf("password rejected: %v", err)
		}
	}
	for _, password := range []string{"", "12345", "中文密码测", strings.Repeat("a", 73), strings.Repeat("😀", 19)} {
		if err := auth.ValidateMemberPassword(password); err == nil {
			t.Error("invalid password accepted")
		}
	}
}
func testMembersCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	send := func(method, url string, body any, status int) MemberView {
		encoded, _ := json.Marshal(body)
		w := call(method, url, string(encoded), cookie)
		if w.Code != status {
			t.Fatalf("%s %s: %d %s", method, url, w.Code, w.Body.String())
		}
		if strings.Contains(w.Body.String(), "passwordHash") || strings.Contains(w.Body.String(), "password_hash") {
			t.Fatal("password hash leaked")
		}
		var result MemberResponse
		json.Unmarshal(w.Body.Bytes(), &result)
		return result.Data
	}
	if w := call("GET", "/api/admin/members", "", nil); w.Code != 401 {
		t.Fatal("members accepted anonymous")
	}
	fields := MemberFields{RealName: "会员测试", Email: "Member-CRUD@example.com", Status: 1}
	for _, password := range []string{"", "12345", "中文密码测"} {
		send("POST", "/api/admin/members", MemberCreate{MemberFields: fields, Password: password}, 400)
	}
	u := send("POST", "/api/admin/members", MemberCreate{MemberFields: fields, Password: "123456"}, 200)
	if u.ID == 0 || u.Email == nil || *u.Email != "member-crud@example.com" {
		t.Fatal("invalid new member")
	}
	root := fmt.Sprintf("/api/admin/members/%d", u.ID)
	send("POST", "/api/admin/members", MemberCreate{MemberFields: fields, Password: "123456"}, 409)
	var stored auth.User
	db.First(&stored, u.ID)
	if stored.PasswordHash == "123456" || bcrypt.CompareHashAndPassword([]byte(stored.PasswordHash), []byte("123456")) != nil {
		t.Fatal("password not hashed")
	}
	originalHash := stored.PasswordHash
	tokens := auth.NewTokenIssuer("member-test", time.Hour)
	service := auth.NewService(auth.NewRepository(db), tokens, 3600)
	login, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "123456"})
	if err != nil {
		t.Fatal(err)
	}
	protected := gin.New()
	protected.GET("/protected", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), func(c *gin.Context) { c.Status(200) })
	checkAccess := func(want int) {
		req := httptest.NewRequest("GET", "/protected", nil)
		req.Header.Set("Authorization", "Bearer "+login.AccessToken)
		w := httptest.NewRecorder()
		protected.ServeHTTP(w, req)
		if w.Code != want {
			t.Fatalf("member access: got%d want%d", w.Code, want)
		}
	}
	checkAccess(200)
	// Reject password on the regular edit endpoint; profile-only edit preserves hash.
	send("PUT", root, map[string]any{"realName": "修改姓名", "phone": "", "email": *u.Email, "status": 1, "password": "654321"}, 400)
	fields.RealName = "修改姓名"
	fields.Email = *u.Email
	edited := send("PUT", root, fields, 200)
	if edited.RealName != fields.RealName {
		t.Fatal("edit failed")
	}
	db.First(&stored, u.ID)
	if stored.PasswordHash != originalHash {
		t.Fatal("profile edit changed password")
	}
	send("PUT", root+"/password", MemberPassword{Password: "short"}, 400)
	send("PUT", root+"/password", MemberPassword{Password: "!@#$%^"}, 200)
	if _, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "123456"}); !errors.Is(err, auth.ErrInvalidCredentials) {
		t.Fatal("old password accepted")
	}
	if _, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "!@#$%^"}); err != nil {
		t.Fatal("new password cannot log in", err)
	}
	fields.Status = 2
	send("PUT", root, fields, 200)
	checkAccess(401)
	if _, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "!@#$%^"}); !errors.Is(err, auth.ErrUserDisabled) {
		t.Fatal("disabled member login")
	}
	fields.Status = 1
	send("PUT", root, fields, 200)
	checkAccess(200)
	send("GET", root, nil, 200)
	if w := call("GET", "/api/admin/members?q=member-crud&page=1&pageSize=20", "", cookie); w.Code != 200 || !strings.Contains(w.Body.String(), `"total":1`) {
		t.Fatal("member search failed", w.Body.String())
	}
	// Keep historical holdings through member deletion.
	snapshot := portfolio.CategorySnapshot{UserID: u.ID, Category: "a_stock", Generation: "retained", TotalAssets: "200000.00", Payload: `{"items":[]}`}
	if err := db.Create(&snapshot).Error; err != nil {
		t.Fatal(err)
	}
	send("DELETE", root, nil, 200)
	checkAccess(401)
	var deleted auth.User
	if err := db.Unscoped().First(&deleted, u.ID).Error; err != nil || !deleted.DeletedAt.Valid {
		t.Fatal("physical deletion or missing deleted_at")
	}
	var count int64
	db.Model(&portfolio.CategorySnapshot{}).Where("user_id = ?", u.ID).Count(&count)
	if count != 1 {
		t.Fatal("member holdings physically deleted")
	}
	send("GET", root, nil, 404)
	send("PUT", root, fields, 404)
	send("PUT", root+"/password", MemberPassword{Password: "abcdef"}, 404)
	if _, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "!@#$%^"}); !errors.Is(err, auth.ErrInvalidCredentials) {
		t.Fatal("deleted member login accepted")
	}
	if w := call("GET", "/api/admin/members?q=member-crud", "", cookie); !strings.Contains(w.Body.String(), `"total":0`) {
		t.Fatal("deleted member listed", w.Body.String())
	}
	if w := call("GET", "/api/admin/holding-members?q=member-crud", "", cookie); !strings.Contains(w.Body.String(), `"data":[]`) {
		t.Fatal("deleted member offered for holdings", w.Body.String())
	}
	send("POST", "/api/admin/members", MemberCreate{MemberFields: fields, Password: "abcdef"}, 409)
	db.Model(&MemberAudit{}).Where("user_id = ?", u.ID).Count(&count)
	if count != 6 {
		t.Fatalf("expected6 successful member audit records, got%d", count)
	}
	deletedPath := "/api/admin/deleted-members"
	if w := call("GET", deletedPath, "", nil); w.Code != 401 {
		t.Fatal("deleted list missing auth", w.Code)
	}
	if w := call("GET", deletedPath+"?page=0", "", cookie); w.Code != 400 {
		t.Fatal("invalid pagination accepted")
	}
	w := call("GET", deletedPath+"?q=member-crud", "", cookie)
	var removed DeletedMemberPageResponse
	if err := json.Unmarshal(w.Body.Bytes(), &removed); err != nil || w.Code != 200 || removed.Data.Total != 1 || len(removed.Data.Items) != 1 || removed.Data.Items[0].DeletedAt.IsZero() {
		t.Fatal("deleted search", w.Body.String(), err)
	}
	if strings.Contains(w.Body.String(), "password") {
		t.Fatal("deleted list leaked password")
	}
	restorePath := fmt.Sprintf("%s/%d/restore", deletedPath, u.ID)
	if w := call("POST", restorePath, "", nil); w.Code != 401 {
		t.Fatal("restore missing auth")
	}
	send("POST", restorePath, nil, 200)
	send("POST", restorePath, nil, 404)
	send("GET", root, nil, 200)
	var restored auth.User
	if err := db.First(&restored, u.ID).Error; err != nil || restored.Status != 1 || restored.DeletedAt.Valid || restored.PasswordHash != deleted.PasswordHash {
		t.Fatal("restored identity/password", err)
	}
	if _, err := service.Login(auth.LoginRequest{Account: *u.Email, Password: "!@#$%^"}); err != nil {
		t.Fatal("restored login", err)
	}
	checkAccess(200)
	if w := call("GET", deletedPath+"?q=member-crud", "", cookie); !strings.Contains(w.Body.String(), `"total":0`) {
		t.Fatal("restored member still deleted", w.Body.String())
	}
	if w := call("GET", "/api/admin/members?q=member-crud", "", cookie); !strings.Contains(w.Body.String(), `"total":1`) {
		t.Fatal("restored member missing", w.Body.String())
	}
	db.Model(&portfolio.CategorySnapshot{}).Where("user_id = ?", u.ID).Count(&count)
	if count != 1 {
		t.Fatal("restoration lost holdings")
	}
	db.Model(&MemberAudit{}).Where("user_id = ? AND action='restore'", u.ID).Count(&count)
	if count != 1 {
		t.Fatal("restoration audit", count)
	}
}
