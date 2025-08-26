package repo

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
)

type MenuRepo interface {
	// 基础查询
	ListAll(ctx context.Context) ([]model.Menu, error)
	ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error)

	// listAll,已经有了,目前认为能进入这个页面就是对所有菜单有管理权限,这个我们就不改了
	//添加,删除,修改,接口
	//修改就意味着,我们可以更改它的父级,菜单表有parent_id,
	Create(ctx context.Context, m *model.Menu) (uint64, error)
	Update(ctx context.Context, m *model.Menu) error
	Delete(ctx context.Context, menuID uint64) error
}

var (
	ErrHasChildren   = errors.New(" has not children")
	ErrNameDuplicate = errors.New("name duplicate")
	ErrParentIsDescendant = errors.New("parent is descendant")
	ErrParentNotFound      = errors.New("parent not found")
	ErrParentIsSelf        = errors.New("parent is self")
)
