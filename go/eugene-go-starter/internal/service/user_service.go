// service/user_service.go
package service

import (
	"context"
	"errors"
	"strings"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"

	"golang.org/x/crypto/bcrypt"
)

// 直接实现版 Service（无接口）
type UserService struct {
	users repo.UserRepo
}

func NewUserService(r repo.UserRepo) *UserService {
	return &UserService{users: r}
}

// 前端允许修改的字段（密码仍走你已有的 UpdatePassword）
type UpdateUserReq struct {
	UserID   uint64           `json:"userId"`
	Username string           `json:"username"`
	Status   model.UserStatus `json:"status"` // 0/1
}

// 列表查询（当前仓储签名不返回 total；如需 total 可升级仓储）
func (s *UserService) ListUsers(ctx context.Context, page, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	kw := strings.TrimSpace(keywords)
	return s.users.ListAll(ctx, page, size, kw)
}

// 详情
func (s *UserService) GetUser(ctx context.Context, userID uint64) (*model.User, error) {
	if userID == 0 {
		return nil, repo.ErrNotFound
	}
	return s.users.GetByID(ctx, userID)
}

// 更新（仅 username/status；密码请走 UpdatePassword）
func (s *UserService) UpdateUser(ctx context.Context, in UpdateUserReq) error {
	if in.UserID == 0 {
		return errors.New("userId required")
	}
	in.Username = strings.TrimSpace(in.Username)
	if in.Username == "" {
		return errors.New("username required")
	}
	if in.Status != 0 && in.Status != 1 {
		return errors.New("status must be 0 or 1")
	}

	// 读一遍确保存在（也可直接 UPDATE 后看 RowsAffected）
	u, err := s.users.GetByID(ctx, in.UserID)
	if err != nil {
		return err
	}
	u.Username = in.Username
	u.Status = in.Status

	return s.users.UpdateByID(ctx, u)
}

// 删除
func (s *UserService) DeleteUser(ctx context.Context, userID uint64) error {
	if userID == 0 {
		return repo.ErrNotFound
	}
	// 这里可加业务保护：禁止删除自己/超级管理员等
	return s.users.DdleteByID(ctx, userID) // 沿用你当前方法名（后续可更名为 DeleteByID）
}

// internal/service/user_service.go

// 请求 DTO
type CreateUserReq struct {
	SiteID   uint64           `json:"siteId"`
	Username string           `json:"username"`
	Password string           `json:"password"` // plain text，内部转 hash
	Status   model.UserStatus `json:"status"`
}

func (s *UserService) AddUser(ctx context.Context, in CreateUserReq) error {
	in.Username = strings.TrimSpace(in.Username)
	if in.Username == "" {
		return errors.New("username required")
	}
	if in.Password == "" {
		return errors.New("password required")
	}
	if in.Status != model.UserStatusDisabled && in.Status != model.UserStatusEnabled {
		return errors.New("invalid status")
	}

	// 生成密码 hash
	hash, err := bcrypt.GenerateFromPassword([]byte(in.Password), bcrypt.DefaultCost)
	if err != nil {
		return err
	}

	u := &model.User{
		SiteID:       in.SiteID,
		Username:     in.Username,
		PasswordHash: string(hash),
		Status:       in.Status,
	}
	return s.users.AddUser(ctx, u)
}
