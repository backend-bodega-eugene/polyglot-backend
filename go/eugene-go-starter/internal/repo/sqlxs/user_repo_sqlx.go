package sqlxs

import (
	"context"
	"database/sql"
	"errors"
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"fmt"
	"strings"
	"time"

	"golang.org/x/crypto/bcrypt"

	"github.com/jmoiron/sqlx"
)

const userColumns = `user_id, site_id, username, password_hash, status, last_login_at, updated_at`

type userRepoSQLX struct{ db *sqlx.DB }

func NewUserRepoSQLX(db *sqlx.DB) repo.UserRepo { return &userRepoSQLX{db: db} }

func (r *userRepoSQLX) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	var u model.User
	err := r.db.GetContext(ctx, &u,
		`SELECT user_id, site_id, username, password_hash, status, last_login_at, updated_at
		   FROM users WHERE username=? LIMIT 1`, username)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	return &u, err
}

func (r *userRepoSQLX) Create(ctx context.Context, u *model.User) error {
	_, err := r.db.NamedExecContext(ctx, `
INSERT INTO users (site_id, username, password_hash, status, updated_at)
VALUES (:site_id, :username, :password_hash, :status, CURRENT_TIMESTAMP)`, u)
	return err
}

func (r *userRepoSQLX) UpdateLoginAt(ctx context.Context, userID uint64) error {
	_, err := r.db.ExecContext(ctx,
		`UPDATE users SET last_login_at=?, updated_at=CURRENT_TIMESTAMP WHERE user_id=?`,
		time.Now(), userID)
	return err
}
func (r *userRepoSQLX) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	// 1) 拉当前哈希
	var u model.User
	if err := r.db.GetContext(ctx, &u,
		`SELECT user_id, password_hash FROM users WHERE user_id = ? LIMIT 1`, userID); err != nil {
		return repo.ErrUserNotFound // 可能是 sql.ErrNoRows
	}

	// 2) 校验旧密码
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return repo.ErrOldPasswordIncorrect // 上层可统一成“旧密码不正确”
	}

	// 3) 生成新哈希
	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return repo.ErrHashWrong
	}

	// 4) 写入新哈希并更新时间（用 DB 时间）
	res, err := r.db.ExecContext(ctx,
		`UPDATE users SET password_hash = ?, updated_at = NOW() WHERE user_id = ?`,
		string(hash), userID)
	if err != nil {
		return repo.ErrUpdateFailed
	}
	if rows, _ := res.RowsAffected(); rows == 0 {
		return sql.ErrNoRows
	}
	return nil
}

func (r *userRepoSQLX) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	_, err := r.db.ExecContext(ctx,
		`UPDATE users SET status=? WHERE user_id=?`,
		status, userID)
	if err != nil {
		return repo.ErrUpdateStatusFailed
	}
	return nil
}

// 统一的扫描函数
func scanUser(row interface{ Scan(dest ...any) error }) (*model.User, error) {
	var u model.User
	var last sql.NullTime
	if err := row.Scan(&u.UserID, &u.SiteID, &u.Username, &u.PasswordHash, &u.Status, &last, &u.UpdatedAt); err != nil {
		return nil, err
	}
	if last.Valid {
		t := last.Time
		u.LastLoginAt = &t
	}
	return &u, nil
}

// ===== 下面是你“只需要生成”的 4 个实现 =====

// ListAll: 基于分页与关键词（匹配 username），不返回 total（按你签名）
func (r *userRepoSQLX) ListAll(ctx context.Context, page, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	offset := (page - 1) * size

	where := make([]string, 0, 2)
	args := make([]any, 0, 2)

	kw := strings.TrimSpace(keywords)
	if kw != "" {
		where = append(where, "username LIKE ?")
		args = append(args, "%"+kw+"%")
	}
	cond := ""
	if len(where) > 0 {
		cond = "WHERE " + strings.Join(where, " AND ")
	}

	q := fmt.Sprintf(`SELECT %s FROM users %s ORDER BY user_id DESC LIMIT ? OFFSET ?`, userColumns, cond)
	args = append(args, size, offset)

	rows, err := r.db.QueryContext(ctx, q, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	users := make([]model.User, 0, size)
	for rows.Next() {
		u, err := scanUser(rows)
		if err != nil {
			return nil, err
		}
		users = append(users, *u) // 用值切片，GC 友好
	}
	return users, rows.Err()
}

// GetByID: 按 user_id 查询
func (r *userRepoSQLX) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	const q = `SELECT ` + userColumns + ` FROM users WHERE user_id=? LIMIT 1`
	row := r.db.QueryRowContext(ctx, q, userID)
	u, err := scanUser(row)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, repo.ErrNotFound
		}
		return nil, err
	}
	return u, nil
}

// DdleteByID: 删除（保留你的方法名，建议后续改为 DeleteByID）
func (r *userRepoSQLX) DdleteByID(ctx context.Context, userID uint64) error {
	const q = `DELETE FROM users WHERE user_id=?`
	res, err := r.db.ExecContext(ctx, q, userID)
	if err != nil {
		return err
	}
	if n, _ := res.RowsAffected(); n == 0 {
		return repo.ErrNotFound
	}
	return nil
}

// UpdateByID: 允许修改 username 与 status（password 请走 UpdatePassword）
func (r *userRepoSQLX) UpdateByID(ctx context.Context, user *model.User) error {
	// 这里不改 site_id / password_hash / last_login_at / updated_at 由库端维护 NOW()
	const q = `UPDATE users SET username=?, status=?, updated_at=NOW() WHERE user_id=?`
	res, err := r.db.ExecContext(ctx, q, user.Username, user.Status, user.UserID)
	if err != nil {
		// 唯一索引 uk_user_site（site_id, username）冲突时，MySQL 会抛 duplicate
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return err
	}
	if n, _ := res.RowsAffected(); n == 0 {
		return repo.ErrNotFound
	}
	return nil
}

// internal/repo/mysql/user_mysql.go
func (r *userRepoSQLX) AddUser(ctx context.Context, user *model.User) error {
	const q = `INSERT INTO users (site_id, username, password_hash, status, updated_at)
               VALUES (?, ?, ?, ?, NOW())`
	_, err := r.db.ExecContext(ctx, q,
		user.SiteID,
		user.Username,
		user.PasswordHash,
		int(user.Status),
	)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return err
	}
	return nil
}
