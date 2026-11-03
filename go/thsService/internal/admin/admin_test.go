package admin

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"ths-service/internal/auth"
	"ths-service/internal/market"
	"ths-service/internal/portfolio"
)

func TestIdentityIsolation(t *testing.T) {
	h := NewHandler(nil, "test-secret", 3600)
	members := auth.NewTokenIssuer("test-secret", time.Hour)
	memberToken, _ := members.Issue(1)
	adminToken, _ := h.tokens.Issue(1)
	if _, err := members.Parse(adminToken); err == nil {
		t.Fatal("admin token accepted as member")
	}
	if _, err := h.tokens.Parse(memberToken); err == nil {
		t.Fatal("member token accepted as admin")
	}
	r := gin.New()
	r.GET("/me", h.RequireLogin, h.Me)
	for _, token := range []string{"", memberToken, "invalid"} {
		req := httptest.NewRequest("GET", "/me", nil)
		req.AddCookie(&http.Cookie{Name: cookieName, Value: token})
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		if w.Code != 401 {
			t.Fatalf("status %d", w.Code)
		}
	}
}

func TestAdminPagesAndOrigin(t *testing.T) {
	r := gin.New()
	Register(r, nil, "test-secret", 3600)
	for _, path := range []string{"/admin/nanwang.html", "/admin/js/nanwang.js", "/admin/tax-business.html", "/admin/js/tax-business.js", "/admin/alipay.html", "/admin/js/alipay.js", "/admin/js/alipay-batch.js", "/admin/deleted-members.html", "/admin/js/deleted-members.js", "/admin/pdf-assets.html", "/admin/js/pdf-assets.js", "/admin/pdf-reports.html", "/admin/js/pdf-reports.js"} {
		w := httptest.NewRecorder()
		r.ServeHTTP(w, httptest.NewRequest("GET", path, nil))
		if w.Code != 200 {
			t.Fatalf("%s: %d", path, w.Code)
		}
	}
	for _, path := range []string{"/admin/login.html", "/admin/index.html", "/admin/css/adminlte.css", "/admin/js/login.js", "/admin/electricity.html", "/admin/js/electricity.js", "/admin/bills.html", "/admin/js/bills.js", "/admin/tax-profile.html", "/admin/js/tax-profile.js", "/admin/employment.html", "/admin/js/employment.js"} {
		w := httptest.NewRecorder()
		r.ServeHTTP(w, httptest.NewRequest("GET", path, nil))
		if w.Code != 200 {
			t.Fatalf("%s: %d", path, w.Code)
		}
	}
	req := httptest.NewRequest("POST", "/api/admin/auth/login", strings.NewReader(`{}`))
	req.Header.Set("Origin", "https://evil.example")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)
	if w.Code != 403 {
		t.Fatalf("cross origin: %d", w.Code)
	}
}

