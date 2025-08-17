package service

import (
	"context"
	"errors"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
)

var (
	ErrInvalidCredential = errors.New("invalid username or password")
	ErrDisabledUser      = errors.New("user disabled")
	ErrUserExists        = errors.New("user already exists") 
)

type UserService struct {
	repo repo.UserRepo
}

func NewUserService(r repo.UserRepo) *UserService {
	return &UserService{repo: r}
}

func (s *UserService) Register(ctx context.Context, siteID uint64, username, password string) error {
	// 1. 查重
	exist, err := s.repo.FindBySiteAndUsername(ctx, siteID, username)
	if err != nil { return err }
	if exist != nil { return ErrUserExists }

	// 2. 组装实体 + 生成 hash
	u := &model.User{SiteID: siteID, Username: username, Status: model.UserStatusEnabled}
	if err := u.SetPassword(password); err != nil { return err }

	// 3. 入库
	return s.repo.Create(ctx, u)
}

func (s *UserService) Login(ctx context.Context, siteID uint64, username, password string) (*model.User, error) {
	u, err := s.repo.FindBySiteAndUsername(ctx, siteID, username)
	if err != nil || u == nil { return nil, ErrInvalidCredential }
	if u.Status != model.UserStatusEnabled { return nil, ErrDisabledUser }
	if !u.CheckPassword(password) { return nil, ErrInvalidCredential }

	// 成功就顺手更 last_login_at（失败不阻塞）
	_ = s.repo.UpdateLoginAt(ctx, u.UserID)
	return u, nil
}

