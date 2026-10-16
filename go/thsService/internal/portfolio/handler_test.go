package portfolio

import (
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestRejectInvalidQueryBeforeDatabase(t *testing.T) {
	gin.SetMode(gin.TestMode)
	h := NewHandler(nil)
	r := gin.New()
	r.GET("/positions", h.Positions)
	r.GET("/trades", h.Trades)
	for _, path := range []string{"/positions?totalAssets=1&totalAssets=2", "/trades?page=0", "/trades?page=-1", "/trades?page=abc", "/trades?pageSize=101", "/trades?pageSize=0"} {
		recorder := httptest.NewRecorder()
		r.ServeHTTP(recorder, httptest.NewRequest("GET", path, nil))
		if recorder.Code != 400 {
			t.Errorf("%s returned %d", path, recorder.Code)
		}
	}
}
