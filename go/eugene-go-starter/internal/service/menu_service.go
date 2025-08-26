// service/menu_service.go
package service

import "eugene-go-starter/internal/model"

// BuildMenuTree —— 多层级正确版（保持原签名不变）
func BuildMenuTree(items []model.Menu) []model.Menu {
	// 1) 建索引 & 按父分组
	byID := make(map[uint64]model.Menu, len(items))
	childrenOf := make(map[uint64][]uint64)
	var rootIDs []uint64

	for _, m := range items {
		byID[m.MenuID] = m
		if m.ParentID == 0 {
			rootIDs = append(rootIDs, m.MenuID)
		} else {
			childrenOf[m.ParentID] = append(childrenOf[m.ParentID], m.MenuID)
		}
	}

	// 2) 递归构建“值类型”节点，子节点在构建时一并填充
	var build func(id uint64) model.Menu
	build = func(id uint64) model.Menu {
		n := byID[id] // 拷贝当前值
		if ids := childrenOf[id]; len(ids) > 0 {
			n.Children = make([]model.Menu, 0, len(ids))
			for _, cid := range ids {
				n.Children = append(n.Children, build(cid))
			}
		} else {
			n.Children = nil
		}
		return n
	}

	// 3) 生成根集合（顺序沿用 SQL 的排序：sort, menu_id）
	roots := make([]model.Menu, 0, len(rootIDs))
	for _, rid := range rootIDs {
		roots = append(roots, build(rid))
	}
	return roots
}
