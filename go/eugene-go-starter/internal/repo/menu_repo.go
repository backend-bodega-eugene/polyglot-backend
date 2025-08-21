package repo

import(
	"context"
	"eugene-go-starter/internal/model"
)

type MenuRepo interface {
	// 基础查询
	ListAll(ctx context.Context) ([]model.Menu, error)
	ListByUser(ctx context.Context, userID int64) ([]model.Menu, error)

	// （后续 CRUD / 绑定可慢慢加）
	// Create(ctx context.Context, m *Menu) (int64, error)
	// Update(ctx context.Context, m *Menu) error
	// Delete(ctx context.Context, menuID int64) error
	// ReplaceUserMenus(ctx context.Context, userID int64, menuIDs []int64) error
}