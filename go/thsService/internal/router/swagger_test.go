package router

import (
	"net/http/httptest"
	"testing"

	_ "ths-service/docs"
	"ths-service/internal/config"
	"ths-service/internal/database"
)

func TestSwaggerConfigurationSwitch(t *testing.T) {
	for _, enabled := range []bool{false, true} {
		var cfg config.Config
		cfg.JWT.Secret = "documentation-test"
		cfg.Swagger.Enabled = enabled
		r := New(&database.Connection{}, cfg)
		for _, path := range []string{"/swagger/index.html", "/swagger/app.json", "/swagger/admin.json"} {
			w := httptest.NewRecorder()
			r.ServeHTTP(w, httptest.NewRequest("GET", path, nil))
			expected := 404
			if enabled {
				expected = 200
			}
			if w.Code != expected {
				t.Fatalf("enabled=%v %s HTTP%d want%d", enabled, path, w.Code, expected)
			}
		}
	}
}
