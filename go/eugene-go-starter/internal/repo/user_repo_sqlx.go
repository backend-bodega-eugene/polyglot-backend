package repo

import (
	"context"
	"database/sql"
	"time"

	"eugene-go-starter/internal/model"

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
