package auth

import (
	"errors"
	"time"

	driver "github.com/go-sql-driver/mysql"
	"gorm.io/gorm"
)

var (
	ErrAccountExists = errors.New("account already exists")
	ErrUserNotFound  = errors.New("user not found")
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

func (r *Repository) Create(user *User) error {
	if err := r.db.Create(user).Error; err != nil {
		var mysqlError *driver.MySQLError
		if errors.As(err, &mysqlError) && mysqlError.Number == 1062 {
			return ErrAccountExists
		}
		return err
	}

	return nil
}

func (r *Repository) FindByAccount(phone, email *string) (User, error) {
	var user User
	query := r.db
	if phone != nil {
		query = query.Where("phone = ?", *phone)
	} else {
		query = query.Where("email = ?", *email)
	}

	if err := query.First(&user).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return User{}, ErrUserNotFound
		}
		return User{}, err
	}

	return user, nil
}

func (r *Repository) UpdateLastLogin(userID uint64, loggedInAt time.Time) error {
	return r.db.Model(&User{}).
		Where("id = ?", userID).
		Update("last_login_at", loggedInAt).
		Error
}
