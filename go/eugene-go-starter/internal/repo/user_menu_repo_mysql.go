// repo/permission_mysql.go
package repo

import (
	"context"
	"database/sql"
	"errors"
	"strconv"

	"eugene-go-starter/internal/model"

	"github.com/jmoiron/sqlx"
)

type PermissionMySQL struct {
	DB *sqlx.DB
}

func NewPermissionMySQL(db *sqlx.DB) UserMenuRepo { return &PermissionMySQL{DB: db} }

// 创建“多条”
func (r *PermissionMySQL) CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error {
	// 注意：你原型这里的注释和参数有点反了（单复数），我保留签名，做单条插入
	const q = `INSERT INTO user_menu (user_id, menu_id) VALUES (?, ?)`
	_, err := r.DB.ExecContext(ctx, q, userMenus.UserId, userMenus.MenuId)
	return err
}

// 创建“单条”（你注释说可以多个，所以这里支持批量）
func (r *PermissionMySQL) CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error {
	if len(userMenus) == 0 {
		return nil
	}
	// 批量插入
	q := "INSERT INTO user_menu (user_id, menu_id) VALUES "
	vals := make([]string, 0, len(userMenus))
	args := make([]any, 0, len(userMenus)*2)
	for _, um := range userMenus {
		vals = append(vals, "(?, ?)")
		args = append(args, um.UserId, um.MenuId)
	}
	q += join(vals, ",")
	_, err := r.DB.ExecContext(ctx, q, args...)
	return err
}

func (r *PermissionMySQL) FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error) {
	const q = `SELECT user_menu_id, user_id, menu_id FROM user_menu WHERE user_id=? ORDER BY menu_id`
	var out []model.UserMenu
	_, err := strconv.ParseUint(userId, 10, 64) // 校验一下
	if err != nil {
		return nil, errors.New("invalid userId")
	}
	err = r.DB.SelectContext(ctx, &out, q, userId)
	return out, err
}

func (r *PermissionMySQL) UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error {
	// 权限关联一般不更新，通常“删旧插新”；给个最小实现：把某条的 menu_id 改一下
	const q = `UPDATE user_menu SET menu_id=? WHERE user_id=? AND user_menu_id=?`
	_, err := r.DB.ExecContext(ctx, q, userMenu.MenuId, userId, userMenu.UserMenuId)
	return err
}

func (r *PermissionMySQL) DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error {
	const q = `DELETE FROM user_menu WHERE user_menu_id=?`
	_, err := r.DB.ExecContext(ctx, q, userMenuId)
	return err
}

func (r *PermissionMySQL) DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error {
	if len(userMenuIds) == 0 {
		return nil
	}
	qs, args, _ := sqlx.In(`DELETE FROM user_menu WHERE user_menu_id IN (?)`, userMenuIds)
	qs = r.DB.Rebind(qs)
	_, err := r.DB.ExecContext(ctx, qs, args...)
	return err
}

// ✅ 关键：saveUserMenus —— 先删后插（只存叶子，幂等）
func (r *PermissionMySQL) SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error {
	uid, err := strconv.ParseUint(userId, 10, 64)
	if err != nil || uid == 0 {
		return errors.New("invalid userId")
	}

	tx, err := r.DB.BeginTxx(ctx, &sql.TxOptions{Isolation: sql.LevelReadCommitted})
	if err != nil {
		return err
	}
	defer func() { _ = tx.Rollback() }()

	// 可选：过滤有效菜单（status=1）
	validIds, err := filterValidMenuIDsTx(ctx, tx, menuIds)
	if err != nil {
		return err
	}

	if _, err := tx.ExecContext(ctx, `DELETE FROM user_menu WHERE user_id=?`, uid); err != nil {
		return err
	}
	if len(validIds) > 0 {
		q := "INSERT INTO user_menu (user_id, menu_id) VALUES "
		vals := make([]string, 0, len(validIds))
		args := make([]any, 0, len(validIds)*2)
		for _, mid := range validIds {
			vals = append(vals, "(?, ?)")
			args = append(args, uid, mid)
		}
		q += join(vals, ",")
		if _, err := tx.ExecContext(ctx, q, args...); err != nil {
			return err
		}
	}

	return tx.Commit()
}

// —— 小工具 —— //
func filterValidMenuIDsTx(ctx context.Context, tx *sqlx.Tx, ids []uint64) ([]uint64, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	qs, args, _ := sqlx.In(`SELECT menu_id FROM menus WHERE menu_id IN (?) AND status=1`, ids)
	qs = tx.Rebind(qs)
	var out []uint64
	if err := tx.SelectContext(ctx, &out, qs, args...); err != nil {
		return nil, err
	}
	return out, nil
}
func join(arr []string, sep string) string {
	if len(arr) == 0 {
		return ""
	}
	s := arr[0]
	for i := 1; i < len(arr); i++ {
		s += sep + arr[i]
	}
	return s
}
