package admin

import (
	"encoding/json"
	"fmt"
	"math/big"
	"net/http"
	"net/http/httptest"
	"reflect"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
)

func testBillsCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if err := db.Exec("INSERT INTO users (id,phone,real_name,password_hash,status) VALUES (41,'13900000041','账单会员甲','unused',1),(42,'13900000042','账单会员乙','unused',1),(43,'13900000043','账单并发测试','unused',1)").Error; err != nil {
		t.Fatal(err)
	}
	service := electricity.NewService(db)
	tokens := auth.NewTokenIssuer("bill-test", time.Hour)
	app := gin.New()
	app.GET("/api/app/electricity/bills", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db), electricity.NewHandler(service).MyBills)
	appCall := func(uid uint64, query string, status int) []electricity.BillView {
		req := httptest.NewRequest("GET", "/api/app/electricity/bills"+query, nil)
		token, _ := tokens.Issue(uid)
		if uid != 0 {
			req.Header.Set("Authorization", "Bearer "+token)
		}
		w := httptest.NewRecorder()
		app.ServeHTTP(w, req)
		if w.Code != status {
			t.Fatalf("App bills HTTP%d want%d %s", w.Code, status, w.Body.String())
		}
		if status != 200 {
			return nil
		}
		var data electricity.AppBillsResponse
		if err := json.Unmarshal(w.Body.Bytes(), &data); err != nil {
			t.Fatal(err)
		}
		stored, err := service.MyBills(t.Context(), uid)
		if err != nil {
			t.Fatal(err)
		}
		if data.Msg != "success" || len(data.Data) != len(stored) {
			t.Fatal("bill response contract", w.Body.String())
		}
		for i, b := range data.Data {
			if b.Code != stored[i].Code || b.BillMonth != b.Code || b.Amount.String() != stored[i].Amount || b.TotalUsage.String() != stored[i].TotalUsage || b.StartDate != stored[i].StartDate || b.EndDate != stored[i].EndDate {
				t.Fatal("bill field mismatch", b)
			}
			if !reflect.DeepEqual(b.Billing, stored[i].Billing) {
				t.Fatalf("App billing differs from database/admin: %+v vs %+v", b.Billing, stored[i].Billing)
			}
			var appDetail, storedDetail any
			if json.Unmarshal(b.Detail, &appDetail) != nil || json.Unmarshal(stored[i].Detail, &storedDetail) != nil || !reflect.DeepEqual(appDetail, storedDetail) {
				t.Fatal("App detail differs from saved detail")
			}
		}
		var raw struct {
			Data []map[string]any `json:"data"`
		}
		if err := json.Unmarshal(w.Body.Bytes(), &raw); err != nil {
			t.Fatal(err)
		}
		for _, b := range raw.Data {
			if _, ok := b["amount"].(float64); !ok {
				t.Fatal("amount is not JSON number")
			}
			if _, ok := b["total_usage"].(float64); !ok {
				t.Fatal("usage is not JSON number")
			}
		}
		return stored
	}
	appCall(0, "", 401)
	first := appCall(41, "", 200)
	again := appCall(41, "?userId=42", 200)
	if len(first) != 20 || !reflect.DeepEqual(first, again) {
		t.Fatal("bills not stable or not 20")
	}
	other := appCall(42, "?userId=41", 200)
	if len(other) != 20 || other[0].UserID != 42 {
		t.Fatal("cross-member bills")
	}
	var wg sync.WaitGroup
	ids := make(chan uint64, 6)
	for range 6 {
		wg.Go(func() {
			data, err := service.MyBills(t.Context(), 43)
			if err != nil {
				t.Error(err)
				return
			}
			if len(data) != 20 {
				t.Error("concurrent generation count")
			}
			ids <- data[0].ID
		})
	}
	wg.Wait()
	close(ids)
	var previous uint64
	for id := range ids {
		if previous != 0 && id != previous {
			t.Fatal("multiple generations")
		}
		previous = id
	}
	root := "/api/admin/electricity/bills/41"
	send := func(method, path string, body any, status int) *httptest.ResponseRecorder {
		encoded, _ := json.Marshal(body)
		w := call(method, path, string(encoded), cookie)
		if w.Code != status {
			t.Fatalf("%s %s HTTP%d want%d %s", method, path, w.Code, status, w.Body.String())
		}
		return w
	}
	list := func() electricity.BillList {
		w := send("GET", "/api/admin/electricity/bills?userId=41&pageSize=7", nil, 200)
		var data electricity.BillListResponse
		json.Unmarshal(w.Body.Bytes(), &data)
		return data.Data
	}
	if w := call("GET", "/api/admin/electricity/bills", "", nil); w.Code != 401 {
		t.Fatal("anonymous bills access")
	}
	listed := list()
	if listed.Total != 20 || len(listed.Items) != 7 || listed.SetVersion == "" || listed.Items[0].Amount == "" || len(listed.Items[0].Detail) == 0 {
		t.Fatal("admin bill list mapping", listed)
	}
	send("GET", fmt.Sprintf("/api/admin/electricity/bills/42/%d", first[0].ID), nil, 404)
	req := electricity.BillWrite{Code: "200001", StartDate: "2000-01-01", EndDate: "2000-01-31", Amount: "100.01", TotalUsage: "200.02", Detail: json.RawMessage(`{"note":"后台录入"}`)}
	var one electricity.BillResponse
	json.Unmarshal(send("POST", root, req, 200).Body.Bytes(), &one)
	created := one.Data
	rowPath := fmt.Sprintf("%s/%d", root, created.ID)
	send("POST", root, req, 409)
	req.ExpectedVersion = created.Version
	req.Amount = "199.99"
	json.Unmarshal(send("PUT", rowPath, req, 200).Body.Bytes(), &one)
	updated := one.Data
	if updated.Amount != "199.99" || len(appCall(41, "", 200)) != 21 {
		t.Fatal("bill edit not synchronized")
	}
	send("PUT", rowPath, req, 409)
	req.ExpectedVersion = updated.Version
	req.StartDate = "2000-02-01"
	send("PUT", rowPath, req, 400)
	send("DELETE", rowPath, electricity.BillWrite{ExpectedVersion: updated.Version}, 200)
	firstPath := fmt.Sprintf("%s/%d", root, first[0].ID)
	send("DELETE", firstPath, electricity.BillWrite{ExpectedVersion: first[0].Version}, 200)
	if len(appCall(41, "", 200)) != 19 {
		t.Fatal("partial delete auto-topped-up")
	}
	send("POST", root+"/generate", electricity.BillWrite{}, 409)
	set := list().SetVersion
	send("POST", root+"/generate", electricity.BillWrite{ExpectedVersion: set}, 200)
	send("POST", root+"/generate", electricity.BillWrite{ExpectedVersion: set}, 409)
	regenerated := appCall(41, "", 200)
	if len(regenerated) != 20 || regenerated[0].ID == first[0].ID {
		t.Fatal("regeneration failed")
	}
	if !reflect.DeepEqual(other, appCall(42, "", 200)) {
		t.Fatal("regeneration changed other member")
	}
	// Removing every record causes exactly one new set at next read.
	for _, b := range regenerated {
		send("DELETE", fmt.Sprintf("%s/%d", root, b.ID), electricity.BillWrite{ExpectedVersion: b.Version}, 200)
	}
	if list().Total != 0 {
		t.Fatal("admin query regenerated bills")
	}
	if got := appCall(41, "", 200); len(got) != 20 || got[0].ID == regenerated[0].ID {
		t.Fatal("empty bills did not regenerate")
	}
	if err := db.Delete(&auth.User{}, 42).Error; err != nil {
		t.Fatal(err)
	}
	appCall(42, "", 401)
	send("POST", "/api/admin/electricity/bills/42/generate", electricity.BillWrite{}, 404)
	var count int64
	db.Model(&electricity.Bill{}).Where("user_id = ?", 42).Count(&count)
	if count != 20 {
		t.Fatal("logical member deletion removed bills")
	}
	// Audit includes JSON details rather than omitting the detail column.
	var audit electricity.BillAudit
	db.Where("user_id = ? AND action = ?", 41, "create").First(&audit)
	if audit.AfterPayload == nil {
		t.Fatal("missing audit")
	}
	var recorded map[string]any
	json.Unmarshal([]byte(*audit.AfterPayload), &recorded)
	if _, ok := recorded["detail"]; !ok {
		t.Fatal("audit omitted detail")
	}
	// Structured edits survive storage and are returned unchanged to the App.
	stored := list().Items[0]
	billing := stored.Billing
	billing.DueDate = "2026-11-24"
	billing.LateFeeStartDate = "2026-11-25"
	billing.LateFeeAsOfDate = "2026-11-26"
	billing.PaymentStatus = "overdue"
	billing.ArrearsPrincipal = stored.Amount
	billing.LateFee = "1.23"
	for i := range billing.Tiers {
		billing.Tiers[i].Usage = fmt.Sprintf("%d.11", i+1)
		billing.Tiers[i].UnitPrice = fmt.Sprintf("0.%d1234567", i+1)
		billing.Tiers[i].Amount = fmt.Sprintf("%d.22", i+10)
	}
	base, _ := new(big.Rat).SetString(stored.Amount)
	billing.PayableAmount = new(big.Rat).Add(base, big.NewRat(123, 100)).FloatString(2)
	detail, _ := json.Marshal(map[string]any{"billing": billing, "note": "保留扩展字段"})
	edit := electricity.BillWrite{Code: stored.Code, StartDate: stored.StartDate, EndDate: stored.EndDate, Amount: stored.Amount, TotalUsage: stored.TotalUsage, Detail: detail, ExpectedVersion: stored.Version}
	path := fmt.Sprintf("%s/%d", root, stored.ID)
	send("PUT", path, edit, 200)
	got := appCall(41, "", 200)[0]
	if !reflect.DeepEqual(got.Billing, billing) {
		t.Fatalf("billing edit lost: %+v", got.Billing)
	}
	var adminRead electricity.BillResponse
	if err := json.Unmarshal(send("GET", path, nil, 200).Body.Bytes(), &adminRead); err != nil {
		t.Fatal(err)
	}
	if !reflect.DeepEqual(adminRead.Data.Billing, billing) || !reflect.DeepEqual(list().Items[0].Billing, billing) {
		t.Fatal("admin detail/list billing differs from App")
	}
	bad := billing
	bad.Tiers = append([]electricity.TariffTier(nil), billing.Tiers...)
	bad.Tiers[0].Amount = "0.01"
	edit.Detail, _ = json.Marshal(map[string]any{"billing": bad})
	edit.ExpectedVersion = got.Version
	send("PUT", path, edit, 200)
	if saved := appCall(41, "", 200)[0]; saved.Billing.Tiers[0].Amount != "0.01" {
		t.Fatal("manual tier amount was overwritten")
	}
}
