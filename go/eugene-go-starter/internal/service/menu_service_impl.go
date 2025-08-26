package service

import (
	"context"
	"strings"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
)

// 保留你原来的 BuildMenuTree（在另一个文件），这里新增 Service

type Service struct {
	repo repo.MenuRepo
}

func NewService(r repo.MenuRepo) *Service { return &Service{repo: r} }

// ===== 读：树（全部菜单）=====
func (s *Service) GetTree(ctx context.Context) ([]*model.Node, error) {
	list, err := s.repo.ListAll(ctx)
	if err != nil {
		return nil, err
	}
	return buildTreeNodes(list), nil
}

// ===== 读：树（按用户）=====
func (s *Service) GetTreeByUser(ctx context.Context, userID uint64) ([]*model.Node, error) {
	list, err := s.repo.ListByUser(ctx, userID)
	if err != nil {
		return nil, err
	}
	return buildTreeNodes(list), nil
}

// ===== 读：单个（目前没有 GetByID，就从 ListAll 里找一下）=====
func (s *Service) GetOne(ctx context.Context, id uint64) (*model.Menu, error) {
	list, err := s.repo.ListAll(ctx)
	if err != nil {
		return nil, err
	}
	for i := range list {
		if list[i].MenuID == id {
			m := list[i] // 拷贝，避免外部改动底层切片
			return &m, nil
		}
	}
	// 你 repo 包里已有 ErrNotFound（在你的实现里被引用过），这里直接复用
	return nil, repo.ErrNotFound
}

// ===== 写：增删改（规范一下入参再透传到 repo）=====

func (s *Service) Create(ctx context.Context, m *model.Menu) (uint64, error) {
	normalize(m)
	return s.repo.Create(ctx, m)
}

func (s *Service) Update(ctx context.Context, m *model.Menu) error {
	normalize(m)
	return s.repo.Update(ctx, m)
}

func (s *Service) Delete(ctx context.Context, id uint64) error {
	return s.repo.Delete(ctx, id)
}

// ===== 工具：把 []model.Menu 拍成树（*model.Node 指针树，任意层级都 OK）=====

func buildTreeNodes(list []model.Menu) []*model.Node {
	idx := make(map[uint64]*model.Node, len(list))
	for i := range list {
		m := list[i] // 拷贝值，避免被外部修改
		idx[m.MenuID] = &model.Node{Menu: m}
	}

	var roots []*model.Node
	for _, n := range idx {
		if n.ParentID == 0 {
			roots = append(roots, n)
			continue
		}
		if p, ok := idx[n.ParentID]; ok {
			p.Children = append(p.Children, n)
		} else {
			// 父级缺失兜底为根，防止“掉节点”
			roots = append(roots, n)
		}
	}
	return roots
}

// ===== 规整入参：空白字符串 -> nil，开关量归一到 0/1 =====

func normalize(m *model.Menu) {
	m.Name = strings.TrimSpace(m.Name)

	if m.Path != nil {
		if t := strings.TrimSpace(*m.Path); t == "" {
			m.Path = nil
		} else {
			m.Path = &t
		}
	}
	if m.Component != nil {
		if t := strings.TrimSpace(*m.Component); t == "" {
			m.Component = nil
		} else {
			m.Component = &t
		}
	}
	if m.Icon != nil {
		if t := strings.TrimSpace(*m.Icon); t == "" {
			m.Icon = nil
		} else {
			m.Icon = &t
		}
	}

	// 你的表里是 TINYINT，Go 侧用 int8：统一 0/1
	if m.Visible != 0 {
		m.Visible = 1
	}
	if m.Status != 0 {
		m.Status = 1
	}
}
