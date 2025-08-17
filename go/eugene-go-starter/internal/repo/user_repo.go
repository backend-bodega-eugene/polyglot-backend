package repo

import(
	"context"
	"eugene-go-starter/internal/model"
)

type UserRepo interface {
    FindBySiteAndUsername(ctx context.Context, siteID uint64, username string) (*model.User, error)
    Create(ctx context.Context, u *model.User) error
    UpdateLoginAt(ctx context.Context, userID uint64) error
}
