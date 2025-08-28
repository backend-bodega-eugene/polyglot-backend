// model/menu.go
package model

type Menu struct {
	MenuID    uint64  `db:"menu_id" json:"menuId" bson:"menu_id"`
	ParentID  uint64  `db:"parent_id" json:"parentId" bson:"parent_id"`
	Name      string  `db:"name" json:"name" bson:"name"`
	Path      *string `db:"path" json:"path,omitempty" bson:"path,omitempty"`
	Icon      *string `db:"icon" json:"icon,omitempty" bson:"icon,omitempty"`
	Sort      int     `db:"sort" json:"sort" bson:"sort"`
	Status    int8    `db:"status" json:"status" bson:"status"`
	Component *string `db:"component" json:"component,omitempty" bson:"component,omitempty"`
	Visible   int8    `db:"visible" json:"visible" bson:"visible"`
	Children  []Menu  `db:"-" gorm:"-" json:"children,omitempty" bson:"-"` // 不入库，只做返回
}

type Node struct {
	Menu     `bson:",inline"`                     // 展平 Menu 字段（可选）
	Children []*Node `json:"children,omitempty" bson:"-"` // 也不入库
}

