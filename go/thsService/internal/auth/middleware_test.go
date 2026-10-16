package auth

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
)

func TestRequireAccessToken(t *testing.T) {
	gin.SetMode(gin.TestMode)
	tokens := NewTokenIssuer("test-secret-with-at-least-thirty-two-characters", time.Hour)
	token, err := tokens.Issue(42)
	if err != nil {
		t.Fatal(err)
	}

	engine := gin.New()
	engine.GET("/protected", RequireAccessToken(tokens), func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"userID": c.GetUint64(UserIDContextKey)})
	})

	request := httptest.NewRequest(http.MethodGet, "/protected", nil)
	request.Header.Set("Authorization", "Bearer "+token)
	response := httptest.NewRecorder()
	engine.ServeHTTP(response, request)
	if response.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", response.Code, response.Body.String())
	}
}

func TestRequireAccessTokenRejectsMissingToken(t *testing.T) {
	gin.SetMode(gin.TestMode)
	engine := gin.New()
	engine.GET("/protected", RequireAccessToken(NewTokenIssuer("test-secret-with-at-least-thirty-two-characters", time.Hour)), func(c *gin.Context) {
		c.Status(http.StatusOK)
	})

	response := httptest.NewRecorder()
	engine.ServeHTTP(response, httptest.NewRequest(http.MethodGet, "/protected", nil))
	if response.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", response.Code)
	}
}
