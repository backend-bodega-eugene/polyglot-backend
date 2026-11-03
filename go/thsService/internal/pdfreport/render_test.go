package pdfreport

import (
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"testing"
)

func testDraft(kind string) Draft {
	s, _ := Definition(kind)
	d := Draft{Kind: kind, UserID: 7, Fields: map[string]string{}, Tables: map[string][]map[string]string{}}
	for _, f := range s.Fields {
		d.Fields[f.Key] = ""
	}
	for _, t := range s.Tables {
		d.Tables[t.Key] = []map[string]string{}
	}
	d.Fields["name"] = "张三"
	d.Fields["issuedDate"] = "2026-10-27"
	switch kind {
	case "work":
		d.Fields["sex"] = "男"
		d.Fields["idNumber"] = "DEMO-ID-0007"
		d.Fields["companyName"] = "示例科技有限公司"
		d.Fields["department"] = "产品部"
		d.Fields["post"] = "产品经理"
		d.Fields["companyAddress"] = "示例市中心路18号"
		d.Fields["startDate"] = "2020-03-01"
		d.Fields["monthlyIncome"] = "20000"
	case "electricity":
		d.Fields["accountNo"] = "DEMO-E-0007"
		d.Fields["address"] = "示例市中心路18号"
		d.Fields["supplier"] = "示例供电服务单位"
		d.Fields["meterNo"] = "DEMO-M-007"
		d.Fields["usageType"] = "居民生活用电"
		d.Fields["voltage"] = "交流220V"
		d.Fields["startDate"] = "2026-09-01"
		d.Fields["endDate"] = "2026-09-30"
		d.Fields["readDate"] = "2026-10-01"
		d.Fields["dueDate"] = "2026-10-24"
		d.Fields["lateFeeStartDate"] = "2026-10-25"
		d.Fields["lateFeeAsOfDate"] = "2026-10-26"
		d.Fields["arrearsPrincipal"] = "220.37"
		d.Fields["lateFee"] = "0.44"
		d.Fields["totalUsage"] = "389"
		d.Tables["readings"] = []map[string]string{{"label": "峰", "previous": "9821", "current": "9957", "multiplier": "1"}, {"label": "平", "previous": "26279", "current": "26437", "multiplier": "1"}, {"label": "谷", "previous": "6804", "current": "6899", "multiplier": "1"}}
		d.Tables["fees"] = []map[string]string{{"label": "峰段电费", "quantity": "136", "price": "0.5224", "amount": "0"}, {"label": "平段电费", "quantity": "158", "price": "0.5224", "amount": "0"}, {"label": "谷段电费", "quantity": "95", "price": "0.2535", "amount": "0"}, {"label": "第三阶梯加价", "quantity": "109", "price": "0.3", "amount": "0"}, {"label": "第二阶梯加价", "quantity": "100", "price": "0.1", "amount": "0"}}
	case "social":
		d.Fields["idNumber"] = "DEMO-ID-0007"
		d.Fields["city"] = "深圳市"
		d.Fields["manageCode"] = "110500123456789"
		d.Tables["coverage"] = []map[string]string{{"kind": "职工社保（合计）", "startMonth": "2021-01", "months": "60", "status": "参保缴费"}}
		for y := 2021; y <= 2025; y++ {
			for m := 1; m <= 12; m++ {
				d.Tables["payments"] = append(d.Tables["payments"], map[string]string{"month": fmt.Sprintf("%04d-%02d", y, m), "company": "示例科技有限公司", "companyNo": "DEMO-C-007", "base": "20000.00", "companyPay": "4500.00", "personalPay": "2040.00"})
			}
		}
	}
	return d
}
func TestRenderSamples(t *testing.T) {
	for _, kind := range []string{"work", "electricity", "social"} {
		t.Run(kind, func(t *testing.T) {
			d := testDraft(kind)
			assets := []Asset{}
			if dir := os.Getenv("THS_PDF_QA_ASSETS"); dir != "" {
				for _, slot := range map[string][]int{"work": {5}, "electricity": {3, 4, 7}, "social": {6}}[kind] {
					b, e := os.ReadFile(filepath.Join(dir, fmt.Sprintf("%d.png", slot)))
					if e != nil {
						t.Fatal(e)
					}
					assets = append(assets, Asset{Slot: slot, ID: uint64(slot), Data: b})
				}
			}
			b, e := Render(d, assets)
			if e != nil {
				t.Fatal(e)
			}
			if !bytes.HasPrefix(b, []byte("%PDF-")) || len(b) < 1000 {
				t.Fatal("invalid PDF")
			}
			if dir := os.Getenv("THS_PDF_QA_DIR"); dir != "" {
				if e := os.MkdirAll(dir, 0755); e != nil {
					t.Fatal(e)
				}
				if e := os.WriteFile(filepath.Join(dir, kind+".pdf"), b, 0600); e != nil {
					t.Fatal(e)
				}
			}
		})
	}
}
func TestReportValidation(t *testing.T) {
	manual := testDraft("electricity")
	manual.Tables["fees"][0]["calculation"] = "manual"
	manual.Tables["fees"][0]["amount"] = "1.23"
	if err := Validate(&manual); err != nil || Fee(manual.Tables["fees"][0]) != 123 {
		t.Fatalf("manual fee was recalculated: %v", err)
	}
	d := testDraft("electricity")
	if e := Validate(&d); e != nil {
		t.Fatal(e)
	}
	var total int64
	for _, r := range d.Tables["fees"] {
		total += Fee(r)
	}
	if Money(total) != "220.37" {
		t.Fatal(total)
	}
	d.Tables["fees"][0]["quantity"] = "999999999"
	d.Tables["fees"][0]["price"] = "999999999"
	if Validate(&d) == nil {
		t.Fatal("overflow accepted")
	}
	d = testDraft("social")
	d.Tables["details"] = []map[string]string{{"month": "2021-01", "kind": "养老", "base": "20000", "companyPay": "1", "personalPay": "1"}}
	if Validate(&d) == nil {
		t.Fatal("inconsistent detail sums accepted")
	}
	d = testDraft("work")
	d.Fields["removeWatermark"] = "true"
	if Validate(&d) == nil {
		t.Fatal("unknown field accepted")
	}
}
func TestChineseMoney(t *testing.T) {
	for input, want := range map[string]string{"20000": "贰万元整", "10001": "壹万零壹元整", "100100001": "壹亿零壹拾万零壹元整", "0.01": "零元壹分", "1000.50": "壹仟元伍角"} {
		if got := chineseMoney(input); got != want {
			t.Fatalf("%s got %s want %s", input, got, want)
		}
	}
}

func TestRenderManualTierSnapshot(t *testing.T) {
	d := testDraft("electricity")
	d.Tables["readings"] = nil
	d.Fields["totalUsage"] = "368.90"
	d.Fields["electricityFee"] = "299.00"
	d.Fields["payableAmount"] = "300.00"
	d.Tables["fees"] = []map[string]string{
		{"label": "第一档", "quantity": "260.00", "price": "0.66286875", "amount": "172.35", "calculation": "manual"},
		{"label": "第二档", "quantity": "108.90", "price": "0.71286875", "amount": "88.88", "calculation": "manual"},
		{"label": "第三档", "quantity": "0.00", "price": "0.96286875", "amount": "0.00", "calculation": "manual"},
	}
	if e := Validate(&d); e != nil {
		t.Fatal(e)
	}
	if d.Tables["fees"][1]["amount"] != "88.88" {
		t.Fatal("manual amount replaced")
	}
	b, e := Render(d, nil)
	if e != nil {
		t.Fatal(e)
	}
	if dir := os.Getenv("THS_PDF_QA_DIR"); dir != "" {
		if e := os.WriteFile(filepath.Join(dir, "electricity-tiered.pdf"), b, 0600); e != nil {
			t.Fatal(e)
		}
	}
}
