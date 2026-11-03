package admin

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"reflect"
	"regexp"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/tax"
)

func testTaxCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if err := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES (51,'13900000051','税务会员甲','unused',1),(52,'13900000052','税务会员乙','unused',1)").Error; err != nil {
		t.Fatal(err)
	}
	service := tax.NewService(db)
	tokens := auth.NewTokenIssuer("tax-test-key", time.Hour)
	app := gin.New()
	app.POST("/api/app/tax/profile", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), tax.NewHandler(service).Mine)
	app.POST("/v1/tax-user/info", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), tax.NewHandler(service).Mine)
	appCall := func(uid uint64, query string, status int) tax.View {
		req := httptest.NewRequest("POST", "/v1/tax-user/info"+query, nil)
		token, _ := tokens.Issue(uid)
		if uid != 0 {
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("tax App HTTP%d want%d %s", w.Code, status, w.Body.String())
		}
		if status != 200 {
			return tax.View{}
		}
		var data tax.AppProfileResponse
		if err := json.Unmarshal(w.Body.Bytes(), &data); err != nil {
			t.Fatal(err)
		}
		stored, err := service.Get(t.Context(), uid)
		if err != nil {
			t.Fatal(err)
		}
		if data.Code != 1 || data.Msg != "success" || data.Data.Name != stored.Name || data.Data.Phone != stored.Phone || data.Data.TaxNumber != stored.TaxNumber || data.Data.IDCard != stored.IDCard || data.Data.BankNum != stored.BankNum || data.Data.BankType != stored.BankType || data.Data.TaxBackMoney != stored.TaxBackMoney {
			t.Fatal("tax App contract mismatch", w.Body.String())
		}
		return stored
	}
	send := func(method, path string, body any, status int) tax.View {
		b, _ := json.Marshal(body)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("%s %s HTTP%d want%d %s", method, path, w.Code, status, w.Body.String())
		}
		var data tax.ProfileResponse
		json.Unmarshal(w.Body.Bytes(), &data)
		return data.Data
	}
	root := "/api/admin/tax/profiles/51"
	if w := call("GET", "/api/admin/tax/profiles", "", nil); w.Code != 401 {
		t.Fatal("anonymous tax access")
	}
	send("GET", root, nil, 404)
	appCall(0, "", 401)
	first := appCall(51, "", 200)
	if first.BankType != "" {
		t.Fatal("bank type must be initially empty")
	}
	for _, number := range []string{first.IDCard, first.TaxNumber, first.BankNum} {
		if !regexp.MustCompile(`^[1-9][0-9]{11}$`).MatchString(number) {
			t.Fatal("App generated nonnumeric placeholder", number)
		}
	}
	again := appCall(51, "?userId=52", 200)
	if first.UserID != 51 || !first.Simulated || !reflect.DeepEqual(first, again) || len(strings.Split(first.TaxBackMoney, ",")) != 5 {
		t.Fatal("profile not stable/private")
	}
	var wg sync.WaitGroup
	versions := make(chan string, 6)
	for range 6 {
		wg.Go(func() {
			v, err := service.Mine(t.Context(), 52)
			if err != nil {
				t.Error(err)
				return
			}
			versions <- v.Version
		})
	}
	wg.Wait()
	close(versions)
	var previous string
	for version := range versions {
		if previous != "" && version != previous {
			t.Fatal("duplicate initialization")
		}
		previous = version
	}
	w := call("GET", "/api/admin/tax/profiles?q=税务会员&pageSize=20", "", cookie)
	var list tax.ListResponse
	json.Unmarshal(w.Body.Bytes(), &list)
	if w.Code != 200 || list.Data.Total != 2 || len(list.Data.Items) != 2 || list.Data.Items[0].TaxBackMoney == "" {
		t.Fatal("tax list mapping", w.Body.String())
	}
	req := tax.WriteRequest{Fields: first.Fields, ExpectedVersion: first.Version}
	req.Name = "独立税务姓名"
	req.BankType = "自定义银行类型"
	req.Phone = "13800000051"
	req.TaxBackMoney = "1,2.1,3.02,4,5.99"
	edited := send("PUT", root, req, 200)
	if edited.TaxBackMoney != "1.00,2.10,3.02,4.00,5.99" {
		t.Fatal("refund normalization", edited.TaxBackMoney)
	}
	if got := appCall(51, "", 200); got.Name != "独立税务姓名" || got.Version != edited.Version || got.Phone != req.Phone || got.BankType != req.BankType {
		t.Fatal("App did not see edit")
	}
	var member auth.User
	db.First(&member, 51)
	if member.RealName != "税务会员甲" || *member.Phone != "13900000051" || member.PasswordHash != "unused" {
		t.Fatal("tax edit changed member")
	}
	send("PUT", root, req, 409)
	req.ExpectedVersion = edited.Version
	req.TaxBackMoney = "-1,2,3,4,5"
	send("PUT", root, req, 400)
	after, _ := service.Get(t.Context(), 51)
	if after.Version != edited.Version {
		t.Fatal("invalid edit persisted")
	}
	send("DELETE", root, tax.WriteRequest{ExpectedVersion: edited.Version}, 200)
	send("GET", root, nil, 404)
	req.TaxBackMoney = "1,2,3,4,5"
	req.ExpectedVersion = ""
	created := send("POST", root, req, 200)
	send("POST", root, req, 409)
	send("POST", root+"/generate", tax.WriteRequest{}, 409)
	generated := send("POST", root+"/generate", tax.WriteRequest{ExpectedVersion: created.Version}, 200)
	for _, number := range []string{generated.IDCard, generated.TaxNumber, generated.BankNum} {
		if !regexp.MustCompile(`^[1-9][0-9]{11}$`).MatchString(number) {
			t.Fatal("backend generated invalid number", number)
		}
	}
	if got := appCall(51, "", 200); got.IDCard != generated.IDCard || got.TaxNumber != generated.TaxNumber || got.BankNum != generated.BankNum {
		t.Fatal("App did not read saved numeric identifiers")
	}
	if generated.Version == created.Version || generated.Name != "税务会员甲" {
		t.Fatal("regeneration failed")
	}
	send("DELETE", root, tax.WriteRequest{ExpectedVersion: created.Version}, 409)
	send("DELETE", root, tax.WriteRequest{ExpectedVersion: generated.Version}, 200)
	if got := appCall(51, "", 200); got.Version == generated.Version {
		t.Fatal("did not generate after delete")
	}
	db.Delete(&auth.User{}, 52)
	appCall(52, "", 401)
	send("GET", "/api/admin/tax/profiles/52", nil, 404)
	send("POST", "/api/admin/tax/profiles/52/generate", tax.WriteRequest{}, 404)
	var count int64
	db.Model(&tax.Profile{}).Where("user_id = ?", 52).Count(&count)
	if count != 1 {
		t.Fatal("deleted member lost historical tax data")
	}
	db.Model(&tax.Audit{}).Count(&count)
	if count != 8 {
		t.Fatalf("expected8 successful tax audit entries got%d", count)
	}
}
