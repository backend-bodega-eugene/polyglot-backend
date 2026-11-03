package admin

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
)

func testElectricityCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if err := db.Exec("INSERT INTO users (id,phone,real_name,password_hash,status) VALUES (31,'13900000031','用电会员甲','unused',1),(32,'13900000032','用电会员乙','unused',1)").Error; err != nil {
		t.Fatal(err)
	}
	service := electricity.NewService(db)
	tokens := auth.NewTokenIssuer("electricity-test-secret", time.Hour)
	app := gin.New()
	app.GET("/api/app/electricity/profile", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), electricity.NewHandler(service).Mine)
	app.GET("/api/app/users", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), electricity.NewHandler(service).LookupPhone)
	lookup := func(query string, status int) electricity.AppLookupData {
		token, _ := tokens.Issue(31)
		req := httptest.NewRequest("GET", "/api/app/users"+query, nil)
		req.Header.Set("Authorization", "Bearer "+token)
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("lookup HTTP%d want%d %s", w.Code, status, w.Body.String())
		}
		if strings.Contains(w.Body.String(), "password") {
			t.Fatal("lookup leaked unrelated fields")
		}
		var data electricity.AppLookupResponse
		if err := json.Unmarshal(w.Body.Bytes(), &data); err != nil {
			t.Fatal(err)
		}
		return data.Data
	}
	lookup("", 400)
	lookup("?username=1390000003", 400)
	lookup("?username=13900000031&username=13900000032", 400)
	lookup("?keyword=13900000031&username=13900000032", 400)
	lookup("?keyword=13900000031&keyword=13900000032", 400)
	if got := lookup("?username=13900000032", 200); len(got.List) != 1 || got.List[0].ID != 32 || got.List[0].Username != "13900000032" {
		t.Fatal("phone field lookup failed", got)
	}
	if got := lookup("?username=13900000039", 200); len(got.List) != 0 {
		t.Fatal("unknown lookup not empty")
	}
	var profileCount int64
	db.Model(&electricity.Profile{}).Count(&profileCount)
	if profileCount != 1 {
		t.Fatal("phone lookup did not persist profile")
	}
	if got := lookup("?keyword=13900000032", 200); len(got.List) != 1 || got.List[0].Supplier == "" || got.List[0].MeterNo == "" || got.List[0].DisplayName == "" {
		t.Fatal("incomplete electricity lookup", got)
	}
	appCall := func(uid uint64, path string, status int) electricity.View {
		token, _ := tokens.Issue(uid)
		req := httptest.NewRequest("GET", path, nil)
		if uid != 0 {
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("App got%d want%d: %s", w.Code, status, w.Body.String())
		}
		if status != 200 {
			return electricity.View{}
		}
		var result electricity.AppProfileResponse
		if err := json.Unmarshal(w.Body.Bytes(), &result); err != nil {
			t.Fatal(err)
		}
		stored, err := service.Get(t.Context(), uid)
		if err != nil {
			t.Fatal(err)
		}
		if result.Msg != "success" || result.Data.ID != stored.ID || result.Data.DisplayName != stored.DisplayName || result.Data.MeterNo != stored.MeterNo || result.Data.UserIDNumber != stored.UserIDNumber {
			t.Fatal("App profile contract mismatch", w.Body.String())
		}
		return stored
	}
	ownPath := "/api/app/electricity/profile"
	appCall(0, ownPath, 401)
	first := appCall(31, ownPath, 200)
	again := appCall(31, ownPath+"?userId=32", 200)
	if !reflect.DeepEqual(first, again) || first.UserID != 31 || first.ID != 31 || first.Username != "13900000031" || !first.Simulated {
		t.Fatalf("unstable or cross-user profile: %+v %+v", first, again)
	}
	if len(first.UserIDNumber) <= 10 || len(first.MeterNo) <= 5 || first.PostalCode != "510000" || !strings.HasPrefix(first.Address, "广东省广州市") {
		t.Fatal("generated fields", first)
	}
	appCall(31, ownPath+"/32", 404)
	// Concurrent first reads must result in one row and a shared version.
	var wg sync.WaitGroup
	versions := make(chan string, 8)
	for range 8 {
		wg.Go(func() {
			data, err := service.Mine(t.Context(), 32)
			if err != nil {
				t.Error(err)
				return
			}
			versions <- data.Version
		})
	}
	wg.Wait()
	close(versions)
	var version string
	for v := range versions {
		if version != "" && version != v {
			t.Fatal("concurrent initialization changed data")
		}
		version = v
	}
	var count int64
	db.Model(&electricity.Profile{}).Where("user_id = ?", 32).Count(&count)
	if count != 1 {
		t.Fatal("not 1:1")
	}
	root := "/api/admin/electricity/profiles/31"
	send := func(method, path string, req any, status int) electricity.View {
		b, _ := json.Marshal(req)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("%s %s got%d want%d %s", method, path, w.Code, status, w.Body.String())
		}
		var result electricity.ProfileResponse
		json.Unmarshal(w.Body.Bytes(), &result)
		return result.Data
	}
	if w := call("GET", "/api/admin/electricity/profiles", "", nil); w.Code != 401 {
		t.Fatal("anonymous admin access")
	}
	send("GET", root, nil, 200)
	w := call("GET", "/api/admin/electricity/profiles?q=用电会员&page=1&pageSize=20", "", cookie)
	var list electricity.ListResponse
	json.Unmarshal(w.Body.Bytes(), &list)
	if w.Code != 200 || list.Data.Total != 2 || len(list.Data.Items) != 2 || list.Data.Items[0].UserID == 0 || list.Data.Items[0].DisplayName == "" {
		t.Fatalf("list mapping: %s", w.Body.String())
	}
	req := electricity.WriteRequest{Fields: first.Fields, ExpectedVersion: first.Version}
	req.DisplayName = "独立展示姓名"
	req.Address = "广东省广州市天河区后台模拟路20号101室"
	edited := send("PUT", root, req, 200)
	if edited.DisplayName != req.DisplayName {
		t.Fatal("edit failed")
	}
	mine := appCall(31, ownPath, 200)
	if mine.Version != edited.Version || mine.DisplayName != req.DisplayName {
		t.Fatal("App did not reflect edit")
	}
	var user auth.User
	db.First(&user, 31)
	if user.RealName != "用电会员甲" || user.PasswordHash != "unused" {
		t.Fatal("profile edit changed member")
	}
	send("PUT", root, req, 409)
	req.ExpectedVersion = edited.Version
	req.PostalCode = "123"
	send("PUT", root, req, 400)
	after, _ := service.Get(t.Context(), 31)
	if after.Version != edited.Version {
		t.Fatal("invalid save persisted")
	}
	req.PostalCode = "510000"
	send("DELETE", root, req, 200)
	send("GET", root, nil, 404)
	req.ExpectedVersion = ""
	created := send("POST", root, req, 200)
	send("POST", root, req, 409)
	send("POST", root+"/generate", electricity.WriteRequest{}, 409)
	generated := send("POST", root+"/generate", electricity.WriteRequest{ExpectedVersion: created.Version}, 200)
	if generated.Version == created.Version || generated.DisplayName != "用电会员甲" {
		t.Fatal("regeneration failed")
	}
	send("DELETE", root, electricity.WriteRequest{ExpectedVersion: created.Version}, 409)
	send("DELETE", root, electricity.WriteRequest{ExpectedVersion: generated.Version}, 200)
	regenerated := appCall(31, ownPath, 200)
	if regenerated.Version == generated.Version {
		t.Fatal("deleted profile did not regenerate")
	}
	// Deleted members remain in storage but cannot fetch or modify profiles.
	if err := db.Delete(&auth.User{}, 32).Error; err != nil {
		t.Fatal(err)
	}
	if got := lookup("?username=13900000032", 200); len(got.List) != 0 {
		t.Fatal("lookup returned deleted member")
	}
	appCall(32, ownPath, 401)
	send("GET", "/api/admin/electricity/profiles/32", nil, 404)
	send("POST", "/api/admin/electricity/profiles/32/generate", electricity.WriteRequest{}, 404)
	db.Model(&electricity.Profile{}).Where("user_id = ?", 32).Count(&count)
	if count != 1 {
		t.Fatal("member deletion removed historical profile")
	}
	w = call("GET", "/api/admin/electricity/profiles?userId=32", "", cookie)
	json.Unmarshal(w.Body.Bytes(), &list)
	if list.Data.Total != 0 {
		t.Fatal("deleted member appeared in list")
	}
	db.Model(&electricity.Audit{}).Count(&count)
	if count != 8 {
		t.Fatalf("expected 8 successful operation audits got%d", count)
	}
}
