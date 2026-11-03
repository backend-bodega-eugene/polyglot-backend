package admin

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"gorm.io/gorm"
	"ths-service/internal/alipay"
)

func testAlipayBatch(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if e := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES(93,'13900000093','社保批量','unused',1)").Error; e != nil {
		t.Fatal(e)
	}
	req := alipay.GenerateContributions{UserID: 93, StartMonth: "2021-01", EndMonth: "2025-12", Salary: "20000", Company: "深圳市科技有限公司", Rules: []alipay.ContributionRule{
		{Kind: "pension", CompanyRate: "16", PersonalRate: "8"}, {Kind: "medical", CompanyRate: "5", PersonalRate: "2"}, {Kind: "unemployment", CompanyRate: "0.8", PersonalRate: "0.2"}, {Kind: "injury", CompanyRate: "0.2", PersonalRate: "0"}, {Kind: "maternity", CompanyRate: "0.5", PersonalRate: "0"},
	}}
	invoke := func(path string, c *http.Cookie, status int) alipay.ContributionPreview {
		t.Helper()
		b, _ := json.Marshal(req)
		w := call("POST", "/api/admin/alipay/records/"+path, string(b), c)
		if w.Code != status {
			t.Fatalf("batch %s: %d %s", path, w.Code, w.Body.String())
		}
		var body AlipayGenerationResponse
		if e := json.Unmarshal(w.Body.Bytes(), &body); e != nil {
			t.Fatal(e)
		}
		return body.Data
	}
	invoke("generate", nil, 401)
	p := invoke("generate-preview", cookie, 200)
	if p.Created != 60 || p.Skipped != 0 || p.Total != "6540.00" {
		t.Fatalf("preview %+v", p)
	}
	var count int64
	db.Model(&alipay.Record{}).Where("user_id=93").Count(&count)
	if count != 0 {
		t.Fatal("preview wrote records")
	}
	p = invoke("generate", cookie, 200)
	if p.Created != 60 {
		t.Fatalf("generated %+v", p)
	}
	// A second request with changed company and salary still preserves existing months.
	req.Company = "另一单位"
	req.Salary = "10000"
	p = invoke("generate", cookie, 200)
	if p.Created != 0 || p.Skipped != 60 {
		t.Fatalf("retry %+v", p)
	}
	service := alipay.NewService(db)
	rows, e := service.List(t.Context(), "records", 93, 2025, "", 1, 100)
	if e != nil || rows.Total != 12 {
		t.Fatalf("list %+v %v", rows, e)
	}
	var payload map[string]any
	json.Unmarshal(rows.Items[0].Payload, &payload)
	if payload["company"] != "深圳市科技有限公司" || payload["companyPay"] != "4500.00" {
		t.Fatal("existing record overwritten")
	}
	summary, e := service.Summary(t.Context(), 93)
	if e != nil {
		t.Fatal(e)
	}
	b, _ := json.Marshal(summary)
	var data map[string]any
	json.Unmarshal(b, &data)
	if data["monthCount"] != "60" || data["personalAmount"] != "122400.00" || data["companyAmount"] != "270000.00" {
		t.Fatalf("summary %s", b)
	}
	db.Model(&alipay.Audit{}).Where("user_id=93 AND action='admin_generate'").Count(&count)
	if count != 60 {
		t.Fatal("missing batch audits", count)
	}
	req.UserID = 999999
	invoke("generate", cookie, 404)
	req.UserID = 93
	req.EndMonth = "2099-01"
	invoke("generate", cookie, 400)
}
