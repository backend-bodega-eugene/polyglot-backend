// handler/menu_handler.go
package handler

import (
	"net/http"
	"strconv"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/internal/service"
	"github.com/gin-gonic/gin"
)

type MenuHandler struct{ Repo repo.MenuRepo }

// ===== 你原有的接口（保留原样）=====
func (h *MenuHandler) GetMyMenus(c *gin.Context) {
	uid, _ := c.Get("uid")
	uname, _ := c.Get("uname")
	var items []model.Menu
	var err error

	if uname == "eugene" { // 超管
		items, err = h.Repo.ListAll(c)
	} else {
		items, err = h.Repo.ListByUser(c, uid.(int64))
	}
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
		return
	}
	// 这里仍然沿用你原来的 BuildMenuTree（注意它只会长一层，管理页不要用它）
	c.JSON(http.StatusOK, service.BuildMenuTree(items))
}

// ===== 新增：管理页树接口（全量）=====

type resp struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data,omitempty"`
}

// snake_case 的节点 DTO（适配前端：menu_id / parent_id）
type nodeSnake struct {
	MenuID    int64       `json:"menu_id"`
	ParentID  int64       `json:"parent_id"`
	Name      string      `json:"name"`
	Path      *string     `json:"path,omitempty"`
	Icon      *string     `json:"icon,omitempty"`
	Sort      int         `json:"sort"`
	Status    int8        `json:"status"`
	Component *string     `json:"component,omitempty"`
	Visible   int8        `json:"visible"`
	Children  []nodeSnake `json:"children,omitempty"`
}

func toSnakeNodes(ns []*model.Node) []nodeSnake {
	out := make([]nodeSnake, 0, len(ns))
	for _, n := range ns {
		item := nodeSnake{
			MenuID:    n.MenuID,
			ParentID:  n.ParentID,
			Name:      n.Name,
			Path:      n.Path,
			Icon:      n.Icon,
			Sort:      n.Sort,
			Status:    n.Status,
			Component: n.Component,
			Visible:   n.Visible,
		}
		if len(n.Children) > 0 {
			item.Children = toSnakeNodes(n.Children)
		}
		out = append(out, item)
	}
	return out
}

// GET /api/menus/tree （管理页用）
func (h *MenuHandler) GetTree(c *gin.Context) {
	svc := service.NewService(h.Repo) // 直接复用你的 repo
	nodes, err := svc.GetTree(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: err.Error()})
		return
	}
	c.JSON(http.StatusOK, resp{Code: 0, Msg: "success", Data: toSnakeNodes(nodes)})
}

// ===== 新增：详情 / 新增 / 修改 / 删除 =====

// 输入也是 snake_case，方便直接接你前端 formJSON
type menuInSnake struct {
	MenuID    int64   `json:"menu_id"`   // POST 可不传 / 传 0
	ParentID  int64   `json:"parent_id"`
	Name      string  `json:"name"`
	Path      *string `json:"path"`
	Icon      *string `json:"icon"`
	Sort      int     `json:"sort"`
	Status    int8    `json:"status"`
	Component *string `json:"component"`
	Visible   int8    `json:"visible"`
}

func fromSnake(in menuInSnake) model.Menu {
	return model.Menu{
		MenuID:    in.MenuID,
		ParentID:  in.ParentID,
		Name:      in.Name,
		Path:      in.Path,
		Icon:      in.Icon,
		Sort:      in.Sort,
		Status:    in.Status,
		Component: in.Component,
		Visible:   in.Visible,
	}
}

func toSnakeOne(m *model.Menu) nodeSnake {
	return nodeSnake{
		MenuID:    m.MenuID,
		ParentID:  m.ParentID,
		Name:      m.Name,
		Path:      m.Path,
		Icon:      m.Icon,
		Sort:      m.Sort,
		Status:    m.Status,
		Component: m.Component,
		Visible:   m.Visible,
	}
}

// GET /api/menus/:id
func (h *MenuHandler) GetOne(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: "invalid id"})
		return
	}
	svc := service.NewService(h.Repo)
	m, err := svc.GetOne(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: err.Error()})
		return
	}
	c.JSON(http.StatusOK, resp{Code: 0, Msg: "success", Data: toSnakeOne(m)})
}

// POST /api/menus
func (h *MenuHandler) Create(c *gin.Context) {
	var in menuInSnake
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: "bad request: " + err.Error()})
		return
	}
	m := fromSnake(in)
	m.MenuID = 0 // 自增
	svc := service.NewService(h.Repo)
	newID, err := svc.Create(c.Request.Context(), &m)
	if err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: err.Error()})
		return
	}
	c.JSON(http.StatusOK, resp{Code: 0, Msg: "success", Data: gin.H{"menu_id": newID}})
}

// PUT /api/menus/:id
func (h *MenuHandler) Update(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: "invalid id"})
		return
	}
	var in menuInSnake
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: "bad request: " + err.Error()})
		return
	}
	m := fromSnake(in)
	m.MenuID = id
	svc := service.NewService(h.Repo)
	if err := svc.Update(c.Request.Context(), &m); err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: err.Error()})
		return
	}
	c.JSON(http.StatusOK, resp{Code: 0, Msg: "success"})
}

// DELETE /api/menus/:id
func (h *MenuHandler) Delete(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: "invalid id"})
		return
	}
	svc := service.NewService(h.Repo)
	if err := svc.Delete(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusOK, resp{Code: 1, Msg: err.Error()})
		return
	}
	c.JSON(http.StatusOK, resp{Code: 0, Msg: "success"})
}
