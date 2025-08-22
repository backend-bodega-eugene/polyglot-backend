package repo

import(
	"context"
	"eugene-go-starter/internal/model"
)

type UserRepo interface {
    FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error)
    Create(ctx context.Context, u *model.User) error
    UpdateLoginAt(ctx context.Context, userID uint64) error
    UpdatePassword(ctx context.Context, userID uint64,oldpassword string ,newPassword string) error
    UpdateStatus(ctx context.Context, userID uint64, status int) error
}
   