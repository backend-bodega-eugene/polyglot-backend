package auth

import (
	"gorm.io/gorm"
	"time"
)

const UserStatusActive uint8 = 1

type User struct {
	ID           uint64  `gorm:"primaryKey;autoIncrement"`
	Phone        *string `gorm:"size:20;uniqueIndex:uq_users_phone"`
	Email        *string `gorm:"size:254;uniqueIndex:uq_users_email"`
	RealName     string  `gorm:"size:64;not null"`
	PasswordHash string  `gorm:"size:255;not null"`
	Status       uint8   `gorm:"not null"`
	LastLoginAt  *time.Time
	CreatedAt    time.Time
	UpdatedAt    time.Time
	DeletedAt    gorm.DeletedAt `json:"-"`
}

func (User) TableName() string {
	return "users"
}
