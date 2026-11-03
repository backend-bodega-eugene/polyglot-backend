package admin

import (
	"encoding/json"
	"os"
	"strings"
	"testing"

	"gorm.io/gorm"
	"ths-service/internal/alipay"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
	"ths-service/internal/nanwang"
	"ths-service/internal/tax"
	"ths-service/internal/taxbusiness"
)

func testDisplayCleanupSQL(t *testing.T, db *gorm.DB) {
	t.Helper()
	phone := "13900000101"
	if e := db.Create(&auth.User{ID: 101, Phone: &phone, RealName: "张三", PasswordHash: "unused", Status: 1}).Error; e != nil {
		t.Fatal(e)
	}
	p := electricity.Profile{UserID: 101, Fields: electricity.Fields{DisplayName: "张三（模拟）", Address: "广东省广州市天河区模拟路18号", Supplier: "供电局（测试）", MeterReader: "模拟抄表员", UserIDNumber: "00123456789", MeterNo: "00012345", PostalCode: "510000"}, Version: "before-clean"}
	if e := db.Create(&p).Error; e != nil {
		t.Fatal(e)
	}
	taxp := tax.Profile{UserID: 101, Fields: tax.Fields{Name: "张三（测试）", CardType: "模拟证件", IDCard: "SIM-ID-preserved", TaxNumber: "SIM-TAX-preserved", BankNum: "SIM-BANK-preserved", HujiAddress: "天河区模拟路18号"}, Refund2019: "0.00", Refund2020: "0.00", Refund2021: "0.00", Refund2022: "0.00", Refund2023: "0.00", Version: "before-clean"}
	if e := db.Create(&taxp).Error; e != nil {
		t.Fatal(e)
	}
	company := tax.Employment{UserID: 101, EmploymentFields: tax.EmploymentFields{CompanyName: "广州模拟信息科技有限公司", TaxID: "SIM-EMP-preserved", CompanyNum: "SIM-COMPANY-preserved", Post: "职员（模拟）", StartDate: "2025-01-01"}, Version: "before-clean"}
	if e := db.Create(&company).Error; e != nil {
		t.Fatal(e)
	}
	bill := electricity.Bill{UserID: 101, Code: "202501", StartDate: "2025-01-01", EndDate: "2025-01-31", Amount: "12.30", TotalUsage: "20.00", Detail: `{"description":"模拟居民用电账单，仅供展示","unitPrice":"0.6200","note":false}`, Version: "before-clean"}
	if e := db.Create(&bill).Error; e != nil {
		t.Fatal(e)
	}
	if e := db.Create(&nanwang.Scope{Kind: "messages", OwnerID: 101}).Error; e != nil {
		t.Fatal(e)
	}
	nr := nanwang.Record{Kind: "messages", OwnerID: 101, Payload: json.RawMessage(`{"title":"模拟电费账单已出","content":"欢迎使用模拟用电服务","count":2,"hasPile":false,"id":"SIM-preserved"}`), Version: "before-clean"}
	if e := db.Create(&nr).Error; e != nil {
		t.Fatal(e)
	}
	if e := db.Create(&taxbusiness.Scope{Kind: "messages", UserID: 101}).Error; e != nil {
		t.Fatal(e)
	}
	tr := taxbusiness.Record{Kind: "messages", UserID: 101, Payload: json.RawMessage(`{"title":"税务服务消息（仅供展示）","content":"此消息仅为模拟展示，不代表真实税务通知。","yzm":"SIM-code-preserved","tax":"12.30"}`), Version: "before-clean"}
	if e := db.Create(&tr).Error; e != nil {
		t.Fatal(e)
	}
	if e := db.Create(&alipay.Scope{Kind: "water", UserID: 101}).Error; e != nil {
		t.Fatal(e)
	}
	ar := alipay.Record{Kind: "water", UserID: 101, Payload: json.RawMessage(`{"addressLine1":"深圳市模拟路8号（模拟展示）","supplier":"模拟供水有限公司","amount":"12.30","accountNo":"SIM-W-preserved","addressLine2":"2栋101室（仅用于展示）"}`), Version: "before-clean"}
	if e := db.Create(&ar).Error; e != nil {
		t.Fatal(e)
	}
	run := func(suffix string) {
		t.Helper()
		b, e := os.ReadFile("../../migrations/000020_clean_display_text." + suffix + ".sql")
		if e != nil {
			t.Fatal(e)
		}
		for _, sql := range strings.Split(string(b), ";") {
			if strings.TrimSpace(sql) != "" {
				if e = db.Exec(sql).Error; e != nil {
					t.Fatal(e)
				}
			}
		}
	}
	run("up")
	var clean electricity.Profile
	db.First(&clean, "user_id=?", 101)
	if clean.DisplayName != "张三" || clean.Address != "广东省广州市天河区天河路18号" || clean.MeterReader != "李明" || clean.Version == p.Version {
		t.Fatal("profile cleanup", clean)
	}
	var ct tax.Profile
	db.First(&ct, "user_id=?", 101)
	if ct.IDCard != taxp.IDCard || ct.CardType != "居民身份证" {
		t.Fatal("changed identifier or wrong card type")
	}
	db.First(&company, "user_id=?", 101)
	if company.CompanyName != "广州信息科技有限公司" || company.Post != "职员" {
		t.Fatal("company cleanup")
	}
	db.First(&bill, bill.ID)
	var detail map[string]any
	json.Unmarshal([]byte(bill.Detail), &detail)
	if detail["description"] != "居民用电账单" || detail["note"] != false || bill.Amount != "12.30" {
		t.Fatal("bill type or amount changed", detail, bill.Amount)
	}
	db.First(&nr, nr.ID)
	var payload map[string]any
	json.Unmarshal(nr.Payload, &payload)
	if payload["title"] != "电费账单已出" || payload["count"] != float64(2) || payload["hasPile"] != false || payload["id"] != "SIM-preserved" {
		t.Fatal("JSON types changed", payload)
	}
	db.First(&tr, tr.ID)
	if strings.Contains(string(tr.Payload), "仅供展示") || strings.Contains(string(tr.Payload), "模拟") {
		t.Fatal("tax text not cleaned", string(tr.Payload))
	}
	json.Unmarshal(tr.Payload, &payload)
	if payload["yzm"] != "SIM-code-preserved" || payload["tax"] != "12.30" {
		t.Fatal("changed scan code or tax")
	}
	db.First(&ar, ar.ID)
	json.Unmarshal(ar.Payload, &payload)
	if payload["amount"] != "12.30" || payload["accountNo"] != "SIM-W-preserved" {
		t.Fatal("changed utility identifiers")
	}
	if payload["addressLine2"] != "2栋101室" {
		t.Fatal("display-only suffix not removed", payload)
	}
	version := clean.Version
	run("up")
	db.First(&clean, "user_id=?", 101)
	if clean.Version != version {
		t.Fatal("cleanup is not idempotent")
	}
	run("down")
	db.First(&clean, "user_id=?", 101)
	if clean.DisplayName != p.DisplayName || clean.Address != p.Address || clean.Version == version {
		t.Fatal("backup restore failed")
	}
}
