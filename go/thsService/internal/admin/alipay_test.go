package admin

import (
	"encoding/json"
	"fmt"
	"math"
	"net/http"
	"net/http/httptest"
	"net/url"
	"reflect"
	"strconv"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/alipay"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
)

func testAlipayCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if e := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES (91,'13900000091','支付宝甲','unused',1),(92,'13900000092','支付宝乙','unused',1)").Error; e != nil {
		t.Fatal(e)
	}
	service := alipay.NewService(db)
	tokens := auth.NewTokenIssuer("alipay-test", time.Hour)
	app := gin.New()
	alipay.Register(app.Group("", alipay.RequireMember(db, tokens)), service)
	appCall := func(uid uint64, path string, status, code int) alipay.AppResponse {
		t.Helper()
		req := httptest.NewRequest("GET", path, nil)
		if uid != 0 {
			token, _ := tokens.Issue(uid)
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("App %s got%d want%d: %s", path, w.Code, status, w.Body.String())
		}
		var out alipay.AppResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		if out.Code != code || out.Msg == "" {
			t.Fatal("App contract", w.Body.String())
		}
		if strings.Contains(w.Body.String(), "password") {
			t.Fatal("password leaked")
		}
		return out
	}
	adminCall := func(method, path string, req any, status int) *httptest.ResponseRecorder {
		t.Helper()
		b, _ := json.Marshal(req)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("admin %s %s got%d want%d: %s", method, path, w.Code, status, w.Body.String())
		}
		return w
	}
	list := func(kind string, year int) alipay.List {
		out, e := service.List(t.Context(), kind, 91, year, "", 1, 100)
		if e != nil {
			t.Fatal(e)
		}
		return out
	}
	fields := func(r alipay.Record) map[string]any {
		var p map[string]any
		if e := json.Unmarshal(r.Payload, &p); e != nil {
			t.Fatal(e)
		}
		return p
	}
	parse := func(s string) int64 {
		n, e := strconv.ParseFloat(s, 64)
		if e != nil {
			t.Fatal(e)
		}
		return int64(math.Round(n * 100))
	}
	appCall(0, "/api/user/profile", 401, 0)
	req := httptest.NewRequest("GET", "/api/user/profile", nil)
	req.Header.Set("Authorization", "Bearer invalid")
	w := httptest.NewRecorder()
	app.ServeHTTP(w, req)
	if w.Code != 401 || !strings.Contains(w.Body.String(), `"msg"`) {
		t.Fatal("invalid token contract")
	}
	if w := call("GET", "/api/admin/alipay/profile", "", nil); w.Code != 401 {
		t.Fatal("anonymous admin")
	}
	adminCall("GET", "/api/admin/alipay/schemas", nil, 200)
	if list("profile", 0).Total != 0 {
		t.Fatal("admin generated data")
	}
	appCall(91, "/api/insurance/records", 200, 0)
	appCall(91, "/api/insurance/records?year=1999", 200, 0)
	appCall(91, "/api/insurance/records?year=2025&year=2024", 200, 0)
	appCall(91, "/api/insurance/record?year=2025&month=202413&company=test", 200, 0)
	records := appCall(91, "/api/insurance/records?year=2025", 200, 1)
	if !reflect.DeepEqual(records, appCall(91, "/api/insurance/records?year=2025&userId=92", 200, 1)) {
		t.Fatal("unstable or cross-member records")
	}
	rdata := records.Data.(map[string]any)
	rows := rdata["list"].([]any)
	if len(rows) != 12 || rdata["year"] != "2025" {
		t.Fatal("record generation")
	}
	var total int64
	for _, row := range rows {
		total += parse(row.(map[string]any)["amount"].(string))
	}
	summary := appCall(91, "/api/insurance/summary", 200, 1).Data.(map[string]any)
	if summary["manageCode"] != rdata["manageCode"] || summary["cityName"] != rdata["cityName"] || summary["firstDate"] != "20250101" || summary["lastMonth"] != "202512" || summary["monthCount"] != "12" || parse(summary["personalAmount"].(string))+parse(summary["companyAmount"].(string)) != total {
		t.Fatal("summary mismatch", summary)
	}
	first := rows[0].(map[string]any)
	detailPath := "/api/insurance/record?" + url.Values{"year": {"2025"}, "month": {first["month"].(string)}, "company": {first["company"].(string)}}.Encode()
	detail := appCall(91, detailPath, 200, 1).Data.(map[string]any)
	if detail["year"] != first["month"] || detail["unitName"] != first["company"] || detail["total"] != first["amount"] || parse(detail["companyPay"].(string))+parse(detail["personalPay"].(string)) != parse(detail["total"].(string)) {
		t.Fatal("detail mismatched")
	}
	appCall(91, "/api/insurance/record?year=2025&month=202501&company=notfound", 200, 0)
	paths := map[string]string{"profile": "/api/user/profile", "insurance": "/api/insurance/summary", "accounts": "/api/life/accounts", "electric": "/api/utility/electric", "water": "/api/utility/water"}
	for kind, path := range paths {
		a := appCall(91, path, 200, 1)
		if !reflect.DeepEqual(a, appCall(91, path, 200, 1)) {
			t.Fatal("unstable singleton", kind)
		}
		if !reflect.DeepEqual(a, appCall(91, path+"?userId=92", 200, 1)) {
			t.Fatal("cross-member singleton")
		}
		appCall(92, path, 200, 1)
	}
	// Six concurrent first-year queries must share one record set.
	var wg sync.WaitGroup
	ids := make(chan uint64, 6)
	for range 6 {
		wg.Go(func() {
			rows, e := service.Ensure(t.Context(), "records", 92, 2025)
			if e != nil {
				t.Error(e)
				return
			}
			if len(rows) != 12 {
				t.Error("bad concurrent count")
			}
			ids <- rows[0].ID
		})
	}
	wg.Wait()
	close(ids)
	var prev uint64
	for id := range ids {
		if prev != 0 && prev != id {
			t.Fatal("duplicate initialization")
		}
		prev = id
	}
	// Backend editing of each category must be immediately visible to the App.
	for _, kind := range []string{"profile", "insurance", "records", "accounts", "electric", "water"} {
		year := 0
		if kind == "records" {
			year = 2025
		}
		row := list(kind, year).Items[0]
		p := fields(row)
		root := "/api/admin/alipay/" + kind
		path := fmt.Sprintf("%s/%d", root, row.ID)
		switch kind {
		case "profile":
			p["name"] = "独立展示姓名"
			p["phone"] = "13900000099"
		case "insurance":
			p["cityName"] = "广州市"
			p["interestAmount"] = "123.45"
		case "records":
			p["companyPay"] = "1000.00"
			p["personalPay"] = "500.00"
		case "accounts":
			p["rightName"] = ""
			p["rightAccountNo"] = ""
		case "electric":
			p["amount"] = "88.80"
		case "water":
			p["amount"] = "18.01"
			p["addressLine2"] = ""
		}
		req := alipay.Write{UserID: 91, Year: year, Payload: p, ExpectedVersion: row.Version}
		var edited AlipayRecordResponse
		json.Unmarshal(adminCall("PUT", path, req, 200).Body.Bytes(), &edited)
		adminCall("PUT", path, req, 409)
		adminCall("GET", path, nil, 200)
		if kind == "records" {
			data := appCall(91, detailPath, 200, 1).Data.(map[string]any)
			if data["total"] != "1500.00" {
				t.Fatal("record edit not reflected")
			}
			sum := appCall(91, "/api/insurance/summary", 200, 1).Data.(map[string]any)
			if parse(sum["companyAmount"].(string))+parse(sum["personalAmount"].(string)) != total-parse(first["amount"].(string))+150000 {
				t.Fatal("summary edit not reflected")
			}
		} else {
			data := appCall(91, paths[kind], 200, 1).Data.(map[string]any)
			switch kind {
			case "profile":
				if data["name"] != p["name"] || data["phone"] != p["phone"] {
					t.Fatal("profile edit")
				}
			case "insurance":
				if data["cityName"] != "广州市" || data["interestAmount"] != "123.45" {
					t.Fatal("insurance edit")
				}
			case "accounts":
				if data["right"].(map[string]any)["name"] != "" || data["right"].(map[string]any)["accountNo"] != "" {
					t.Fatal("single household")
				}
			default:
				if data["amount"] != p["amount"] {
					t.Fatal("utility edit")
				}
			}
		}
		adminCall("POST", root, alipay.Write{UserID: 91, Year: year, Payload: p}, 409)
		adminCall("DELETE", path, alipay.Write{ExpectedVersion: row.Version}, 409)
		adminCall("DELETE", path, alipay.Write{ExpectedVersion: edited.Data.Version}, 200)
		adminCall("GET", path, nil, 404)
		var created AlipayRecordResponse
		json.Unmarshal(adminCall("POST", root, alipay.Write{UserID: 91, Year: year, Payload: p}, 200).Body.Bytes(), &created)
		if created.Data.Version == row.Version {
			t.Fatal("version reused")
		}
		adminCall("POST", root, alipay.Write{UserID: 999999, Year: year, Payload: p}, 404)
		bad := fields(created.Data)
		bad["unexpected"] = "x"
		adminCall("PUT", fmt.Sprintf("%s/%d", root, created.Data.ID), alipay.Write{ExpectedVersion: created.Data.Version, Payload: bad}, 400)
	}
	// Changes stay independent of member authentication and the Southern Grid module.
	var member auth.User
	db.First(&member, 91)
	if *member.Phone != "13900000091" || member.RealName != "支付宝甲" || member.PasswordHash != "unused" {
		t.Fatal("modified member credentials")
	}
	var electricCount int64
	db.Model(&electricity.Profile{}).Where("user_id=?", 91).Count(&electricCount)
	if electricCount != 0 {
		t.Fatal("created Southern Grid data")
	}
	listData := list("records", 2025)
	one := listData.Items[0]
	adminCall("DELETE", fmt.Sprintf("/api/admin/alipay/records/%d", one.ID), alipay.Write{ExpectedVersion: one.Version}, 200)
	if len(appCall(91, "/api/insurance/records?year=2025", 200, 1).Data.(map[string]any)["list"].([]any)) != 11 {
		t.Fatal("partial delete topped up")
	}
	for _, r := range list("records", 2025).Items {
		if _, e := service.Write(t.Context(), 1, "records", r.ID, alipay.Write{ExpectedVersion: r.Version}, "delete"); e != nil {
			t.Fatal(e)
		}
	}
	if len(appCall(91, "/api/insurance/records?year=2025", 200, 1).Data.(map[string]any)["list"].([]any)) != 12 {
		t.Fatal("empty year not regenerated")
	}
	profileRow := list("profile", 0).Items[0]
	adminCall("DELETE", fmt.Sprintf("/api/admin/alipay/profile/%d", profileRow.ID), alipay.Write{ExpectedVersion: profileRow.Version}, 200)
	if appCall(91, "/api/user/profile", 200, 1).Data.(map[string]any)["name"] != "支付宝甲" {
		t.Fatal("profile not regenerated")
	}
	var audits int64
	db.Model(&alipay.Audit{}).Where("admin_id IS NOT NULL").Count(&audits)
	if audits < 18 {
		t.Fatal("missing audits")
	}
	if e := db.Delete(&auth.User{}, 92).Error; e != nil {
		t.Fatal(e)
	}
	appCall(92, "/api/user/profile", 401, 0)
	deleted, e := service.List(t.Context(), "records", 92, 2025, "", 1, 100)
	if e != nil || deleted.Total != 0 {
		t.Fatal("deleted member visible")
	}
	var retained int64
	db.Model(&alipay.Record{}).Where("user_id=? AND kind='records' AND year=2025", 92).Count(&retained)
	if retained != 12 {
		t.Fatal("deleted history lost")
	}
}
