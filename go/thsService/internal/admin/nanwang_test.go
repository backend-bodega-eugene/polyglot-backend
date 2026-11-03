package admin

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
	"ths-service/internal/nanwang"
)

func testNanwangCRUD(t *testing.T, db *gorm.DB, call func(string, string, string, *http.Cookie) *httptest.ResponseRecorder, cookie *http.Cookie) {
	t.Helper()
	if e := db.Exec("INSERT INTO users(id,phone,real_name,password_hash,status) VALUES (71,'13900000071','南网甲','unused',1),(72,'13900000072','南网乙','unused',1)").Error; e != nil {
		t.Fatal(e)
	}
	service := nanwang.NewService(db)
	tokens := auth.NewTokenIssuer("nanwang-test", time.Hour)
	app := gin.New()
	group := app.Group("", auth.RequireAccessToken(tokens), auth.RequireActiveMember(db))
	nanwang.Register(group, service)
	appCall := func(uid uint64, path, body string, status int) nanwang.AppResponse {
		t.Helper()
		req := httptest.NewRequest("POST", path, strings.NewReader(body))
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
		var out nanwang.AppResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		if status == 200 && out.Sta != "00" {
			t.Fatal("sta mismatch")
		}
		return out
	}
	adminCall := func(method, path string, body any, status int) *httptest.ResponseRecorder {
		t.Helper()
		b, _ := json.Marshal(body)
		w := call(method, path, string(b), cookie)
		if w.Code != status {
			t.Fatalf("admin %s %s got%d want%d: %s", method, path, w.Code, status, w.Body.String())
		}
		return w
	}
	paths := map[string]string{"news": "/mp/ucs/ma/zt/content/queryHotInformations", "recommended": "/mp/w2/wx/portal/eleCustNumber/queryRecommendUsers", "bound": "/mp/w2/wx/portal/eleCustNumber/queryBindEleUsers", "arrears": "/mp/w2/wx/elecmanageImp/electric-fee/queryBindArrearsList", "points": "/mp/w2/wx/elecmanageImp/electric-fee/queryMeteringPointList", "halls": "/mp/ucs/ma/businesshall/businessHall/queryBusinessHallNearby", "messages": "/mp/ucs/ma/intramsg/txhMsg/queryUnreadTitleList", "charging": "/mp/w2/wx/elecmanageImp/electric-daily/checkChargingPile"}
	hallBody := `{"longitude":"114.06","latitude":"22.54"}`
	appCall(0, paths["news"], `{}`, 401)
	appCall(71, paths["news"], `{"count":"0"}`, 400)
	appCall(71, paths["halls"], `{}`, 400)
	appCall(71, paths["halls"], `{"longitude":"NaN","latitude":"22"}`, 400)
	adminCall("GET", "/api/admin/nanwang/unknown", nil, 400)
	adminCall("GET", "/api/admin/nanwang/schemas", nil, 200)
	if w := call("GET", "/api/admin/nanwang/news", "", nil); w.Code != 401 {
		t.Fatal("anonymous admin")
	}
	var empty NanwangListResponse
	json.Unmarshal(adminCall("GET", "/api/admin/nanwang/news", nil, 200).Body.Bytes(), &empty)
	if empty.Data.Total != 0 {
		t.Fatal("admin auto-generated")
	}
	for _, def := range nanwang.Schemas {
		body := `{}`
		if def.Kind == "halls" {
			body = hallBody
		}
		first := appCall(71, paths[def.Kind], body, 200)
		again := appCall(71, paths[def.Kind], body, 200)
		if !reflect.DeepEqual(first, again) {
			t.Fatal("unstable", def.Kind)
		}
		other := appCall(72, paths[def.Kind], body, 200)
		if def.Public && !reflect.DeepEqual(first, other) {
			t.Fatal("public data not shared")
		}
		uid := uint64(71)
		if def.Public {
			uid = 0
		}
		list, e := service.List(t.Context(), def.Kind, uid, "", 1, 100)
		if e != nil || len(list.Items) == 0 {
			t.Fatal("missing stored data", e)
		}
		row := list.Items[0]
		if !def.Public {
			for _, r := range list.Items {
				if r.OwnerID != 71 {
					t.Fatal("cross member list")
				}
			}
		}
		var payload map[string]any
		dec := json.NewDecoder(bytes.NewReader(row.Payload))
		dec.UseNumber()
		if e = dec.Decode(&payload); e != nil {
			t.Fatal(e)
		}
		f := def.Fields[0]
		switch f.Type {
		case "boolean":
			payload[f.Key] = true
		default:
			payload[f.Key] = "后台修改（模拟）"
		}
		req := nanwang.Write{UserID: uid, Payload: payload, ExpectedVersion: row.Version}
		root := "/api/admin/nanwang/" + def.Kind
		path := fmt.Sprintf("%s/%d", root, row.ID)
		var changed NanwangRecordResponse
		json.Unmarshal(adminCall("PUT", path, req, 200).Body.Bytes(), &changed)
		adminCall("PUT", path, req, 409)
		adminCall("GET", path, nil, 200)
		got := appCall(71, paths[def.Kind], body, 200)
		if def.Kind != "news" && !strings.Contains(fmt.Sprint(got.Data), fmt.Sprint(payload[f.Key])) {
			t.Fatal("App did not reflect admin update", def.Kind, got)
		}
		if def.Kind == "news" {
			detail := appCall(71, "/mp/ucs/ma/zt/content/queryInformationInfoDetail", fmt.Sprintf(`{"id":"%d"}`, row.ID), 200)
			if detail.Data.(map[string]any)["title"] != payload["title"] {
				t.Fatal("news detail not updated")
			}
		}
		adminCall("DELETE", path, nanwang.Write{ExpectedVersion: row.Version}, 409)
		adminCall("DELETE", path, nanwang.Write{ExpectedVersion: changed.Data.Version}, 200)
		adminCall("GET", path, nil, 404)
		// Manual creation and singleton constraints.
		req.ExpectedVersion = ""
		json.Unmarshal(adminCall("POST", root, req, 200).Body.Bytes(), &changed)
		if def.Singleton {
			adminCall("POST", root, req, 409)
		}
		if def.Public {
			adminCall("POST", root, nanwang.Write{UserID: 71, Payload: payload}, 400)
		} else {
			adminCall("POST", root, nanwang.Write{UserID: 999999, Payload: payload}, 404)
		}
		bad := nanwang.Write{UserID: uid, Payload: map[string]any{"unexpected": true}, ExpectedVersion: changed.Data.Version}
		adminCall("PUT", fmt.Sprintf("%s/%d", root, changed.Data.ID), bad, 400)
	}
	listPath := "/mp/ucs/ma/zt/content/queryInformationInfos"
	filtered := appCall(71, listPath, `{"classId":"1","pageNo":"1","pageSize":"2"}`, 200)
	if len(filtered.Data.([]any)) != 2 {
		t.Fatal("news pagination")
	}
	for _, r := range filtered.Data.([]any) {
		if r.(map[string]any)["classId"] != "1" {
			t.Fatal("class filter")
		}
	}
	appCall(71, "/mp/ucs/ma/zt/content/queryLatestInformations", `{"count":"4"}`, 200)
	appCall(71, listPath, `{"pageNo":"999999","pageSize":"10"}`, 200)
	appCall(71, "/mp/ucs/ma/zt/content/queryInformationInfoDetail", `{"id":"999999"}`, 404)
	appCall(71, paths["points"], `{"eleCustId":"unmatched"}`, 200)
	var p electricity.Profile
	db.First(&p, "user_id=?", 71)
	list, _ := service.List(t.Context(), "bound", 72, "", 1, 100)
	if len(list.Items) != 1 || strings.Contains(string(list.Items[0].Payload), p.UserIDNumber) {
		t.Fatal("cross-member household")
	}
	// Partial deletion never tops up; complete deletion initializes once under concurrency.
	list, _ = service.List(t.Context(), "messages", 71, "", 1, 100)
	for i, r := range list.Items {
		_, e := service.Write(t.Context(), 1, "messages", r.ID, nanwang.Write{ExpectedVersion: r.Version}, "delete")
		if e != nil {
			t.Fatal(e)
		}
		if i == 0 && len(list.Items) > 1 {
			rows, e := service.Ensure(t.Context(), "messages", 71)
			if e != nil || len(rows) != len(list.Items)-1 {
				t.Fatal("partial delete topped up")
			}
		}
	}
	var wg sync.WaitGroup
	versions := make(chan uint64, 6)
	for range 6 {
		wg.Go(func() {
			r, e := service.Ensure(t.Context(), "messages", 71)
			if e != nil {
				t.Error(e)
				return
			}
			if len(r) != 2 {
				t.Error("bad regeneration")
			}
			versions <- r[0].ID
		})
	}
	wg.Wait()
	close(versions)
	var last uint64
	for id := range versions {
		if last != 0 && last != id {
			t.Fatal("duplicate initialization")
		}
		last = id
	}
	var count int64
	db.Model(&nanwang.Audit{}).Where("admin_id IS NOT NULL").Count(&count)
	if count < 24 {
		t.Fatal("missing admin audits")
	}
	if e := db.Delete(&auth.User{}, 72).Error; e != nil {
		t.Fatal(e)
	}
	appCall(72, paths["bound"], `{}`, 401)
	list, _ = service.List(t.Context(), "bound", 72, "", 1, 100)
	if list.Total != 0 {
		t.Fatal("deleted member visible")
	}
	db.Model(&nanwang.Record{}).Where("kind=? AND owner_id=?", "bound", 72).Count(&count)
	if count != 1 {
		t.Fatal("member delete removed history")
	}
}
