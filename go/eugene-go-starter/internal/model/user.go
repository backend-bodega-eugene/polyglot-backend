package model

import (
	"time"
)

type UserStatus uint8

const (
	UserStatusDisabled UserStatus = 0
	UserStatusEnabled  UserStatus = 1
)

type User struct {
	UserID       uint64     `db:"user_id" json:"userId" bson:"user_id"`
	SiteID       uint64     `db:"site_id" json:"siteId" bson:"site_id"`
	Username     string     `db:"username" json:"username" bson:"username"`
	PasswordHash string     `db:"password_hash" json:"-" bson:"password_hash"`
	Status       UserStatus `db:"status" json:"status" bson:"status"`
	LastLoginAt  *time.Time `db:"last_login_at" json:"lastLoginAt,omitempty" bson:"last_login_at"`
	UpdatedAt    time.Time  `db:"updated_at" json:"updatedAt" bson:"updated_at"`
}

// func (u *User) SetPassword(plain string) error {
// 	hash, err := bcrypt.GenerateFromPassword([]byte(plain), bcrypt.DefaultCost)
// 	if err != nil {
// 		return err
// 	}
// 	u.PasswordHash = string(hash)
// 	return nil
// }

// func (u *User) CheckPassword(plain string) bool {
// 	return bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(plain)) == nil
// }
