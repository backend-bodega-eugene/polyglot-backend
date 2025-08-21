// service/menu_service.go
package service

import "eugene-go-starter/internal/model"

func BuildMenuTree(items []model.Menu) []model.Menu {
	mp := make(map[int64]*model.Menu, len(items))
	var roots []model.Menu
	for i := range items { mp[items[i].MenuID] = &items[i] }
	for _, it := range items {
		if it.ParentID == 0 {
			roots = append(roots, it)
		} else if p, ok := mp[it.ParentID]; ok {
			p.Children = append(p.Children, it)
		}
	}
	return roots
}
