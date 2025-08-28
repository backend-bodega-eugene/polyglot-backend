package repo

import (
	"context"
	"eugene-go-starter/internal/model"

	"gorm.io/gorm"
)

type PermissionGorm struct{ DB *gorm.DB }

func NewPermissionGorm(db *gorm.DB) UserMenuRepo { return &PermissionGorm{DB: db} }

func (r *PermissionGorm) CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error {
	return r.DB.WithContext(ctx).Table("user_menu").Create(userMenus).Error
}

func (r *PermissionGorm) CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error {
	if len(userMenus) == 0 {
		return nil
	}
	return r.DB.WithContext(ctx).Table("user_menu").Create(&userMenus).Error
}

func (r *PermissionGorm) FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error) {
	var out []model.UserMenu
	if err := r.DB.WithContext(ctx).Table("user_menu").
		Where("user_id = ?", userId).Order("menu_id").Find(&out).Error; err != nil {
		return nil, err
	}
	return out, nil
}

func (r *PermissionGorm) UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error {
	res := r.DB.WithContext(ctx).Table("user_menu").
		Where("user_id = ? AND user_menu_id = ?", userId, userMenu.UserMenuId).
		Update("menu_id", userMenu.MenuId)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *PermissionGorm) DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error {
	res := r.DB.WithContext(ctx).Table("user_menu").Where("user_menu_id = ?", userMenuId).Delete(nil)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *PermissionGorm) DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error {
	if len(userMenuIds) == 0 {
		return nil
	}
	return r.DB.WithContext(ctx).Table("user_menu").Where("user_menu_id in ?", userMenuIds).Delete(nil).Error
}

func (r *PermissionGorm) SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error {
	return r.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 过滤有效菜单（status=1）
		var validIds []uint64
		if len(menuIds) > 0 {
			if err := tx.Table("menus").Where("menu_id in ? AND status = 1", menuIds).Pluck("menu_id", &validIds).Error; err != nil {
				return err
			}
		}
		if err := tx.Table("user_menu").Where("user_id = ?", userId).Delete(nil).Error; err != nil {
			return err
		}
		if len(validIds) > 0 {
			for _, mid := range validIds {
				if err := tx.Exec("INSERT INTO user_menu (user_id, menu_id) VALUES (?, ?)", userId, mid).Error; err != nil {
					return err
				}
			}
		}
		return nil
	})
}
