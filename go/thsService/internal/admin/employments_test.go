package admin

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"reflect"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/tax"
)

func testEmploymentCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if err := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES (61,'13900000061','任职会员甲','unused',1),(62,'13900000062','任职会员乙','unused',1)").Error; err != nil {
		t.Fatal(err)
	}
	service := tax.NewService(db)
	taxProfile, err := service.Mine(t.Context(), 61)
	if err != nil {
		t.Fatal(err)
	}
	tokens := auth.NewTokenIssuer("employment-test", time.Hour)
	app := gin.New()
	app.POST("/api/app/tax/companies", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), tax.NewHandler(service).MyEmployment)
	appCall := func(uid uint64, query string, status int) []tax.EmploymentView {
		req := httptest.NewRequest("POST", "/api/app/tax/companies"+query, nil)
		token, _ := tokens.Issue(uid)
		if uid != 0 {
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("App companies HTTP%d want%d %s", w.Code, status, w.Body.String())
		}
		if status != 200 {
			return nil
		}
		var data tax.AppEmploymentResponse
		if err := json.Unmarshal(w.Body.Bytes(), &data); err != nil {
			t.Fatal(err)
		}
		stored, err := service.GetEmployment(t.Context(), uid)
		if err != nil {
			t.Fatal(err)
		}
		if data.Code != 1 || data.Msg != "success" || len(data.Data) != 1 || data.Data[0].CompanyName != stored.CompanyName || data.Data[0].EndDate != stored.EndDate {
			t.Fatal("employment App contract mismatch", w.Body.String())
		}
		return []tax.EmploymentView{stored}
	}
	send := func(method, path string, body any, status int) tax.EmploymentView {
		b, _ := json.Marshal(body)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("%s %s HTTP%d want%d %s", method, path, w.Code, status, w.Body.String())
		}
		var data tax.EmploymentResponse
		json.Unmarshal(w.Body.Bytes(), &data)
		return data.Data
	}
	root := "/api/admin/tax/companies/61"
	if w := call("GET", "/api/admin/tax/companies", "", nil); w.Code != 401 {
		t.Fatal("anonymous company access")
	}
	send("GET", root, nil, 404)
	appCall(0, "", 401)
	first := appCall(61, "", 200)
	again := appCall(61, "?userId=62", 200)
	if len(first) != 1 || first[0].UserID != 61 || !reflect.DeepEqual(first, again) {
		t.Fatal("not one-element stable own array")
	}
	var wg sync.WaitGroup
	versions := make(chan string, 6)
	for range 6 {
		wg.Go(func() {
			data, err := service.MyEmployment(t.Context(), 62)
			if err != nil {
				t.Error(err)
				return
			}
			if len(data) != 1 {
				t.Error("not one employment")
				return
			}
			versions <- data[0].Version
		})
	}
	wg.Wait()
	close(versions)
	var previous string
	for v := range versions {
		if previous != "" && previous != v {
			t.Fatal("multiple initialization")
		}
		previous = v
	}
	var count int64
	db.Model(&tax.Employment{}).Where("user_id = ?", 62).Count(&count)
	if count != 1 {
		t.Fatal("not 1:1")
	}
	w := call("GET", "/api/admin/tax/companies?q=任职会员&pageSize=20", "", cookie)
	var list tax.EmploymentListResponse
	json.Unmarshal(w.Body.Bytes(), &list)
	if w.Code != 200 || list.Data.Total != 2 || list.Data.Items[0].CompanyName == "" {
		t.Fatal("list mapping", w.Body.String())
	}
	req := tax.EmploymentWrite{EmploymentFields: first[0].EmploymentFields, ExpectedVersion: first[0].Version}
	req.CompanyName = "后台模拟任职单位"
	req.Post = "模拟经理"
	req.EndDate = time.Now().In(time.FixedZone("CST", 8*3600)).Format("2006-01-02")
	edited := send("PUT", root, req, 200)
	if got := appCall(61, "", 200); got[0].CompanyName != req.CompanyName || got[0].Version != edited.Version || got[0].EndDate != req.EndDate {
		t.Fatal("edit not synchronized")
	}
	send("PUT", root, req, 409)
	req.ExpectedVersion = edited.Version
	req.EndDate = "1900-01-01"
	send("PUT", root, req, 400)
	after, _ := service.GetEmployment(t.Context(), 61)
	if after.Version != edited.Version {
		t.Fatal("invalid edit persisted")
	}
	req.EndDate = ""
	send("DELETE", root, tax.EmploymentWrite{ExpectedVersion: edited.Version}, 200)
	send("GET", root, nil, 404)
	req.ExpectedVersion = ""
	created := send("POST", root, req, 200)
	send("POST", root, req, 409)
	send("POST", root+"/generate", tax.EmploymentWrite{}, 409)
	generated := send("POST", root+"/generate", tax.EmploymentWrite{ExpectedVersion: created.Version}, 200)
	if generated.Version == created.Version || generated.EndDate != "" {
		t.Fatal("generate failed")
	}
	send("DELETE", root, tax.EmploymentWrite{ExpectedVersion: created.Version}, 409)
	send("DELETE", root, tax.EmploymentWrite{ExpectedVersion: generated.Version}, 200)
	if got := appCall(61, "", 200); len(got) != 1 || got[0].Version == generated.Version {
		t.Fatal("did not regenerate after deletion")
	}
	var member auth.User
	db.First(&member, 61)
	if member.RealName != "任职会员甲" || member.PasswordHash != "unused" {
		t.Fatal("employment modified member")
	}
	unchanged, err := service.Mine(t.Context(), 61)
	if err != nil || !reflect.DeepEqual(taxProfile, unchanged) {
		t.Fatal("employment modified tax profile")
	}
	db.Delete(&auth.User{}, 62)
	appCall(62, "", 401)
	send("GET", "/api/admin/tax/companies/62", nil, 404)
	send("POST", "/api/admin/tax/companies/62/generate", tax.EmploymentWrite{}, 404)
	db.Model(&tax.Employment{}).Where("user_id = ?", 62).Count(&count)
	if count != 1 {
		t.Fatal("lost deleted member historical employment")
	}
	db.Model(&tax.EmploymentAudit{}).Count(&count)
	if count != 8 {
		t.Fatalf("expected8 audit records got%d", count)
	}
}
