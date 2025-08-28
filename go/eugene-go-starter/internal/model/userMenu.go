package model

import "time"

type UserMenu struct {
	UserMenuId uint64    `db:"user_menu_id" json:"userMenuId" bson:"user_menu_id"`
	MenuId     uint64    `db:"menu_id"      json:"menuId"    bson:"menu_id"`
	UserId     uint64    `db:"user_id"      json:"userId"    bson:"user_id"`
	CreatedAt  time.Time `db:"created_at"   json:"createdAt" bson:"created_at"`
}
