// model/menu.go
package model

type Menu struct {
	MenuID   int64   `db:"menu_id" json:"menuId"`
	ParentID int64   `db:"parent_id" json:"parentId"`
	Name     string  `db:"name" json:"name"`
	Path     *string `db:"path" json:"path,omitempty"`
	Icon     *string `db:"icon" json:"icon,omitempty"`
	Sort     int     `db:"sort" json:"sort"`
	Status   int8    `db:"status" json:"status"`
	Component *string `db:"component" json:"component,omitempty"`
	Visible   int8    `db:"visible" json:"visible"`
	Children []Menu  `json:"children,omitempty"`
}
type Node struct {
    Menu
    Children []*Node `json:"children,omitempty"`
}
