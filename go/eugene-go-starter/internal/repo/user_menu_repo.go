package repo

import (
	"context"
	"eugene-go-starter/internal/model"
)

type UserMenuRepo interface {
	CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error
	CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error
	FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error)
	UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error
	DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error
	DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error
	SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error

	// //创建多个权限记录
	// CreateUserMenuModels(ctx context.Context, userMenu *model.UserMenu) error
	// //创建单个权限记录,其实上面创建也可以创建多个
	// CreateUserMenuModel(ctx context.Context, userMenu []model.UserMenu) error

	// //更新一条权限记录
	// UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error
	// //删除一条权限记录
	// DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error
	// //删除多条权限记录
	// DeleteUserMenuModels(ctx context.Context, userMenuId []uint64) error

	// 	//通过用户ID查找权限记录
	// FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error)
	// //根据你定以的 saveUserMenus,这样符合我们现在的表结构吗,
	// //因为现在的表结构是一对一的userid vs menuid,那这方法只要menuids是多个,就要长生很多条记录?
	// saveUserMenus(ctx context.Context, userId string, menuIds []uint64) error
}
