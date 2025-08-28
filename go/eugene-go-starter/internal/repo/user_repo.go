package repo

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"strings"
)

type UserRepo interface {
	FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error)
	Create(ctx context.Context, u *model.User) error
	UpdateLoginAt(ctx context.Context, userID uint64) error
	UpdatePassword(ctx context.Context, userID uint64, oldpassword string, newPassword string) error
	UpdateStatus(ctx context.Context, userID uint64, status int) error
	//上面是我原来的登陆,修改密码,更新状态等接口,下面是我自己写的,你看这样定以行不行
	ListAll(ctx context.Context, page int, size int, keywords string) ([]model.User, error)
	GetByID(ctx context.Context, userID uint64) (*model.User, error)
	DdleteByID(ctx context.Context, userID uint64) error
	UpdateByID(ctx context.Context, user *model.User) error
	AddUser(ctx context.Context, user *model.User) error
}

var (
	ErrNotFound             = errors.New("user not found")
	ErrConflict             = errors.New("user conflict")
	ErrInvalidPassword      = errors.New("invalid password")
	ErrUserNotFound         = errors.New("user not found")
	ErrOldPasswordIncorrect = errors.New("old password incorrect")
	ErrUpdateFailed         = errors.New("update failed")
	ErrHashWrong            = errors.New("hash generate failed")
	ErrUpdateStatusFailed   = errors.New("update status failed")
)

func TrimPtr(s *string) *string {
	if s == nil {
		return nil
	}
	t := strings.TrimSpace(*s)
	return &t
}
