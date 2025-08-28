package gorms

import (
	"context"
	"errors"
	"strconv"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"

	"gorm.io/gorm"
)

type PermissionGORM struct{ DB *gorm.DB }

func NewPermissionMySQLGORM(db *gorm.DB) repo.UserMenuRepo { // 名字沿用你现有风格
	return &PermissionGORM{DB: db}
}

// CreateUserMenuModels：单条插入（保留你原签名与语义）
func (r *PermissionGORM) CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error {
	return r.DB.WithContext(ctx).Table("user_menu").Create(userMenus).Error
}

// CreateUserMenuModel：批量插入（[]model.UserMenu）
func (r *PermissionGORM) CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error {
	if len(userMenus) == 0 {
		return nil
	}
	return r.DB.WithContext(ctx).Table("user_menu").Create(&userMenus).Error
}

func (r *PermissionGORM) FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error) {
	if _, err := strconv.ParseUint(userId, 10, 64); err != nil {
		return nil, errors.New("invalid userId")
	}
	var out []model.UserMenu
	err := r.DB.WithContext(ctx).Table("user_menu").
		Select("user_menu_id", "user_id", "menu_id").
		Where("user_id = ?", userId).
		Order("menu_id").
		Find(&out).Error
	return out, err
}

// UpdateUserMenuModel：把某条的 menu_id 改一下（最小实现）
func (r *PermissionGORM) UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error {
	res := r.DB.WithContext(ctx).Table("user_menu").
		Where("user_id = ? AND user_menu_id = ?", userId, userMenu.UserMenuId).
		Update("menu_id", userMenu.MenuId)
	return res.Error
}

func (r *PermissionGORM) DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error {
	return r.DB.WithContext(ctx).Table("user_menu").
		Where("user_menu_id = ?", userMenuId).
		Delete(&model.UserMenu{}).Error
}

func (r *PermissionGORM) DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error {
	if len(userMenuIds) == 0 {
		return nil
	}
	return r.DB.WithContext(ctx).Table("user_menu").
		Where("user_menu_id IN ?", userMenuIds).
		Delete(&model.UserMenu{}).Error
}

// SaveUserMenus：幂等保存（事务：先删后插；可选过滤有效菜单）
func (r *PermissionGORM) SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error {
	uid, err := strconv.ParseUint(userId, 10, 64)
	if err != nil || uid == 0 {
		return errors.New("invalid userId")
	}
	return r.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 可选：只保留有效菜单（status=1）
		var validIds []uint64
		if len(menuIds) > 0 {
			if err := tx.Table("menus").
				Where("menu_id IN ? AND status = 1", menuIds).
				Pluck("menu_id", &validIds).Error; err != nil {
				return err
			}
		}

		if err := tx.Table("user_menu").Where("user_id = ?", uid).Delete(&model.UserMenu{}).Error; err != nil {
			return err
		}
		if len(validIds) == 0 {
			return nil
		}
		// 批量插入
		rows := make([]model.UserMenu, 0, len(validIds))
		for _, mid := range validIds {
			rows = append(rows, model.UserMenu{UserId: uid, MenuId: mid})
		}
		return tx.Table("user_menu").Create(&rows).Error
	})
}
