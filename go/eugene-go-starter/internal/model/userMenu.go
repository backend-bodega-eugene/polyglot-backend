package model

import "time"

type UserMenu struct {
	UserMenuId uint64    `db:"user_menu_id" json:"userMenuId"`
	MenuId     uint64    `db:"menu_id" json:"menuId"`
	UserId     uint64    `db:"user_id" json:"createdAt"`
	CreatedAt  time.Time `db:"created_at" json:"updatedAt"`
}
