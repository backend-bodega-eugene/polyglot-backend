package repo

import (
	"context"
	"database/sql"
	"errors"
	"eugene-go-starter/internal/model"
	"fmt"
	"strings"

	"github.com/jmoiron/sqlx"
)

type MenuMySQL struct{ DB *sqlx.DB }

func NewMenuRepoSQLX(db *sqlx.DB) MenuRepo { return &MenuMySQL{DB: db} }

func (r *MenuMySQL) ListAll(ctx context.Context) ([]model.Menu, error) {
	const q = `SELECT menu_id, parent_id, name, path, icon, sort, status
			   FROM menus WHERE status=1 ORDER BY sort ASC, menu_id ASC`
	var out []model.Menu
	return out, r.DB.SelectContext(ctx, &out, q)
}

func (r *MenuMySQL) ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error) {
	const q = `
SELECT m.menu_id, m.parent_id, m.name, m.path, m.icon, m.sort, m.status
FROM user_menu um
JOIN menus m ON um.menu_id = m.menu_id
WHERE um.user_id=? AND m.status=1
ORDER BY m.sort ASC, m.menu_id ASC`
	var out []model.Menu
	return out, r.DB.SelectContext(ctx, &out, q, userID)
}

func (r *MenuMySQL) exists(ctx context.Context, id uint64) (bool, error) {
	var ok int
	if err := r.DB.QueryRowContext(ctx, `SELECT 1 FROM menus WHERE menu_id=?`, id).Scan(&ok); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func (r *MenuMySQL) parentExistsOrZero(ctx context.Context, pid uint64) (bool, error) {
	if pid == 0 {
		return true, nil
	}
	return r.exists(ctx, pid)
}

func (r *MenuMySQL) nameDupUnderParent(ctx context.Context, parentID uint64, name string, excludeID uint64) (bool, error) {
	q := `SELECT 1 FROM menus WHERE parent_id=? AND name=?`
	var args any = nil
	args = []any{parentID, name}
	if excludeID > 0 {
		q += ` AND menu_id<>?`
		args = []any{parentID, name, excludeID}
	}
	var ok int
	err := r.DB.QueryRowContext(ctx, q, args.([]any)...).Scan(&ok)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// 用递归 CTE 判断 newParent 是否在 curr 的后代里（MySQL 8+/9+ 支持）
func (r *MenuMySQL) isDescendant(ctx context.Context, currID, newParentID uint64) (bool, error) {
	if newParentID == 0 {
		return false, nil
	}
	const cte = `
WITH RECURSIVE subtree AS (
  SELECT menu_id
  FROM menus
  WHERE parent_id = ?
  UNION ALL
  SELECT m.menu_id
  FROM menus m
  INNER JOIN subtree s ON m.parent_id = s.menu_id
)
SELECT 1 FROM subtree WHERE menu_id = ? LIMIT 1;
`
	var v int
	err := r.DB.QueryRowContext(ctx, cte, currID, newParentID).Scan(&v)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return false, nil
		}
		return false, err
	}
	return v == 1, nil
}

// ---------- CRUD ----------

// Create(ctx, m) -> (newID, error)
func (r *MenuMySQL) Create(ctx context.Context, m *model.Menu) (uint64, error) {
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
		return 0, ErrParentNotFound
	}
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, 0); err != nil {
		return 0, err
	} else if dup {
		return 0, ErrNameDuplicate
	}

	// 规范一下可空字段
	m.Path = trimPtr(m.Path)
	m.Component = trimPtr(m.Component)
	m.Icon = trimPtr(m.Icon)

	res, err := r.DB.ExecContext(ctx, `
INSERT INTO menus (parent_id, name, path, component, icon, visible, sort, status)
VALUES (?,?,?,?,?,?,?,?)`,
		m.ParentID, m.Name, m.Path, m.Component, m.Icon, m.Visible, m.Sort, m.Status,
	)
	if err != nil {
		return 0, err
	}
	id, _ := res.LastInsertId()
	return uint64(id), nil
}

// Update(ctx, m) -> error
// 允许修改 parent_id，但要做防环校验 & 同父同名校验
func (r *MenuMySQL) Update(ctx context.Context, m *model.Menu) error {
	if m == nil || m.MenuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	// 先确认存在
	if ok, err := r.exists(ctx, m.MenuID); err != nil {
		return err
	} else if !ok {
		return ErrNotFound
	}

	m.Name = strings.TrimSpace(m.Name)
	if m.Name == "" {
		return fmt.Errorf("name required")
	}
	if m.ParentID == m.MenuID {
		return ErrParentIsSelf
	}
	// 父级存在 & 防环
	if ok, err := r.parentExistsOrZero(ctx, m.ParentID); err != nil {
		return err
	} else if !ok {
		return ErrParentNotFound
	}
	if desc, err := r.isDescendant(ctx, m.MenuID, m.ParentID); err != nil {
		return err
	} else if desc {
		return ErrParentIsDescendant
	}
	// 同父同名
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, m.MenuID); err != nil {
		return err
	} else if dup {
		return ErrNameDuplicate
	}

	// 规范可空
	m.Path = trimPtr(m.Path)
	m.Component = trimPtr(m.Component)
	m.Icon = trimPtr(m.Icon)

	_, err := r.DB.ExecContext(ctx, `
UPDATE menus
SET parent_id=?, name=?, path=?, component=?, icon=?, visible=?, sort=?, status=?
WHERE menu_id=?`,
		m.ParentID, m.Name, m.Path, m.Component, m.Icon, m.Visible, m.Sort, m.Status, m.MenuID,
	)
	return err
}

// Delete(ctx, id) -> error
// 默认不级联：若有子节点则拒绝
func (r *MenuMySQL) Delete(ctx context.Context, menuID uint64) error {
	if menuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	// 是否存在
	if ok, err := r.exists(ctx, menuID); err != nil {
		return err
	} else if !ok {
		return ErrNotFound
	}
	// 有子节点则拒绝
	var cnt int64
	if err := r.DB.QueryRowContext(ctx, `SELECT COUNT(1) FROM menus WHERE parent_id=?`, menuID).Scan(&cnt); err != nil {
		return err
	}
	if cnt > 0 {
		return ErrHasChildren
	}
	_, err := r.DB.ExecContext(ctx, `DELETE FROM menus WHERE menu_id=?`, menuID)
	return err
}
func trimPtr(s *string) *string {
	if s == nil {
		return nil
	}
	t := strings.TrimSpace(*s)
	return &t
}
