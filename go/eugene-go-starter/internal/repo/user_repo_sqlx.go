package repo

import (
	"context"
	"database/sql"
	"errors"
	"eugene-go-starter/internal/model"
	"time"

	"golang.org/x/crypto/bcrypt"

	"github.com/jmoiron/sqlx"
)

type userRepoSQLX struct{ db *sqlx.DB }

func NewUserRepoSQLX(db *sqlx.DB) UserRepo { return &userRepoSQLX{db: db} }

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
		return ErrUserNotFound // 可能是 sql.ErrNoRows
	}

	// 2) 校验旧密码
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return ErrOldPasswordIncorrect // 上层可统一成“旧密码不正确”
	}

	// 3) 生成新哈希
	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return ErrHashWrong
	}

	// 4) 写入新哈希并更新时间（用 DB 时间）
	res, err := r.db.ExecContext(ctx,
		`UPDATE users SET password_hash = ?, updated_at = NOW() WHERE user_id = ?`,
		string(hash), userID)
	if err != nil {
		return ErrUpdateFailed
	}
	if rows, _ := res.RowsAffected(); rows == 0 {
		return sql.ErrNoRows
	}
	return nil
}

var (
	ErrUserNotFound         = errors.New("user not found")
	ErrOldPasswordIncorrect = errors.New("old password incorrect")
	ErrUpdateFailed         = errors.New("update failed")
	ErrHashWrong            = errors.New("hash generate failed")
	ErrUpdateStatusFailed   = errors.New("update status failed")
)

func (r *userRepoSQLX) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	_, err := r.db.ExecContext(ctx,
		`UPDATE users SET status=? WHERE user_id=?`,
		status, userID)
	if (err != nil) {
		return ErrUpdateStatusFailed
	}
	return nil
}
