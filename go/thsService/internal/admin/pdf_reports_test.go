package admin

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
	"ths-service/internal/pdfreport"
)

func testPDFReports(t *testing.T, db *gorm.DB, r *gin.Engine, cookie *http.Cookie) {
	t.Helper()
	if e := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES(95,'13900000095','报表会员','unchanged',1)").Error; e != nil {
		t.Fatal(e)
	}
	bill := electricity.Bill{UserID: 95, Code: "202609", StartDate: "2026-09-01", EndDate: "2026-09-30", Amount: "62.00", TotalUsage: "100.00", Detail: `{"unitPrice":"0.6200"}`, Version: "retained"}
	if e := db.Create(&bill).Error; e != nil {
		t.Fatal(e)
	}
	db.First(&bill, bill.ID)
	call := func(method, path string, body any, c *http.Cookie, origin string) *httptest.ResponseRecorder {
		t.Helper()
		b, _ := json.Marshal(body)
		req := httptest.NewRequest(method, "/api/admin/pdf-reports"+path, bytes.NewReader(b))
		req.Header.Set("Content-Type", "application/json")
		if c != nil {
			req.AddCookie(c)
		}
		if origin != "" {
			req.Header.Set("Origin", origin)
		}
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		return w
	}
	check := func(w *httptest.ResponseRecorder, status int) {
		t.Helper()
		if w.Code != status {
			t.Fatalf("PDF got%d want%d %s", w.Code, status, w.Body.String())
		}
	}
	prepare := func(req PDFPrepareRequest) pdfreport.Draft {
		t.Helper()
		w := call("POST", "/prepare", req, cookie, "")
		check(w, 200)
		var out PDFDraftResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		return out.Data
	}
	check(call("GET", "/schemas", nil, nil, ""), 401)
	check(call("POST", "/prepare", PDFPrepareRequest{Kind: "work", UserID: 95}, cookie, "https://evil.example"), 403)
	check(call("POST", "/prepare", PDFPrepareRequest{Kind: "electricity", UserID: 95}, cookie, ""), 400)
	check(call("POST", "/prepare", PDFPrepareRequest{Kind: "electricity", UserID: 93, BillID: bill.ID}, cookie, ""), 404)
	check(call("POST", "/prepare", PDFPrepareRequest{Kind: "social", UserID: 95, StartMonth: "2021-01", EndMonth: "2025-12"}, cookie, ""), 404)
	work := prepare(PDFPrepareRequest{Kind: "work", UserID: 95})
	work.Fields["monthlyIncome"] = "20000"
	work.Fields["companyName"] = "本次编辑单位"
	work.Fields["name"] = "本次编辑姓名"
	electric := prepare(PDFPrepareRequest{Kind: "electricity", UserID: 95, BillID: bill.ID})
	electric.Tables["fees"][0]["price"] = "0.65"
	social := prepare(PDFPrepareRequest{Kind: "social", UserID: 93, StartMonth: "2021-01", EndMonth: "2025-12"})
	if len(social.Tables["payments"]) != 60 || len(social.Tables["details"]) != 0 {
		t.Fatal("social preparation fabricated details")
	}
	var count int64
	db.Model(&PDFExportAudit{}).Count(&count)
	before := count
	for _, draft := range []pdfreport.Draft{work, electric, social} {
		check(call("POST", "/download", draft, nil, ""), 401)
		w := call("POST", "/download", draft, cookie, "")
		check(w, 200)
		if !bytes.HasPrefix(w.Body.Bytes(), []byte("%PDF-")) || w.Header().Get("Content-Type") != "application/pdf" || !strings.Contains(w.Header().Get("Content-Disposition"), "attachment;") {
			t.Fatal("not direct PDF attachment")
		}
	}
	db.Model(&PDFExportAudit{}).Count(&count)
	if count != before+3 {
		t.Fatal("missing export audit")
	}
	var a PDFExportAudit
	if e := db.Where("user_id=95 AND kind='electricity'").Order("id DESC").First(&a).Error; e != nil || a.PDFSha256 == "" {
		t.Fatal("audit", a, e)
	}
	var versions map[int]uint64
	if e := json.Unmarshal([]byte(a.AssetVersions), &versions); e != nil || versions[3] == 0 || versions[4] != 0 || versions[7] != 0 {
		t.Fatal("pending asset exported or approved asset missing", a.AssetVersions, e)
	}
	var u auth.User
	db.First(&u, 95)
	if u.RealName != "报表会员" || u.PasswordHash != "unchanged" {
		t.Fatal("PDF changed user")
	}
	var saved electricity.Bill
	db.First(&saved, bill.ID)
	if saved.Version != "retained" || saved.Amount != "62.00" || saved.Detail != bill.Detail {
		t.Fatal("PDF changed bill", saved.Detail)
	}
	for _, table := range []string{"tax_profiles", "tax_employments", "alipay_records", "electricity_profiles"} {
		db.Table(table).Where("user_id=95").Count(&count)
		if count != 0 {
			t.Fatal("prepare created business data", table)
		}
	}
	work.Fields["unknown"] = "x"
	check(call("POST", "/download", work, cookie, ""), 400)
	delete(work.Fields, "unknown")
	db.Delete(&u)
	check(call("POST", "/download", work, cookie, ""), 404)
	w := call("GET", fmt.Sprintf("/bills?userId=%d", 95), nil, cookie, "")
	check(w, 404)
}
