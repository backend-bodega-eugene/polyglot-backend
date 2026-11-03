package apidocs

import (
	"embed"
	"net/http"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"github.com/swaggo/swag"
)

//go:embed index.html
var assets embed.FS

// Register is called only when the existing Swagger configuration switch is enabled.
func Register(r *gin.Engine) {
	fallback := ginSwagger.WrapHandler(swaggerFiles.Handler, ginSwagger.DocExpansion("none"))
	r.GET("/swagger/*any", func(c *gin.Context) {
		c.Header("Cache-Control", "no-store")
		switch c.Param("any") {
		case "/", "":
			c.Redirect(http.StatusTemporaryRedirect, "/swagger/index.html")
		case "/index.html":
			b, _ := assets.ReadFile("index.html")
			c.Data(200, "text/html; charset=utf-8", b)
		case "/app.json", "/admin.json", "/doc.json":
			group := "app"
			if c.Param("any") == "/admin.json" {
				group = "admin"
			}
			source, e := swag.ReadDoc()
			if e != nil {
				c.Status(500)
				return
			}
			data, e := Build([]byte(source), group)
			if e != nil {
				c.Status(500)
				return
			}
			c.Data(200, "application/json; charset=utf-8", data)
		default:
			fallback(c)
		}
	})
}
