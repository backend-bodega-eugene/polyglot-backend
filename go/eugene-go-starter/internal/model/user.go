
package model

import (
	"time"

	"golang.org/x/crypto/bcrypt"
)

type UserStatus uint8

const (
	UserStatusDisabled UserStatus = 0
	UserStatusEnabled  UserStatus = 1
)

type User struct {
	UserID       uint64     `db:"user_id" json:"userId"`
	SiteID       uint64     `db:"site_id" json:"siteId"`
	Username     string     `db:"username" json:"username"`
	PasswordHash string     `db:"password_hash" json:"-"`
	Status       UserStatus `db:"status" json:"status"`
	LastLoginAt  *time.Time `db:"last_login_at" json:"lastLoginAt,omitempty"`
	UpdatedAt    time.Time  `db:"updated_at" json:"updatedAt"`
}

func (u *User) SetPassword(plain string) error {
	hash, err := bcrypt.GenerateFromPassword([]byte(plain), bcrypt.DefaultCost)
	if err != nil {
		return err
	}
	u.PasswordHash = string(hash)
	return nil
}

func (u *User) CheckPassword(plain string) bool {
	return bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(plain)) == nil
}
