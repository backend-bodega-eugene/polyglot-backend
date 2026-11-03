package electricity

import (
	"encoding/json"
	"testing"
)

func TestAppClientContract(t *testing.T) {
	v := View{Profile: Profile{Fields: Fields{DisplayName: "张三", Address: "示例路", UserIDNumber: "0943000049338409", Supplier: "供电局", MeterReader: "李明", MeterNo: "0000012345", PostalCode: "518126"}}, ID: 1, Username: "13800000000"}
	b, err := json.Marshal(AppProfileResponse{0, "success", appProfile(v)})
	if err != nil {
		t.Fatal(err)
	}
	var result struct {
		Msg  string         `json:"msg"`
		Data map[string]any `json:"data"`
	}
	if err = json.Unmarshal(b, &result); err != nil {
		t.Fatal(err)
	}
	keys := []string{"id", "username", "display_name", "address", "user_id_number", "supplier", "meter_reader", "meter_no", "postal_code"}
	if len(result.Data) != len(keys) || result.Msg != "success" {
		t.Fatal(string(b))
	}
	for _, k := range keys {
		if _, ok := result.Data[k]; !ok {
			t.Fatalf("missing %s: %s", k, b)
		}
	}
	if result.Data["meter_no"] != "0000012345" || result.Data["user_id_number"] != "0943000049338409" {
		t.Fatal("leading zero lost")
	}
	rows := appBills([]BillView{{Bill: Bill{Code: "202508", StartDate: "2025-08-01", EndDate: "2025-08-31", Amount: "128.56", TotalUsage: "210.00"}, Detail: json.RawMessage(`{}`)}})
	b, err = json.Marshal(AppBillsResponse{0, "success", rows})
	if err != nil {
		t.Fatal(err)
	}
	var bills struct {
		Data []map[string]any `json:"data"`
	}
	if err = json.Unmarshal(b, &bills); err != nil {
		t.Fatal(err)
	}
	row := bills.Data[0]
	if len(row) != 8 || row["amount"] != 128.56 || row["total_usage"] != float64(210) || row["bill_month"] != "202508" || row["start_date"] != "2025-08-01" || row["end_date"] != "2025-08-31" {
		t.Fatal(string(b))
	}
	if _, ok := row["detail"].(map[string]any); !ok {
		t.Fatal("detail is not object")
	}
}
