package repo

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"strings"
	"time"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

// GORM implementation of UserRepo
type userRepoGorm struct{ db *gorm.DB }

func NewUserRepoGorm(db *gorm.DB) UserRepo { return &userRepoGorm{db: db} }

func (r *userRepoGorm) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	var u model.User
	tx := r.db.WithContext(ctx).Table("users").
		Select("user_id, site_id, username, password_hash, status, last_login_at, updated_at").
		Where("username = ?", username).
		Limit(1).Take(&u)
	if errors.Is(tx.Error, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	return &u, tx.Error
}

func (r *userRepoGorm) Create(ctx context.Context, u *model.User) error {
	// updated_at 由 DB 维护 CURRENT_TIMESTAMP/NOW()，这里也可手动赋值以兼容不同配置
	u.UpdatedAt = time.Now()
	return r.db.WithContext(ctx).Table("users").Create(u).Error
}

func (r *userRepoGorm) UpdateLoginAt(ctx context.Context, userID uint64) error {
	return r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Updates(map[string]any{
			"last_login_at": time.Now(),
			"updated_at":    gorm.Expr("NOW()"),
		}).Error
}

func (r *userRepoGorm) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	var u model.User
	if err := r.db.WithContext(ctx).Table("users").
		Select("user_id, password_hash").
		Where("user_id = ?", userID).Take(&u).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return ErrUserNotFound
		}
		return err
	}

	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return ErrOldPasswordIncorrect
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return ErrHashWrong
	}

	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Updates(map[string]any{
			"password_hash": string(hash),
			"updated_at":    gorm.Expr("NOW()"),
		})
	if res.Error != nil {
		return ErrUpdateFailed
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *userRepoGorm) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", userID).
		Update("status", status)
	if res.Error != nil {
		return ErrUpdateStatusFailed
	}
	if res.RowsAffected == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoGorm) ListAll(ctx context.Context, page, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	offset := (page - 1) * size

	db := r.db.WithContext(ctx).Table("users").
		Select("user_id, site_id, username, password_hash, status, last_login_at, updated_at").
		Order("user_id DESC").
		Limit(size).Offset(offset)

	kw := strings.TrimSpace(keywords)
	if kw != "" {
		db = db.Where("username LIKE ?", "%"+kw+"%")
	}

	var users []model.User
	if err := db.Find(&users).Error; err != nil {
		return nil, err
	}
	return users, nil
}

func (r *userRepoGorm) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	var u model.User
	err := r.db.WithContext(ctx).Table("users").
		Select("user_id, site_id, username, password_hash, status, last_login_at, updated_at").
		Where("user_id = ?", userID).Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, ErrNotFound
	}
	return &u, err
}

func (r *userRepoGorm) DdleteByID(ctx context.Context, userID uint64) error { // 保留方法名
	res := r.db.WithContext(ctx).Table("users").Where("user_id = ?", userID).Delete(nil)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoGorm) UpdateByID(ctx context.Context, user *model.User) error {
	res := r.db.WithContext(ctx).Table("users").
		Where("user_id = ?", user.UserID).
		Updates(map[string]any{
			"username":   user.Username,
			"status":     user.Status,
			"updated_at": gorm.Expr("NOW()"),
		})
	if res.Error != nil {
		if strings.Contains(strings.ToLower(res.Error.Error()), "duplicate") {
			return ErrConflict
		}
		return res.Error
	}
	if res.RowsAffected == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoGorm) AddUser(ctx context.Context, user *model.User) error {
	user.UpdatedAt = time.Now()
	if err := r.db.WithContext(ctx).Table("users").Create(user).Error; err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return ErrConflict
		}
		return err
	}
	return nil
}
