// internal/repo/mysql/user_repo_gorm.go
package gorms

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"strings"
	"time"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// 保持与 sqlx 版本一致的仓库实现，但基于 GORM
type userRepoGORMSQLX struct{ db *gorm.DB }

func NewUserRepoGORMSQLX(db *gorm.DB) repo.UserRepo { return &userRepoGORMSQLX{db: db} }

// ===== 账号登录&基础信息 =====

func (r *userRepoGORMSQLX) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	var u model.User
	// 与 sqlx 版本一致：按 username 唯一找一条
	err := r.db.WithContext(ctx).
		// 明确列，避免意外的零值覆盖
		Select("user_id", "site_id", "username", "password_hash", "status", "last_login_at", "updated_at").
		Where("username = ?", username).
		Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil // 与 sqlx 行为保持一致：未找到返回 nil, nil
	}
	return &u, err
}

func (r *userRepoGORMSQLX) Create(ctx context.Context, u *model.User) error {
	// updated_at 走数据库 NOW()，与 sqlx 版本一致
	return r.db.WithContext(ctx).Clauses(clause.OnConflict{DoNothing: true}).Exec(
		r.db.WithContext(ctx).ToSQL(func(tx *gorm.DB) *gorm.DB {
			return tx.Exec(
				`INSERT INTO users (site_id, username, password_hash, status, updated_at)
				 VALUES (?, ?, ?, ?, NOW())`,
				u.SiteID, u.Username, u.PasswordHash, u.Status,
			)
		}),
	).Error
}

func (r *userRepoGORMSQLX) UpdateLoginAt(ctx context.Context, userID uint64) error {
	return r.db.WithContext(ctx).Exec(
		`UPDATE users SET last_login_at = ?, updated_at = NOW() WHERE user_id = ?`,
		time.Now(), userID,
	).Error
}

func (r *userRepoGORMSQLX) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	// 1) 拉当前哈希
	var u model.User
	if err := r.db.WithContext(ctx).
		Select("user_id", "password_hash").
		Where("user_id = ?", userID).
		Take(&u).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return repo.ErrUserNotFound
		}
		return err
	}

	// 2) 校验旧密码
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return repo.ErrOldPasswordIncorrect
	}

	// 3) 生成新哈希
	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return repo.ErrHashWrong
	}

	// 4) 更新
	res := r.db.WithContext(ctx).Exec(
		`UPDATE users SET password_hash = ?, updated_at = NOW() WHERE user_id = ?`,
		string(hash), userID,
	)
	if res.Error != nil {
		return repo.ErrUpdateFailed
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *userRepoGORMSQLX) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	res := r.db.WithContext(ctx).Exec(
		`UPDATE users SET status = ? WHERE user_id = ?`,
		status, userID,
	)
	if res.Error != nil {
		return repo.ErrUpdateStatusFailed
	}
	return nil
}

// ===== 你新增的 5 个 CRUD 方法（签名保持一致）=====

func (r *userRepoGORMSQLX) ListAll(ctx context.Context, page int, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	offset := (page - 1) * size

	tx := r.db.WithContext(ctx).
		Select("user_id", "site_id", "username", "password_hash", "status", "last_login_at", "updated_at")

	if kw := strings.TrimSpace(keywords); kw != "" {
		tx = tx.Where("username LIKE ?", "%"+kw+"%")
	}

	var users []model.User
	if err := tx.Order("user_id DESC").Limit(size).Offset(offset).Find(&users).Error; err != nil {
		return nil, err
	}
	return users, nil
}

func (r *userRepoGORMSQLX) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	var u model.User
	err := r.db.WithContext(ctx).
		Select("user_id", "site_id", "username", "password_hash", "status", "last_login_at", "updated_at").
		Where("user_id = ?", userID).
		Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, repo.ErrNotFound
	}
	return &u, err
}

// 注意：保持方法名拼写为 DdleteByID 与你的接口一致
func (r *userRepoGORMSQLX) DdleteByID(ctx context.Context, userID uint64) error {
	res := r.db.WithContext(ctx).Delete(&model.User{}, userID)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoGORMSQLX) UpdateByID(ctx context.Context, user *model.User) error {
	// 与 sqlx 版保持一致：仅允许改 username / status，updated_at=NOW()
	res := r.db.WithContext(ctx).Exec(
		`UPDATE users SET username = ?, status = ?, updated_at = NOW() WHERE user_id = ?`,
		user.Username, user.Status, user.UserID,
	)
	if res.Error != nil {
		// 捕获唯一键冲突（如 uk_user_site），MySQL 一般包含 duplicate
		if strings.Contains(strings.ToLower(res.Error.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return res.Error
	}
	if res.RowsAffected == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoGORMSQLX) AddUser(ctx context.Context, user *model.User) error {
	res := r.db.WithContext(ctx).Exec(
		`INSERT INTO users (site_id, username, password_hash, status, updated_at)
		 VALUES (?, ?, ?, ?, NOW())`,
		user.SiteID, user.Username, user.PasswordHash, int(user.Status),
	)
	if res.Error != nil {
		if strings.Contains(strings.ToLower(res.Error.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return res.Error
	}
	return nil
}
