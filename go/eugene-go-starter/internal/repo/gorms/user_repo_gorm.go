// internal/repo/mysql/user_repo_gorm.go
package gorms

import (
	"context"
	"errors"
	"strings"
	"time"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

// 纯 ORM 写法，不手写 SQL；统一走 Table("users")，不依赖 Model 的 gorm tag
type userRepoGORM struct{ db *gorm.DB }

func NewUserRepoGORM(db *gorm.DB) repo.UserRepo { return &userRepoGORM{db: db} }

// ============ 登录&基础信息 ============

// FindBySiteAndUsername: 命中返回 *User，未命中返回 (nil, nil)
func (r *userRepoGORM) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	var u model.User
	err := r.db.WithContext(ctx).
		Table("users").
		// 明确选择列，避免零值覆盖
		Select("user_id", "site_id", "username", "password_hash", "status", "last_login_at", "updated_at").
		Where("username = ?", username).
		Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	return &u, err
}

func (r *userRepoGORM) Create(ctx context.Context, u *model.User) error {
	// 与 sqlx 行为对齐：唯一键冲突 -> ErrConflict
	err := r.db.WithContext(ctx).
		Table("users").
		Create(&u).Error
	if err != nil {
		if isDuplicate(err) {
			return repo.ErrConflict
		}
		return err
	}
	return nil
}

func (r *userRepoGORM) UpdateLoginAt(ctx context.Context, userID uint64) error {
	now := time.Now()
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Updates(map[string]any{
			"last_login_at": now,
			"updated_at":    now,
		})
	if res.Error != nil {
		return res.Error
	}
	// 不强制 rows>0；登录更新可以允许无变化
	return nil
}

func (r *userRepoGORM) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	// 1) 拉当前哈希
	var u model.User
	if err := r.db.WithContext(ctx).
		Table("users").
		Select("user_id", "password_hash").
		Where("user_id = ?", userID).
		Take(&u).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return repo.ErrUserNotFound
		}
		return err
	}

	// 2) 比对旧密码
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return repo.ErrOldPasswordIncorrect
	}

	// 3) 生成新哈希
	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return repo.ErrHashWrong
	}

	// 4) 更新
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Updates(map[string]any{
			"password_hash": string(hash),
			"updated_at":    time.Now(),
		})
	if res.Error != nil {
		return repo.ErrUpdateFailed
	}
	if res.RowsAffected == 0 {
		return repo.ErrUpdateFailed
	}
	return nil
}

func (r *userRepoGORM) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Update("status", status)
	if res.Error != nil {
		return repo.ErrUpdateStatusFailed
	}
	return nil
}

// ============ 你新增的 CRUD 方法 ============

func (r *userRepoGORM) ListAll(ctx context.Context, page int, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	offset := (page - 1) * size

	tx := r.db.WithContext(ctx).Table("users").
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

func (r *userRepoGORM) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	var u model.User
	err := r.db.WithContext(ctx).Table("users").
		Select("user_id", "site_id", "username", "password_hash", "status", "last_login_at", "updated_at").
		Where("user_id = ?", userID).
		Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, repo.ErrNotFound
	}
	return &u, err
}

// 注意：方法名保持与你接口一致（含拼写）
func (r *userRepoGORM) DdleteByID(ctx context.Context, userID uint64) error {
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Delete(&model.User{})
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoGORM) UpdateByID(ctx context.Context, user *model.User) error {
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", user.UserID).
		Updates(map[string]any{
			"username":   user.Username,
			"status":     user.Status,
			"updated_at": time.Now(),
		})
	if res.Error != nil {
		// MySQL 唯一键冲突（如 uk_user_site）转 ErrConflict
		if isDuplicate(res.Error) {
			return repo.ErrConflict
		}
		return res.Error
	}
	if res.RowsAffected == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoGORM) AddUser(ctx context.Context, user *model.User) error {
	err := r.db.WithContext(ctx).Table("users").Create(&user).Error
	if err != nil {
		if isDuplicate(err) {
			return repo.ErrConflict
		}
		return err
	}
	return nil
}

// ---- 小工具：判断唯一键冲突（MySQL 常见 "Duplicate entry" 文案）----
func isDuplicate(err error) bool {
	if err == nil {
		return false
	}
	s := strings.ToLower(err.Error())
	return strings.Contains(s, "duplicate")
}
