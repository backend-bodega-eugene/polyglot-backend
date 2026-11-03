package admin

import (
	"bytes"
	"encoding/json"
	"fmt"
	"image"
	"image/color"
	"image/png"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func assetTestPNG(t *testing.T) []byte {
	t.Helper()
	img := image.NewNRGBA(image.Rect(0, 0, 8, 8))
	img.SetNRGBA(0, 0, color.NRGBA{R: 12, G: 34, B: 56, A: 70})
	var b bytes.Buffer
	if e := png.Encode(&b, img); e != nil {
		t.Fatal(e)
	}
	return b.Bytes()
}
func TestCanonicalAsset(t *testing.T) {
	b := assetTestPNG(t)
	out, w, h, e := canonicalAsset(b)
	if e != nil || w != 8 || h != 8 {
		t.Fatal(w, h, e)
	}
	img, _, e := image.Decode(bytes.NewReader(out))
	if e != nil {
		t.Fatal(e)
	}
	_, _, _, alpha := img.At(0, 0).RGBA()
	if alpha == 0 || alpha == 65535 {
		t.Fatal("transparency lost")
	}
	if _, _, _, e = canonicalAsset([]byte(`<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>`)); e == nil {
		t.Fatal("SVG accepted")
	}
	if _, _, _, e = canonicalAsset(append([]byte("PNG"), bytes.Repeat([]byte{0}, 100)...)); e == nil {
		t.Fatal("invalid image accepted")
	}
	imgLarge := image.NewNRGBA(image.Rect(0, 0, 8193, 1))
	var large bytes.Buffer
	png.Encode(&large, imgLarge)
	if _, _, _, e = canonicalAsset(large.Bytes()); e == nil {
		t.Fatal("oversized dimensions accepted")
	}
}

func testPDFAssets(t *testing.T, db *gorm.DB, r *gin.Engine, eugene *http.Cookie) {
	t.Helper()
	var asen Administrator
	if e := db.Where("username=?", "Asen").First(&asen).Error; e != nil {
		t.Fatal(e)
	}
	// Even another super administrator cannot review: the username is also required.
	oldSuper := asen.IsSuperAdmin
	db.Model(&asen).Update("is_super_admin", true)
	defer db.Model(&asen).Update("is_super_admin", oldSuper)
	token, e := NewHandler(db, "test-secret", 3600).tokens.Issue(asen.ID)
	if e != nil {
		t.Fatal(e)
	}
	other := &http.Cookie{Name: cookieName, Value: token}
	call := func(method, path string, body []byte, content string, cookie *http.Cookie, origin string) *httptest.ResponseRecorder {
		t.Helper()
		req := httptest.NewRequest(method, "/api/admin/pdf-assets"+path, bytes.NewReader(body))
		if content != "" {
			req.Header.Set("Content-Type", content)
		}
		if cookie != nil {
			req.AddCookie(cookie)
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
			t.Fatalf("got %d want %d: %s", w.Code, status, w.Body.String())
		}
	}
	list := func() PDFAssetList {
		t.Helper()
		w := call("GET", "", nil, "", eugene, "")
		check(w, 200)
		var out PDFAssetListResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		return out.Data
	}
	data := assetTestPNG(t)
	upload := func(slot int, b []byte, cookie *http.Cookie, origin string) *httptest.ResponseRecorder {
		t.Helper()
		var body bytes.Buffer
		m := multipart.NewWriter(&body)
		f, e := m.CreateFormFile("file", "素材.png")
		if e != nil {
			t.Fatal(e)
		}
		f.Write(b)
		m.Close()
		return call("POST", fmt.Sprintf("/slots/%d/upload", slot), body.Bytes(), m.FormDataContentType(), cookie, origin)
	}
	decode := func(w *httptest.ResponseRecorder) PDFAsset {
		t.Helper()
		check(w, 200)
		var out PDFAssetResponse
		if e := json.Unmarshal(w.Body.Bytes(), &out); e != nil {
			t.Fatal(e)
		}
		return out.Data
	}
	review := func(a PDFAsset, decision, note string, cookie *http.Cookie) *httptest.ResponseRecorder {
		t.Helper()
		b, _ := json.Marshal(PDFAssetReview{Decision: decision, Note: note})
		return call("POST", fmt.Sprintf("/versions/%d/review", a.ID), b, "application/json", cookie, "")
	}
	check(call("GET", "", nil, "", nil, ""), 401)
	initial := list()
	if !initial.CanReview || len(initial.Slots) != 5 {
		t.Fatal("slots", initial)
	}
	for _, s := range initial.Slots {
		if s.Active != nil {
			t.Fatal("unexpected default asset")
		}
	}
	check(upload(3, data, nil, ""), 401)
	check(upload(3, data, other, "https://evil.example"), 403)
	check(upload(2, data, other, ""), 400)
	check(upload(3, []byte("<svg/>"), other, ""), 400)
	check(upload(3, bytes.Repeat([]byte{1}, (5<<20)+1), other, ""), 400)
	first := decode(upload(3, data, other, ""))
	if first.Status != "pending" || first.UploadedBy != asen.ID || first.SHA256 == "" {
		t.Fatal(first)
	}
	if list().Slots[0].Active != nil {
		t.Fatal("upload activated before review")
	}
	check(call("GET", fmt.Sprintf("/versions/%d/image", first.ID), nil, "", nil, ""), 401)
	imageResponse := call("GET", fmt.Sprintf("/versions/%d/image", first.ID), nil, "", other, "")
	check(imageResponse, 200)
	if imageResponse.Header().Get("Content-Type") != "image/png" {
		t.Fatal("wrong image type")
	}
	check(review(first, "approve", "", other), 403)
	check(review(first, "reject", "", eugene), 400)
	decode(review(first, "approve", "授权已核对", eugene))
	check(review(first, "approve", "", eugene), 409)
	if s := list().Slots[0]; s.ActiveID == nil || *s.ActiveID != first.ID || s.Active == nil {
		t.Fatal("approval missing")
	}
	second := decode(upload(3, data, other, ""))
	if *list().Slots[0].ActiveID != first.ID {
		t.Fatal("new pending replaced active")
	}
	decode(review(second, "reject", "请更换素材", eugene))
	if *list().Slots[0].ActiveID != first.ID {
		t.Fatal("rejection changed active")
	}
	third := decode(upload(3, data, other, ""))
	decode(review(third, "approve", "复核通过", eugene))
	if *list().Slots[0].ActiveID != third.ID {
		t.Fatal("approved replacement missing")
	}
	var audit PDFAssetAudit
	if e := db.Where("asset_id=? AND action='approve'", third.ID).First(&audit).Error; e != nil || audit.PreviousActiveID == nil || *audit.PreviousActiveID != first.ID {
		t.Fatal("audit previous version", audit, e)
	}
	var retained PDFAsset
	if e := db.First(&retained, first.ID).Error; e != nil || len(retained.ImageData) == 0 {
		t.Fatal("old asset lost", e)
	}
	for slot := 4; slot <= 7; slot++ {
		a := decode(upload(slot, data, other, ""))
		if a.Slot != slot {
			t.Fatal(a)
		}
	}
	if w := call("GET", "", nil, "", other, ""); strings.Contains(w.Body.String(), `"canReview":true`) {
		t.Fatal("other reviewer enabled")
	}
}