// Set THS_ADMIN_TEST_DSN to a local MySQL account allowed to create isolated test databases.
func TestLoginIntegration(t *testing.T) {
	t.Setenv("THS_APK_STORAGE_DIR", t.TempDir())
	dsn := os.Getenv("THS_ADMIN_TEST_DSN")
	if dsn == "" {
		t.Skip("THS_ADMIN_TEST_DSN is not set")
	}
	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatal(err)
	}
	sqlDB, _ := db.DB()
	defer sqlDB.Close()
	name := fmt.Sprintf("ths_admin_test_%d", time.Now().UnixNano())
	if err := db.Exec("CREATE DATABASE " + name).Error; err != nil {
		t.Fatal(err)
	}
	defer db.Exec("DROP DATABASE " + name)
	// A single dedicated connection keeps USE scoped to the isolated database.
	sqlDB.SetMaxOpenConns(1)
	if err := db.Exec("USE " + name).Error; err != nil {
		t.Fatal(err)
	}
	for _, file := range []string{"000001_create_users", "000004_create_demo_portfolio", "000007_category_demo_holdings", "000008_create_administrators", "000009_admin_holding_audits", "000010_member_soft_delete", "000011_admin_login_limits", "000012_electricity_profiles", "000013_electricity_bills", "000014_tax_profiles", "000015_tax_employments", "000016_nanwang_business", "000017_tax_business", "000018_alipay", "000019_seed_asen_admin", "000022_pdf_assets", "000023_pdf_export_audits", "000024_apk_download", "000025_apk_defaults", "000026_tax_bank_type"} {
		migration, err := os.ReadFile("../../migrations/" + file + ".up.sql")
		if err != nil {
			t.Fatal(err)
		}
		for _, statement := range strings.Split(string(migration), ";") {
			if strings.TrimSpace(statement) != "" {
				if err := db.Exec(statement).Error; err != nil {
					t.Fatal(err)
				}
			}
		}
	}
	r := gin.New()
	Register(r, db, "test-secret", 3600)
	portfolioService := portfolio.NewService(portfolio.NewRepository(db), adminTestQuotes{})
	RegisterHoldings(r, NewHandler(db, "test-secret", 3600), portfolioService)
	call := func(method, path, body string, cookie *http.Cookie) *httptest.ResponseRecorder {
		req := httptest.NewRequest(method, path, strings.NewReader(body))
		req.Header.Set("Content-Type", "application/json")
		if cookie != nil {
			req.AddCookie(cookie)
		}
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		return w
	}
	for _, body := range []string{`{"username":"unknown","password":"incorrect"}`, `{"username":"eugene","password":"incorrect"}`} {
		if w := call("POST", "/api/admin/auth/login", body, nil); w.Code != 401 {
			t.Fatalf("bad login: %d %s", w.Code, w.Body.String())
		}
	}
	password := os.Getenv("THS_ADMIN_TEST_PASSWORD")
	if password == "" {
		t.Fatal("THS_ADMIN_TEST_PASSWORD is required")
	}
	body, _ := json.Marshal(LoginRequest{Username: "eugene", Password: password})
	w := call("POST", "/api/admin/auth/login", string(body), nil)
	if w.Code != 200 {
		t.Fatalf("login: %d %s", w.Code, w.Body.String())
	}
	cookies := w.Result().Cookies()
	if len(cookies) != 1 || !cookies[0].HttpOnly || cookies[0].SameSite != http.SameSiteStrictMode || cookies[0].Path != "/api/admin" {
		t.Fatal("invalid session cookie")
	}
	var result struct {
		Data LoginData `json:"data"`
	}
	if err := json.Unmarshal(w.Body.Bytes(), &result); err != nil {
		t.Fatal(err)
	}
	if !result.Data.Admin.IsSuperAdmin || strings.Contains(w.Body.String(), "password") {
		t.Fatal("invalid admin response")
	}
	cookie := cookies[0]
	// The additive seed is safe to reapply and does not replace existing credentials.
	seed, err := os.ReadFile("../../migrations/000019_seed_asen_admin.up.sql")
	if err != nil {
		t.Fatal(err)
	}
	if err = db.Exec(string(seed)).Error; err != nil {
		t.Fatal(err)
	}
	var asen Administrator
	if err = db.Where("username = ?", "Asen").First(&asen).Error; err != nil {
		t.Fatal(err)
	}
	var asenCount int64
	db.Model(&Administrator{}).Where("username = ?", "Asen").Count(&asenCount)
	if asenCount != 1 || asen.Status != 1 || asen.IsSuperAdmin {
		t.Fatal("invalid seeded administrator")
	}
	if asenPassword := os.Getenv("THS_ASEN_TEST_PASSWORD"); asenPassword != "" {
		body, _ := json.Marshal(LoginRequest{Username: "Asen", Password: asenPassword})
		login := call("POST", "/api/admin/auth/login", string(body), nil)
		if login.Code != 200 || len(login.Result().Cookies()) != 1 {
			t.Fatalf("Asen login failed: %d %s", login.Code, login.Body.String())
		}
		if me := call("GET", "/api/admin/auth/me", "", login.Result().Cookies()[0]); me.Code != 200 || !strings.Contains(me.Body.String(), `"username":"Asen"`) {
			t.Fatal("Asen session failed")
		}
	}
	testHoldingsCRUD(t, db, portfolioService, call, cookie)
	testMembersCRUD(t, db, call, cookie)
	testElectricityCRUD(t, db, call, cookie)
	testBillsCRUD(t, db, call, cookie)
	testTaxCRUD(t, db, call, cookie)
	testEmploymentCRUD(t, db, call, cookie)
	testNanwangCRUD(t, db, call, cookie)
	testTaxBusinessCRUD(t, db, call, cookie)
	testAlipayCRUD(t, db, call, cookie)
	testAlipayBatch(t, db, call, cookie)
	testManagementCodeMigration(t, db)
	testPDFAssets(t, db, r, cookie)
	testPDFReports(t, db, r, cookie)
	testAPKDownload(t, db, r, cookie)
	testDisplayCleanupSQL(t, db)
	testLoginGuardIntegration(t, db)
	if w := call("GET", "/api/admin/auth/me", "", cookie); w.Code != 200 {
		t.Fatalf("me: %d", w.Code)
	}
	w = call("POST", "/api/admin/auth/logout", "", cookie)
	if w.Code != 200 || w.Result().Cookies()[0].MaxAge != -1 {
		t.Fatal("logout did not clear session")
	}
	if w := call("GET", "/api/admin/auth/me", "", nil); w.Code != 401 {
		t.Fatal("anonymous me accepted")
	}
	db.Model(&Administrator{}).Where("username = ?", "eugene").Update("status", 2)
	if w := call("GET", "/api/admin/auth/me", "", cookie); w.Code != 401 {
		t.Fatal("disabled admin accepted")
	}
}

