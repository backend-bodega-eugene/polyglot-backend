// internal/repo/mongo/permission_mongo.go
package mongos

import (
	"context"
	"errors"
	"strconv"

	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type PermissionMongo struct {
	DB          *mongo.Database
	userMenuCol *mongo.Collection // "user_menu"
	menuCol     *mongo.Collection // "menus"（用来校验有效菜单 status=1）
}

func NewPermissionMongo(db *mongo.Database) repo.UserMenuRepo {
	return &PermissionMongo{
		DB:          db,
		userMenuCol: db.Collection("user_menu"),
		menuCol:     db.Collection("menus"),
	}
}

// CreateUserMenuModels —— 你的注释里“多条”，签名却是单条；这里尊重签名，做单条插入（对齐 MySQL 实现）
func (r *PermissionMongo) CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error {
	if userMenus == nil {
		return nil
	}
	doc := bson.M{
		"user_menu_id": userMenus.UserMenuId, // 若你在 Mongo 有自增器，可改由服务端生成
		"user_id":      userMenus.UserId,
		"menu_id":      userMenus.MenuId,
	}
	_, err := r.userMenuCol.InsertOne(ctx, doc)
	return err
}

// CreateUserMenuModel —— 注释说“单条”，但签名是多条；这里支持批量（对齐 MySQL 实现的批量插入）
func (r *PermissionMongo) CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error {
	if len(userMenus) == 0 {
		return nil
	}
	batch := make([]interface{}, 0, len(userMenus))
	for _, um := range userMenus {
		batch = append(batch, bson.M{
			"user_menu_id": um.UserMenuId,
			"user_id":      um.UserId,
			"menu_id":      um.MenuId,
		})
	}
	_, err := r.userMenuCol.InsertMany(ctx, batch, options.InsertMany().SetOrdered(true))
	return err
}

func (r *PermissionMongo) FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error) {
	// 与 MySQL 版保持一致：先做一次 userId 的数字校验
	if _, err := strconv.ParseUint(userId, 10, 64); err != nil {
		return nil, errors.New("invalid userId")
	}
	// user_id 在库里是数字的话，建议统一用 uint64 存；这里直接用字符串转为 uint64 再查询
	uid, _ := strconv.ParseUint(userId, 10, 64)

	cur, err := r.userMenuCol.Find(ctx, bson.M{"user_id": uid}, options.Find().SetSort(bson.D{{Key: "menu_id", Value: 1}}))
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	type row struct {
		UserMenuId uint64 `bson:"user_menu_id"`
		UserId     uint64 `bson:"user_id"`
		MenuId     uint64 `bson:"menu_id"`
	}
	var rs []row
	if err := cur.All(ctx, &rs); err != nil {
		return nil, err
	}

	out := make([]model.UserMenu, 0, len(rs))
	for _, x := range rs {
		out = append(out, model.UserMenu{
			UserMenuId: x.UserMenuId,
			UserId:     x.UserId,
			MenuId:     x.MenuId,
		})
	}
	return out, nil
}

// 一般权限映射很少“更新”，但为对齐接口，这里给个最小实现：根据 user_id + user_menu_id 改 menu_id
func (r *PermissionMongo) UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error {
	if userMenu == nil || userMenu.UserMenuId == 0 {
		return errors.New("invalid userMenu")
	}
	_, err := r.userMenuCol.UpdateOne(ctx,
		bson.M{"user_id": userId, "user_menu_id": userMenu.UserMenuId},
		bson.M{"$set": bson.M{"menu_id": userMenu.MenuId}},
	)
	return err
}

func (r *PermissionMongo) DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error {
	_, err := r.userMenuCol.DeleteOne(ctx, bson.M{"user_menu_id": userMenuId})
	return err
}

func (r *PermissionMongo) DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error {
	if len(userMenuIds) == 0 {
		return nil
	}
	_, err := r.userMenuCol.DeleteMany(ctx, bson.M{"user_menu_id": bson.M{"$in": userMenuIds}})
	return err
}

// SaveUserMenus —— 核心：先删后插、只保留有效菜单（status=1），与 MySQL 版一致的幂等语义
func (r *PermissionMongo) SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error {
	uid, err := strconv.ParseUint(userId, 10, 64)
	if err != nil || uid == 0 {
		return errors.New("invalid userId")
	}

	// 1) 过滤有效菜单（menus.status=1）
	valid, err := r.filterValidMenuIDs(ctx, menuIds)
	if err != nil {
		return err
	}

	// 2) 先删旧
	if _, err := r.userMenuCol.DeleteMany(ctx, bson.M{"user_id": uid}); err != nil {
		return err
	}

	// 3) 再插新
	if len(valid) == 0 {
		return nil
	}
	batch := make([]interface{}, 0, len(valid))
	for _, mid := range valid {
		batch = append(batch, bson.M{
			// 这里的 user_menu_id 如需全局自增，可在此对接你的计数器；否则也可省略该字段，用 Mongo 默认 _id
			"user_id": uid,
			"menu_id": mid,
		})
	}
	_, err = r.userMenuCol.InsertMany(ctx, batch, options.InsertMany().SetOrdered(true))
	return err
}

// —— 小工具：过滤有效菜单 —— //
func (r *PermissionMongo) filterValidMenuIDs(ctx context.Context, ids []uint64) ([]uint64, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	cur, err := r.menuCol.Find(ctx, bson.M{
		"menu_id": bson.M{"$in": ids},
		"status":  1,
	}, options.Find().SetProjection(bson.M{"menu_id": 1}))
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	var rows []struct {
		MenuID uint64 `bson:"menu_id"`
	}
	if err := cur.All(ctx, &rows); err != nil {
		return nil, err
	}
	out := make([]uint64, 0, len(rows))
	for _, r := range rows {
		out = append(out, r.MenuID)
	}
	return out, nil
}
