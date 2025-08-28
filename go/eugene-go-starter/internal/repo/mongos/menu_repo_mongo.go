// internal/repo/mongo/menu_mongo.go
package mongos

import (
	"context"
	"errors"
	"fmt"
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"sort"
	"strings"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type MenuMongo struct {
	DB          *mongo.Database
	menuColl    *mongo.Collection // "menus"
	userMenuColl *mongo.Collection // "user_menu"
}

func NewMenuRepoMongo(db *mongo.Database) repo.MenuRepo {
	return &MenuMongo{
		DB:           db,
		menuColl:     db.Collection("menus"),
		userMenuColl: db.Collection("user_menu"),
	}
}

// ========== 基础查询 ==========

func (r *MenuMongo) ListAll(ctx context.Context) ([]model.Menu, error) {
	filter := bson.M{"status": 1}
	opts := options.Find().SetSort(bson.D{{Key: "sort", Value: 1}, {Key: "menu_id", Value: 1}})

	cur, err := r.menuColl.Find(ctx, filter, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	var out []model.Menu
	if err := cur.All(ctx, &out); err != nil {
		return nil, err
	}
	return out, nil
}

func (r *MenuMongo) ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error) {
	// 步骤1：查 user_menu 取该用户拥有的 menu_id 列表
	umCur, err := r.userMenuColl.Find(ctx, bson.M{"user_id": userID})
	if err != nil {
		return nil, err
	}
	defer umCur.Close(ctx)

	type userMenuRow struct {
		MenuID uint64 `bson:"menu_id"`
	}
	var umList []userMenuRow
	if err := umCur.All(ctx, &umList); err != nil {
		return nil, err
	}
	if len(umList) == 0 {
		return []model.Menu{}, nil
	}

	ids := make([]uint64, 0, len(umList))
	for _, x := range umList {
		ids = append(ids, x.MenuID)
	}

	// 步骤2：查 menus in ids 且 status=1
	filter := bson.M{
		"menu_id": bson.M{"$in": ids},
		"status":  1,
	}
	opts := options.Find().SetSort(bson.D{{Key: "sort", Value: 1}, {Key: "menu_id", Value: 1}})
	cur, err := r.menuColl.Find(ctx, filter, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	var out []model.Menu
	if err := cur.All(ctx, &out); err != nil {
		return nil, err
	}
	// 注意：$in 不保证顺序，保险起见再按 sort, menu_id 排一遍
	sort.Slice(out, func(i, j int) bool {
		if out[i].Sort == out[j].Sort {
			return out[i].MenuID < out[j].MenuID
		}
		return out[i].Sort < out[j].Sort
	})
	return out, nil
}

// ========== 工具/校验 ==========

func (r *MenuMongo) exists(ctx context.Context, id uint64) (bool, error) {
	err := r.menuColl.FindOne(ctx, bson.M{"menu_id": id}).Err()
	if errors.Is(err, mongo.ErrNoDocuments) {
		return false, nil
	}
	return err == nil, err
}

func (r *MenuMongo) parentExistsOrZero(ctx context.Context, pid uint64) (bool, error) {
	if pid == 0 {
		return true, nil
	}
	return r.exists(ctx, pid)
}

func (r *MenuMongo) nameDupUnderParent(ctx context.Context, parentID uint64, name string, excludeID uint64) (bool, error) {
	filter := bson.M{
		"parent_id": parentID,
		"name":      name,
	}
	if excludeID > 0 {
		filter["menu_id"] = bson.M{"$ne": excludeID}
	}
	err := r.menuColl.FindOne(ctx, filter).Err()
	if errors.Is(err, mongo.ErrNoDocuments) {
		return false, nil
	}
	return err == nil, err
}

// isDescendant：判断 newParentID 是否在 currID 的后代中
// Mongo 没有直接的递归查询，这里用 BFS 逐层找孩子节点：parent_id in (...) -> 收集 menu_id -> 继续
func (r *MenuMongo) isDescendant(ctx context.Context, currID, newParentID uint64) (bool, error) {
	if newParentID == 0 {
		return false, nil
	}
	queue := []uint64{currID}
	seen := map[uint64]struct{}{currID: {}}

	for len(queue) > 0 {
		// 批量找所有以 queue 为 parent_id 的孩子
		filter := bson.M{"parent_id": bson.M{"$in": queue}}
		proj := options.Find().SetProjection(bson.M{"menu_id": 1})
		cur, err := r.menuColl.Find(ctx, filter, proj)
		if err != nil {
			return false, err
		}
		var kids []struct {
			MenuID uint64 `bson:"menu_id"`
		}
		if err := cur.All(ctx, &kids); err != nil {
			_ = cur.Close(ctx)
			return false, err
		}
		_ = cur.Close(ctx)
		if len(kids) == 0 {
			return false, nil
		}

		queue = queue[:0] // 复用 slice 容量
		for _, k := range kids {
			if k.MenuID == newParentID {
				return true, nil
			}
			if _, ok := seen[k.MenuID]; !ok {
				seen[k.MenuID] = struct{}{}
				queue = append(queue, k.MenuID)
			}
		}
	}
	return false, nil
}

// ========== CRUD ==========

func (r *MenuMongo) Create(ctx context.Context, m *model.Menu) (uint64, error) {
	if m == nil {
		return 0, fmt.Errorf("nil menu")
	}
	m.Name = strings.TrimSpace(m.Name)
	if m.Name == "" {
		return 0, fmt.Errorf("name required")
	}
	if ok, err := r.parentExistsOrZero(ctx, m.ParentID); err != nil {
		return 0, err
	} else if !ok {
		return 0, repo.ErrParentNotFound
	}
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, 0); err != nil {
		return 0, err
	} else if dup {
		return 0, repo.ErrNameDuplicate
	}

	// 规范可空字段（与 MySQL 版保持一致）
	m.Path = repo.TrimPtr(m.Path)
	m.Component = repo.TrimPtr(m.Component)
	m.Icon = repo.TrimPtr(m.Icon)

	// 这里假设 menu_id 由应用层分配（或你已有自增器/雪花等），与 MySQL 语义对齐。
	// 如果你在 Mongo 中用的是某个计数器集合，请在此处先获取新 id 再插入。
	_, err := r.menuColl.InsertOne(ctx, m)
	if err != nil {
		return 0, err
	}
	return m.MenuID, nil
}

func (r *MenuMongo) Update(ctx context.Context, m *model.Menu) error {
	if m == nil || m.MenuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	// 存在性校验
	if ok, err := r.exists(ctx, m.MenuID); err != nil {
		return err
	} else if !ok {
		return repo.ErrNotFound
	}

	m.Name = strings.TrimSpace(m.Name)
	if m.Name == "" {
		return fmt.Errorf("name required")
	}
	if m.ParentID == m.MenuID {
		return repo.ErrParentIsSelf
	}
	// 父级存在 & 防环
	if ok, err := r.parentExistsOrZero(ctx, m.ParentID); err != nil {
		return err
	} else if !ok {
		return repo.ErrParentNotFound
	}
	if desc, err := r.isDescendant(ctx, m.MenuID, m.ParentID); err != nil {
		return err
	} else if desc {
		return repo.ErrParentIsDescendant
	}
	// 同父同名
	if dup, err := r.nameDupUnderParent(ctx, m.ParentID, m.Name, m.MenuID); err != nil {
		return err
	} else if dup {
		return repo.ErrNameDuplicate
	}

	// 规范可空
	m.Path = repo.TrimPtr(m.Path)
	m.Component = repo.TrimPtr(m.Component)
	m.Icon = repo.TrimPtr(m.Icon)

	update := bson.M{
		"$set": bson.M{
			"parent_id": m.ParentID,
			"name":      m.Name,
			"path":      m.Path,
			"component": m.Component,
			"icon":      m.Icon,
			"visible":   m.Visible,
			"sort":      m.Sort,
			"status":    m.Status,
		},
	}
	_, err := r.menuColl.UpdateOne(ctx, bson.M{"menu_id": m.MenuID}, update)
	return err
}

func (r *MenuMongo) Delete(ctx context.Context, menuID uint64) error {
	if menuID == 0 {
		return fmt.Errorf("invalid menu_id")
	}
	// 是否存在
	if ok, err := r.exists(ctx, menuID); err != nil {
		return err
	} else if !ok {
		return repo.ErrNotFound
	}
	// 有子节点则拒绝
	cnt, err := r.menuColl.CountDocuments(ctx, bson.M{"parent_id": menuID})
	if err != nil {
		return err
	}
	if cnt > 0 {
		return repo.ErrHasChildren
	}
	_, err = r.menuColl.DeleteOne(ctx, bson.M{"menu_id": menuID})
	return err
}
