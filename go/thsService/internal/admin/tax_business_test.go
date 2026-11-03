package admin

import (
	"bytes"
	"encoding/json"
	"fmt"
	"math"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strconv"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/tax"
	"ths-service/internal/taxbusiness"
)

func testTaxBusinessCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if e := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES (81,'13900000081','税务业务甲','unused',1),(82,'13900000082','税务业务乙','unused',1)").Error; e != nil {
		t.Fatal(e)
	}
	service := taxbusiness.NewService(db)
	tokens := auth.NewTokenIssuer("tax-business-test", time.Hour)
	app := gin.New()
	g := app.Group("", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db))
	taxbusiness.Register(g, service)
	profiles := tax.NewHandler(tax.NewService(db))
	g.POST("/v1/tax-user/info", profiles.Mine)
	g.POST("/v1/tax-user/company/list", profiles.MyEmployment)
	appCall := func(uid uint64, path, body string, status, code int) taxbusiness.AppResponse {
		t.Helper()
		req := httptest.NewRequest("POST", "/v1/tax-user/"+path, strings.NewReader(body))
		req.Header.Set("Content-Type", "application/json")
		if uid != 0 {
			token, _ := tokens.Issue(uid)
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("App %s got%d want%d: %s", path, w.Code, status, w.Body.String())
		}
		var out taxbusiness.AppResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		if status == 200 && (out.Code != code || out.Msg == "") {
			t.Fatal("tax response contract", w.Body.String())
		}
		return out
	}
	adminCall := func(method, path string, req any, status int) *httptest.ResponseRecorder {
		t.Helper()
		b, _ := json.Marshal(req)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("admin %s %s got%d want%d %s", method, path, w.Code, status, w.Body.String())
		}
		return w
	}
	list := func(kind string, year int) taxbusiness.List {
		r, e := service.List(t.Context(), kind, 81, year, "", 1, 100)
		if e != nil {
			t.Fatal(e)
		}
		return r
	}
	fields := func(r taxbusiness.Record) map[string]any {
		var p map[string]any
		d := json.NewDecoder(bytes.NewReader(r.Payload))
		d.UseNumber()
		if e := d.Decode(&p); e != nil {
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
	appCall(0, "taxYear/list", `{"year":2024}`, 401, 0)
	appCall(81, "taxYear/list", `{"year":true}`, 200, 0)
	appCall(81, "taxYear/list", `{"year":2018}`, 200, 0)
	appCall(81, "taxYear/list", `{"year":2024.5}`, 200, 0)
	adminCall("GET", "/api/admin/tax-business/schemas", nil, 200)
	if w := call("GET", "/api/admin/tax-business/monthly", "", nil); w.Code != 401 {
		t.Fatal("anonymous admin")
	}
	if list("monthly", 2024).Total != 0 {
		t.Fatal("admin generated data")
	}
	profile := appCall(81, "info", `{}`, 200, 1).Data.(map[string]any)
	for _, key := range []string{"card_type", "id_card", "tax_number", "bank_num", "tax_back_money", "huji_city", "huji_address", "usually_city", "usually_address", "contact_city", "contact_address"} {
		if _, ok := profile[key]; !ok {
			t.Fatal("profile field missing", key)
		}
	}
	if _, ok := profile["cardType"]; ok {
		t.Fatal("camelCase profile")
	}
	companies := appCall(81, "company/list", `{}`, 200, 1).Data.([]any)
	if len(companies) != 1 || companies[0].(map[string]any)["company_name"] == nil {
		t.Fatal("company contract")
	}
	first := appCall(81, "taxYear/list", `{"year":"2024"}`, 200, 1)
	again := appCall(81, "taxYear/list", `{"year":2024}`, 200, 1)
	if !reflect.DeepEqual(first, again) {
		t.Fatal("unstable annual")
	}
	a := first.Data.(map[string]any)
	monthly := a["list"].([]any)
	if len(monthly) != 12 {
		t.Fatal("not 12 months")
	}
	var income, taxes int64
	for _, row := range monthly {
		p := row.(map[string]any)
		income += parse(p["income"].(string))
		taxes += parse(p["tax"].(string))
		if p["type"] != float64(1) || p["special_deduction"] != "1020.00" {
			t.Fatal("monthly fields")
		}
	}
	if parse(a["totalIncome"].(string)) != income || parse(a["totalTax"].(string)) != taxes {
		t.Fatal("annual totals")
	}
	var wg sync.WaitGroup
	ids := make(chan uint64, 6)
	for range 6 {
		wg.Go(func() {
			rows, e := service.Ensure(t.Context(), "monthly", 82, 2024)
			if e != nil {
				t.Error(e)
				return
			}
			if len(rows) != 12 {
				t.Error("bad generated count")
			}
			ids <- rows[0].ID
		})
	}
	wg.Wait()
	close(ids)
	var prev uint64
	for id := range ids {
		if prev != 0 && prev != id {
			t.Fatal("concurrent re-generation")
		}
		prev = id
	}
	row := list("monthly", 2024).Items[0]
	p := fields(row)
	oldIncome := parse(p["income"].(string))
	oldTax := parse(p["tax"].(string))
	p["income"] = "30000.00"
	p["tax"] = "1200.00"
	p["additional_deduction"] = "1000.00"
	p["rate"] = "15%"
	req := taxbusiness.Write{UserID: 81, Year: 2024, Payload: p, ExpectedVersion: row.Version}
	path := fmt.Sprintf("/api/admin/tax-business/monthly/%d", row.ID)
	var edited TaxBusinessRecordResponse
	json.Unmarshal(adminCall("PUT", path, req, 200).Body.Bytes(), &edited)
	adminCall("PUT", path, req, 409)
	adminCall("GET", path, nil, 200)
	after := appCall(81, "taxYear/list", `{"year":2024}`, 200, 1).Data.(map[string]any)
	if parse(after["totalIncome"].(string)) != income-oldIncome+3000000 || parse(after["totalTax"].(string)) != taxes-oldTax+120000 {
		t.Fatal("annual edit not reflected")
	}
	calc := appCall(81, "taxCountInfo", `{"date":"2024-12"}`, 200, 1).Data.(map[string]any)
	if calc["nowShenTax"] != "1200.00" || calc["bqsbse"] != "1200.00" || calc["totalFuJiaMin"] != "1000.00" || calc["rate"] != "15%" || calc["detail_list"].([]any)[0].(map[string]any)["income"] != "30000.00" {
		t.Fatal("calculation inconsistent", calc)
	}
	adminCall("GET", "/api/admin/tax-business/calculation?userId=81&date=2024-12", nil, 200)
	appCall(81, "taxCountInfo", `{"date":"2024-99"}`, 200, 0)
	appCall(81, "taxCountInfo", `{"date":"2024-12","type":"不存在"}`, 200, 0)
	application := appCall(81, "applyInfo", `{"date":"2024"}`, 200, 1).Data.(map[string]any)
	if int64(math.Round(application["total_income"].(float64)*100)) != parse(after["totalIncome"].(string)) {
		t.Fatal("application totals")
	}
	applicationRow := list("applications", 2024).Items[0]
	adminCall("PUT", fmt.Sprintf("/api/admin/tax-business/applications/%d", applicationRow.ID), taxbusiness.Write{ExpectedVersion: applicationRow.Version, Payload: map[string]any{"refund_amount": "123.45", "status": "已申报（模拟）"}}, 200)
	application = appCall(81, "applyInfo", `{"date":2024}`, 200, 1).Data.(map[string]any)
	if application["refund_amount"] != 123.45 || application["status"] != "已申报（模拟）" {
		t.Fatal("application edit")
	}
	adminCall("POST", "/api/admin/tax-business/applications", taxbusiness.Write{UserID: 81, Year: 2024, Payload: map[string]any{"refund_amount": "0", "status": "未申报"}}, 409)
	bad := fields(edited.Data)
	bad["income"] = "-1"
	adminCall("PUT", path, taxbusiness.Write{ExpectedVersion: edited.Data.Version, Payload: bad}, 400)
	req.ExpectedVersion = edited.Data.Version
	req.Year = 2023
	adminCall("PUT", path, req, 400)
	req.Year = 2024
	req.ExpectedVersion = ""
	adminCall("POST", "/api/admin/tax-business/monthly", req, 409)
	opens := appCall(81, "openRecord/list", `{}`, 200, 1)
	if !reflect.DeepEqual(opens, appCall(81, "openRecord/list", `{}`, 200, 1)) {
		t.Fatal("unstable open records")
	}
	openList := opens.Data.([]any)
	if len(openList) != 2 {
		t.Fatal("open count")
	}
	for _, r := range openList {
		p := r.(map[string]any)
		if _, e := time.Parse("2006-01-02 15:04:05", p["create_time"].(string)); e != nil {
			t.Fatal(e)
		}
		if p["images_data"] != "" || p["yzm"] != nil {
			t.Fatal("image generated or code leaked")
		}
	}
	openRow := list("openrecords", 0).Items[0]
	openPayload := fields(openRow)
	openPayload["images_data"] = "https://example.com/simulated.png"
	adminCall("PUT", fmt.Sprintf("/api/admin/tax-business/openrecords/%d", openRow.ID), taxbusiness.Write{ExpectedVersion: openRow.Version, Payload: openPayload}, 200)
	scanBody := fmt.Sprintf(`{"yzm":"%s"}`, openPayload["yzm"])
	scan := appCall(81, "openRecord/info", scanBody, 200, 1)
	if scan.Data.(map[string]any)["images_data"] != openPayload["images_data"] {
		t.Fatal("scan mismatch")
	}
	appCall(82, "openRecord/info", scanBody, 200, 0)
	appCall(81, "openRecord/info", `{}`, 200, 0)
	// Message pagination returns a fixed total and never generates more pages.
	// Read count from the full wire envelope separately.
	token, _ := tokens.Issue(81)
	request := httptest.NewRequest("POST", "/v1/tax-user/taxMsg/list", strings.NewReader(`{"page":1,"limit":10}`))
	request.Header.Set("Authorization", "Bearer "+token)
	request.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	app.ServeHTTP(w, request)
	var messages taxbusiness.MessagesResponse
	if e := json.Unmarshal(w.Body.Bytes(), &messages); e != nil {
		t.Fatal(e)
	}
	if messages.Count != 15 || len(messages.Data) != 10 {
		t.Fatal("message count", w.Body.String())
	}
	if len(appCall(81, "taxMsg/list", `{"page":2,"limit":10}`, 200, 1).Data.([]any)) != 5 || len(appCall(81, "taxMsg/list", `{"page":3,"limit":10}`, 200, 1).Data.([]any)) != 0 {
		t.Fatal("message pagination")
	}
	appCall(81, "taxMsg/list", `{"page":0,"limit":10}`, 200, 0)
	// CRUD every record category with validation, stale deletion and audit retention.
	for _, kind := range []string{"monthly", "applications", "openrecords", "messages"} {
		rows := list(kind, 2024)
		if kind == "openrecords" || kind == "messages" {
			rows = list(kind, 0)
		}
		r := rows.Items[0]
		root := "/api/admin/tax-business/" + kind
		rowPath := fmt.Sprintf("%s/%d", root, r.ID)
		adminCall("DELETE", rowPath, taxbusiness.Write{ExpectedVersion: "stale"}, 409)
		adminCall("DELETE", rowPath, taxbusiness.Write{ExpectedVersion: r.Version}, 200)
		adminCall("GET", rowPath, nil, 404)
		payload := fields(r)
		var newRow TaxBusinessRecordResponse
		json.Unmarshal(adminCall("POST", root, taxbusiness.Write{UserID: 81, Year: r.Year, Payload: payload}, 200).Body.Bytes(), &newRow)
		if newRow.Data.Version == r.Version {
			t.Fatal("version reused")
		}
	}
	rows := list("monthly", 2024)
	adminCall("DELETE", fmt.Sprintf("/api/admin/tax-business/monthly/%d", rows.Items[0].ID), taxbusiness.Write{ExpectedVersion: rows.Items[0].Version}, 200)
	if len(appCall(81, "taxYear/list", `{"year":2024}`, 200, 1).Data.(map[string]any)["list"].([]any)) != 11 {
		t.Fatal("partial delete topped up")
	}
	messagesRows := list("messages", 0)
	for _, r := range messagesRows.Items {
		if _, e := service.Write(t.Context(), 1, "messages", r.ID, taxbusiness.Write{ExpectedVersion: r.Version}, "delete"); e != nil {
			t.Fatal(e)
		}
	}
	if len(appCall(81, "taxMsg/list", `{"page":1,"limit":100}`, 200, 1).Data.([]any)) != 15 {
		t.Fatal("empty messages not regenerated")
	}
	var audits int64
	db.Model(&taxbusiness.Audit{}).Where("admin_id IS NOT NULL").Count(&audits)
	if audits < 12 {
		t.Fatal("missing audit")
	}
	if e := db.Delete(&auth.User{}, 82).Error; e != nil {
		t.Fatal(e)
	}
	appCall(82, "taxYear/list", `{"year":2024}`, 401, 0)
	history, _ := service.List(t.Context(), "monthly", 82, 2024, "", 1, 100)
	if history.Total != 0 {
		t.Fatal("deleted member visible")
	}
	var retained int64
	db.Model(&taxbusiness.Record{}).Where("user_id=? AND kind='monthly'", 82).Count(&retained)
	if retained != 12 {
		t.Fatal("lost history")
	}
}
