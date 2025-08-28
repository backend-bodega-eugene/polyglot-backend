package repo

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"strings"

	"gorm.io/gorm"
)

type menuRepoGorm struct{ db *gorm.DB }

func NewMenuRepoGorm(db *gorm.DB) MenuRepo { return &menuRepoGorm{db: db} }

func (r *menuRepoGorm) ListAll(ctx context.Context) ([]model.Menu, error) {
	var menus []model.Menu
	err := r.db.WithContext(ctx).Table("menus").
		Select("menu_id, parent_id, name, path, icon, sort, status, component, visible").
		Order("sort ASC, menu_id ASC").
		Find(&menus).Error
	return menus, err
}

func (r *menuRepoGorm) ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error) {
	// 假设存在 user_menus(user_id, menu_id) 关系表
	var menus []model.Menu
	err := r.db.WithContext(ctx).
		Table("menus m").
		Select("m.menu_id, m.parent_id, m.name, m.path, m.icon, m.sort, m.status, m.component, m.visible").
		Joins("JOIN user_menus um ON um.menu_id = m.menu_id").
		Where("um.user_id = ?", userID).
		Order("m.sort ASC, m.menu_id ASC").
		Find(&menus).Error
	return menus, err
}

func (r *menuRepoGorm) Create(ctx context.Context, m *model.Menu) (uint64, error) {
	// 唯一约束假设为 name
	if err := r.db.WithContext(ctx).Table("menus").Create(m).Error; err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return 0, ErrNameDuplicate
		}
		return 0, err
	}
	return m.MenuID, nil
}

func (r *menuRepoGorm) Update(ctx context.Context, m *model.Menu) error {
	// 不允许直接改主键；允许改 parent_id/name/其他字段
	res := r.db.WithContext(ctx).Table("menus").
		Where("menu_id = ?", m.MenuID).
		Updates(map[string]any{
			"parent_id": m.ParentID,
			"name":      m.Name,
			"path":      m.Path,
			"icon":      m.Icon,
			"sort":      m.Sort,
			"status":    m.Status,
			"component": m.Component,
			"visible":   m.Visible,
		})
	if res.Error != nil {
		if strings.Contains(strings.ToLower(res.Error.Error()), "duplicate") {
			return ErrNameDuplicate
		}
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *menuRepoGorm) Delete(ctx context.Context, menuID uint64) error {
	// 先检查是否有子节点
	var cnt int64
	if err := r.db.WithContext(ctx).Table("menus").
		Where("parent_id = ?", menuID).Count(&cnt).Error; err != nil {
		return err
	}
	if cnt > 0 {
		return ErrHasChildren
	}
	res := r.db.WithContext(ctx).Table("menus").Where("menu_id = ?", menuID).Delete(nil)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return errors.New("menu not found")
	}
	return nil
}
