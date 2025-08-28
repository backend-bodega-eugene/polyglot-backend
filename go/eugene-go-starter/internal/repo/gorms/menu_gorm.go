package gorms

import (
	"context"
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"fmt"
	"strings"

	"gorm.io/gorm"
)

type MenuGORM struct{ DB *gorm.DB }

func NewMenuRepoGORM(db *gorm.DB) repo.MenuRepo { return &MenuGORM{DB: db} }

// 列出所有启用菜单（status=1）
func (r *MenuGORM) ListAll(ctx context.Context) ([]model.Menu, error) {
	var out []model.Menu
	err := r.DB.WithContext(ctx).
		Table("menus").
		Select("menu_id", "parent_id", "name", "path", "icon", "sort", "status").
		Where("status = ?", 1).
		Order("sort ASC, menu_id ASC").
		Find(&out).Error
	return out, err
}

// 列出某用户可见菜单（按排序）
func (r *MenuGORM) ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error) {
	var out []model.Menu
	err := r.DB.WithContext(ctx).
		Table("menus m").
		Select("m.menu_id", "m.parent_id", "m.name", "m.path", "m.icon", "m.sort", "m.status").
		Joins("JOIN user_menu um ON um.menu_id = m.menu_id").
		Where("um.user_id = ? AND m.status = 1", userID).
		Order("m.sort ASC, m.menu_id ASC").
		Find(&out).Error
	return out, err
}

// ---------- helpers ----------

func (r *MenuGORM) exists(ctx context.Context, id uint64) (bool, error) {
	var cnt int64
	if err := r.DB.WithContext(ctx).Table("menus").
		Where("menu_id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *MenuGORM) parentExistsOrZero(ctx context.Context, pid uint64) (bool, error) {
	if pid == 0 {
		return true, nil
	}
	return r.exists(ctx, pid)
}

func (r *MenuGORM) nameDupUnderParent(ctx context.Context, parentID uint64, name string, excludeID uint64) (bool, error) {
	q := r.DB.WithContext(ctx).Table("menus").
		Where("parent_id = ? AND name = ?", parentID, name)
	if excludeID > 0 {
		q = q.Where("menu_id <> ?", excludeID)
	}
	var cnt int64
	if err := q.Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

// isDescendant：判断 newParentID 是否在 currID 的“后代”中
// 纯 ORM 做一轮 BFS：迭代查 children，直到穷尽或命中 newParentID
func (r *MenuGORM) isDescendant(ctx context.Context, currID, newParentID uint64) (bool, error) {
	if newParentID == 0 {
		return false, nil
	}
	frontier := []uint64{currID}
	seen := make(map[uint64]struct{}, 16)
	for len(frontier) > 0 {
		// 拉取所有 frontier 的子节点
		var children []uint64
		if err := r.DB.WithContext(ctx).Table("menus").
			Where("parent_id IN ?", frontier).
			Pluck("menu_id", &children).Error; err != nil {
			return false, err
		}
		// 准备下一轮
		next := make([]uint64, 0, len(children))
		for _, id := range children {
			if id == newParentID {
				return true, nil
			}
			if _, ok := seen[id]; !ok {
				seen[id] = struct{}{}
				next = append(next, id)
			}
		}
		frontier = next
	}
	return false, nil
}

// ---------- CRUD ----------

func (r *MenuGORM) Create(ctx context.Context, m *model.Menu) (uint64, error) {
	if m == nil {
		return 0, fmt.Errorf("nil menu")
	}
	m.Name = strings.TrimSpace(m.Name)
	if m.Name == "" {
		return 0, fmt.Errorf("name required")
	}
	if ok, err := r.parentExistsOrZero(ctx, m.ParentID); err != nil {
		return 0, err
	} else if !ok {
		return 0, repo.ErrParentNotFound
	}
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, 0); err != nil {
		return 0, err
	} else if dup {
		return 0, repo.ErrNameDuplicate
	}

	m.Path = repo.TrimPtr(m.Path)
	m.Component = repo.TrimPtr(m.Component)
	m.Icon = repo.TrimPtr(m.Icon)

	// Create 会回填自增主键到 m.MenuID
	if err := r.DB.WithContext(ctx).Table("menus").Create(m).Error; err != nil {
		return 0, err
	}
	return m.MenuID, nil
}

func (r *MenuGORM) Update(ctx context.Context, m *model.Menu) error {
	if m == nil || m.MenuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	if ok, err := r.exists(ctx, m.MenuID); err != nil {
		return err
	} else if !ok {
		return repo.ErrNotFound
	}

	m.Name = strings.TrimSpace(m.Name)
	if m.Name == "" {
		return fmt.Errorf("name required")
	}
	if m.ParentID == m.MenuID {
		return repo.ErrParentIsSelf
	}
	if ok, err := r.parentExistsOrZero(ctx, m.ParentID); err != nil {
		return err
	} else if !ok {
		return repo.ErrParentNotFound
	}
	if desc, err := r.isDescendant(ctx, m.MenuID, m.ParentID); err != nil {
		return err
	} else if desc {
		return repo.ErrParentIsDescendant
	}
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, m.MenuID); err != nil {
		return err
	} else if dup {
		return repo.ErrNameDuplicate
	}

	m.Path = repo.TrimPtr(m.Path)
	m.Component = repo.TrimPtr(m.Component)
	m.Icon = repo.TrimPtr(m.Icon)

	return r.DB.WithContext(ctx).Table("menus").
		Where("menu_id = ?", m.MenuID).
		Updates(map[string]any{
			"parent_id": m.ParentID,
			"name":      m.Name,
			"path":      m.Path,
			"component": m.Component,
			"icon":      m.Icon,
			"visible":   m.Visible,
			"sort":      m.Sort,
			"status":    m.Status,
		}).Error
}

func (r *MenuGORM) Delete(ctx context.Context, menuID uint64) error {
	if menuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	if ok, err := r.exists(ctx, menuID); err != nil {
		return err
	} else if !ok {
		return repo.ErrNotFound
	}
	var cnt int64
	if err := r.DB.WithContext(ctx).Table("menus").
		Where("parent_id = ?", menuID).Count(&cnt).Error; err != nil {
		return err
	}
	if cnt > 0 {
		return repo.ErrHasChildren
	}
	// 直接按主键删除
	res := r.DB.WithContext(ctx).Table("menus").Where("menu_id = ?", menuID).Delete(&model.Menu{})
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return repo.ErrNotFound
	}
	return nil
}