type adminTestQuotes struct{}

func (adminTestQuotes) List(_ context.Context, category string) (market.QuoteListData, error) {
	q := market.QuoteListData{Category: category, CategoryName: "A股"}
	for i := range 5 {
		q.Items = append(q.Items, market.QuoteItem{Symbol: fmt.Sprintf("60000%d.SH", i), Name: fmt.Sprintf("股票%d", i), Currency: "CNY", LatestPrice: "10.0000", ChangeAmount: "0.1000", QuoteTime: time.Now()})
	}
	return q, nil
}
func testHoldingsCRUD(t *testing.T, db *gorm.DB, s *portfolio.Service, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if err := db.Exec("INSERT INTO users (id, email, real_name, password_hash) VALUES (1, 'admin-test@example.com', '持仓测试会员', 'unused')").Error; err != nil {
		t.Fatal(err)
	}
	root := "/api/admin/holdings/1/a_stock"
	if w := call("GET", "/api/admin/holdings", "", nil); w.Code != 401 {
		t.Fatal("holdings accepted anonymous")
	}
	if w := call("GET", root, "", cookie); w.Code != 404 {
		t.Fatal("missing get generated portfolio")
	}
	req := portfolio.AdminHoldingWrite{TotalAssets: "200000.00", Items: []portfolio.HoldingItem{{Position: portfolio.Position{Symbol: "600001.SH", Name: "后台测试", Currency: "CNY", Quantity: 100, AvailableQuantity: 100, LatestPrice: "12", DailyProfit: "0.00"}, Trades: []portfolio.Trade{{Side: "buy", Quantity: 100, Price: "10", Fees: "0.00", TradedAt: time.Now().Add(-time.Hour)}}}}}
	send := func(method, url string, req portfolio.AdminHoldingWrite, status int) portfolio.HoldingsData {
		body, _ := json.Marshal(req)
		w := call(method, url, string(body), cookie)
		if w.Code != status {
			t.Fatalf("%s %s: %d %s", method, url, w.Code, w.Body.String())
		}
		var envelope portfolio.HoldingsResponse
		json.Unmarshal(w.Body.Bytes(), &envelope)
		return envelope.Data
	}
	data := send("POST", root, req, 200)
	send("POST", root, req, 409)
	if w := call("GET", "/api/admin/holdings?page=1&category=a_stock&userId=1", "", cookie); w.Code != 200 || !strings.Contains(w.Body.String(), `"total":1`) || !strings.Contains(w.Body.String(), `"positionCount":1`) {
		t.Fatalf("list: %s", w.Body.String())
	}
	if w := call("GET", "/api/admin/holding-members?q=持仓测试会员", "", cookie); w.Code != 200 || !strings.Contains(w.Body.String(), "admin-test@example.com") {
		t.Fatal("member lookup")
	}
	req.ExpectedGeneration = data.Account.Generation
	req.Items[0].Position.Name = "修改股票"
	req.Items[0].Trades[0].Price = "11.0000"
	updated := send("PUT", root, req, 200)
	send("PUT", root, req, 409)
	app, err := s.Holdings(context.Background(), 1, "a_stock", nil)
	if err != nil || app.Items[0].Position.Name != "修改股票" || app.Items[0].Position.CostPrice != "11.0000" {
		t.Fatalf("App did not see admin update: %+v %v", app, err)
	}
	req.ExpectedGeneration = updated.Account.Generation
	req.Items[0].Trades[0].Quantity = 200
	send("PUT", root, req, 400)
	after, _ := s.AdminGet(context.Background(), 1, "a_stock")
	if after.Account.Generation != updated.Account.Generation {
		t.Fatal("invalid save changed data")
	}
	req.Items = []portfolio.HoldingItem{}
	empty := send("PUT", root, req, 200)
	app, err = s.Holdings(context.Background(), 1, "a_stock", nil)
	if err != nil || len(app.Items) != 0 {
		t.Fatal("empty portfolio regenerated")
	}
	req.ExpectedGeneration = empty.Account.Generation
	generated := send("POST", root+"/generate", req, 200)
	if len(generated.Items) < 8 || len(generated.Items) > 12 || len(generated.Items[0].Trades) < 40 || len(generated.Items[0].Trades) > 50 {
		t.Fatal("generation counts")
	}
	// Generated portfolios remain editable under manual normalization rules.
	req.ExpectedGeneration = generated.Account.Generation
	req.Items = generated.Items
	saved := send("PUT", root, req, 200)
	req.ExpectedGeneration = generated.Account.Generation
	send("DELETE", root, req, 409)
	req.ExpectedGeneration = saved.Account.Generation
	send("DELETE", root, req, 200)
	if w := call("GET", root, "", cookie); w.Code != 404 {
		t.Fatal("delete failed")
	}
	var count int64
	db.Model(&portfolio.HoldingAudit{}).Count(&count)
	if count != 6 {
		t.Fatalf("expected6 successful mutation audits, got %d", count)
	}
	// Exercise the actual App HTTP entry and backend generation against the same
	// snapshot, using a quote source with only five names to verify pool expansion.
	appRouter := gin.New()
	issuer := auth.NewTokenIssuer("holdings-app-test", time.Hour)
	appRouter.GET("/api/app/market/holdings", auth.RequireAccessToken(issuer), auth.RequireActiveMember(db), portfolio.NewHandler(s).Holdings)
	getApp := func(query string, status int) portfolio.HoldingsData {
		t.Helper()
		request := httptest.NewRequest("GET", "/api/app/market/holdings?"+query, nil)
		token, e := issuer.Issue(1)
		if e != nil {
			t.Fatal(e)
		}
		request.Header.Set("Authorization", "Bearer "+token)
		response := httptest.NewRecorder()
		appRouter.ServeHTTP(response, request)
		if response.Code != status {
			t.Fatalf("App holdings HTTP%d want%d: %s", response.Code, status, response.Body.String())
		}
		var envelope portfolio.HoldingsResponse
		if e := json.Unmarshal(response.Body.Bytes(), &envelope); e != nil {
			t.Fatal(e)
		}
		return envelope.Data
	}
	assertExpanded := func(data portfolio.HoldingsData) {
		t.Helper()
		if len(data.Items) < 8 || len(data.Items) > 12 {
			t.Fatalf("holdings count %d", len(data.Items))
		}
		for _, item := range data.Items {
			if len(item.Trades) < 40 || len(item.Trades) > 50 {
				t.Fatalf("trade count %d", len(item.Trades))
			}
		}
	}
	assertSame := func(a, b portfolio.HoldingsData) {
		t.Helper()
		left, _ := json.Marshal(a)
		right, _ := json.Marshal(b)
		if string(left) != string(right) {
			t.Fatal("App/admin snapshots differ")
		}
	}
	appGenerated := getApp("category=a_stock&totalAssets=200000", 200)
	assertExpanded(appGenerated)
	backendRead, e := s.AdminGet(t.Context(), 1, "a_stock")
	if e != nil {
		t.Fatal(e)
	}
	assertSame(appGenerated, backendRead)
	assertSame(appGenerated, getApp("category=a_stock", 200))
	otherCategory := getApp("category=hk_stock", 200)
	assertExpanded(otherCategory)
	backendGenerated := send("POST", root+"/generate", portfolio.AdminHoldingWrite{ExpectedGeneration: appGenerated.Account.Generation, TotalAssets: "500000"}, 200)
	assertExpanded(backendGenerated)
	assertSame(backendGenerated, getApp("category=a_stock", 200))
	assertSame(otherCategory, getApp("category=hk_stock", 200))
	getApp("category=a_stock&totalAssets=1", 400)
	assertSame(backendGenerated, getApp("category=a_stock", 200))
}
