package repo

import (
	"context"
	"database/sql"
	"time"

	"github.com/jmoiron/sqlx"
	"eugene-go-starter/internal/model"
)

type userRepoSQLX struct{ db *sqlx.DB }

func NewUserRepoSQLX(db *sqlx.DB) UserRepo { return &userRepoSQLX{db: db} }

func (r *userRepoSQLX) FindBySiteAndUsername(ctx context.Context, siteID uint64, username string) (*model.User, error) {
	var u model.User
	err := r.db.GetContext(ctx, &u,
		`SELECT user_id, site_id, username, password_hash, status, last_login_at, updated_at
		   FROM users WHERE site_id=? AND username=? LIMIT 1`, siteID, username)
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
