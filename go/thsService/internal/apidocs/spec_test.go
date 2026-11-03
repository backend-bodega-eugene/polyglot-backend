package apidocs

import (
	"encoding/json"
	"net/http/httptest"
	"os"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	_ "ths-service/docs"
)

func sourceDoc(t *testing.T) []byte {
	t.Helper()
	b, e := os.ReadFile("../../docs/swagger.json")
	if e != nil {
		t.Fatal(e)
	}
	return b
}
func decodeDoc(t *testing.T, b []byte) map[string]any {
	t.Helper()
	var doc map[string]any
	if e := json.Unmarshal(b, &doc); e != nil {
		t.Fatal(e)
	}
	return doc
}
func TestDocumentationGroups(t *testing.T) {
	source := sourceDoc(t)
	all := decodeDoc(t, source)
	seen := map[string]bool{}
	for _, group := range []string{"app", "admin"} {
		b, e := Build(source, group)
		if e != nil {
			t.Fatal(e)
		}
		doc := decodeDoc(t, b)
		paths := object(doc["paths"])
		if len(paths) == 0 {
			t.Fatal("empty group")
		}
		for path, v := range paths {
			if seen[path] {
				t.Fatal("duplicate path across groups", path)
			}
			seen[path] = true
			admin := strings.HasPrefix(path, "/api/admin/") || strings.HasPrefix(path, "/health/")
			if admin != (group == "admin") {
				t.Fatal("wrong group", path)
			}
			for method, raw := range object(v) {
				if method == "parameters" {
					continue
				}
				op := object(raw)
				if op == nil {
					continue
				}
				tag := op["tags"].([]any)[0].(string)
				if tag != classify(path) {
					t.Fatal("wrong category", path)
				}
				if len(op["description"].(string)) < 20 {
					t.Fatal("missing operation notes", path)
				}
			}
		}
		if group == "app" {
			for name := range object(doc["definitions"]) {
				if strings.Contains(name, "internal_admin") {
					t.Fatal("App docs exposed admin model", name)
				}
			}
			if object(doc["securityDefinitions"])["BearerAuth"] == nil {
				t.Fatal("App bearer auth missing")
			}
		} else if doc["securityDefinitions"] != nil {
			t.Fatal("backend offered member authentication")
		}
		defs := object(doc["definitions"])
		var check func(any)
		check = func(v any) {
			switch x := v.(type) {
			case map[string]any:
				if ref, ok := x["$ref"].(string); ok && strings.HasPrefix(ref, "#/definitions/") {
					if defs[strings.TrimPrefix(ref, "#/definitions/")] == nil {
						t.Fatal("broken model reference", ref)
					}
				}
				for _, child := range x {
					check(child)
				}
			case []any:
				for _, child := range x {
					check(child)
				}
			}
		}
		check(doc)
		for name, raw := range defs {
			for key, v := range object(object(raw)["properties"]) {
				if object(v)["description"] == nil {
					t.Errorf("missing field notes %s.%s", name, key)
				}
			}
		}
		t.Logf("%s: %d paths, %d models", group, len(paths), len(defs))
	}
	if len(seen) != len(object(all["paths"])) {
		t.Fatal("some documented routes were lost")
	}
}
func TestSchemaFieldGuides(t *testing.T) {
	b, e := Build(sourceDoc(t), "admin")
	if e != nil {
		t.Fatal(e)
	}
	doc := decodeDoc(t, b)
	for _, path := range []string{"/api/admin/alipay/{kind}", "/api/admin/tax-business/{kind}", "/api/admin/nanwang/{kind}"} {
		op := object(object(object(doc["paths"])[path])["post"])
		desc := op["description"].(string)
		if !strings.Contains(desc, "payload字段") || !strings.Contains(desc, "expectedVersion") {
			t.Fatal("field guide missing", path)
		}
	}
}
func TestSwaggerRoutes(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	Register(r)
	for _, path := range []string{"/swagger/index.html", "/swagger/index.html?group=admin", "/swagger/app.json", "/swagger/admin.json", "/swagger/doc.json", "/swagger/swagger-ui-bundle.js", "/swagger/swagger-ui.css"} {
		w := httptest.NewRecorder()
		r.ServeHTTP(w, httptest.NewRequest("GET", path, nil))
		if w.Code != 200 {
			t.Fatalf("%s: HTTP%d %s", path, w.Code, w.Body.String())
		}
		if strings.Contains(path, "index.html") {
			if !strings.Contains(w.Body.String(), "docExpansion:'none'") || !strings.Contains(w.Body.String(), "App 端") {
				t.Fatal("missing UI grouping/collapse")
			}
		}
	}
	if _, e := Build(sourceDoc(t), "unknown"); e == nil {
		t.Fatal("unknown group accepted")
	}
}
