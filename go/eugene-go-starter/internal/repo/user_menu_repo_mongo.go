package repo

import (
	"context"
	"eugene-go-starter/internal/model"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

type permissionMongo struct{ col *mongo.Collection }

func NewPermissionMongo(db *mongo.Database) UserMenuRepo {
	return &permissionMongo{col: db.Collection("user_menu")}
}

func (r *permissionMongo) CreateUserMenuModels(ctx context.Context, userMenus *model.UserMenu) error {
	_, err := r.col.InsertOne(ctx, userMenus)
	return err
}

func (r *permissionMongo) CreateUserMenuModel(ctx context.Context, userMenus []model.UserMenu) error {
	if len(userMenus) == 0 {
		return nil
	}
	docs := make([]interface{}, 0, len(userMenus))
	for _, um := range userMenus {
		docs = append(docs, um)
	}
	_, err := r.col.InsertMany(ctx, docs)
	return err
}

func (r *permissionMongo) FindByUserId(ctx context.Context, userId string) ([]model.UserMenu, error) {
	cur, err := r.col.Find(ctx, bson.M{"user_id": userId})
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)
	var out []model.UserMenu
	for cur.Next(ctx) {
		var um model.UserMenu
		if err := cur.Decode(&um); err != nil {
			return nil, err
		}
		out = append(out, um)
	}
	return out, cur.Err()
}

func (r *permissionMongo) UpdateUserMenuModel(ctx context.Context, userId uint64, userMenu *model.UserMenu) error {
	_, err := r.col.UpdateOne(ctx, bson.M{"user_id": userId, "user_menu_id": userMenu.UserMenuId}, bson.M{"$set": bson.M{"menu_id": userMenu.MenuId}})
	return err
}

func (r *permissionMongo) DeleteUserMenuModel(ctx context.Context, userMenuId uint64) error {
	_, err := r.col.DeleteOne(ctx, bson.M{"user_menu_id": userMenuId})
	return err
}

func (r *permissionMongo) DeleteUserMenuModels(ctx context.Context, userMenuIds []uint64) error {
	if len(userMenuIds) == 0 {
		return nil
	}
	_, err := r.col.DeleteMany(ctx, bson.M{"user_menu_id": bson.M{"$in": userMenuIds}})
	return err
}

func (r *permissionMongo) SaveUserMenus(ctx context.Context, userId string, menuIds []uint64) error {
	// 先删后插
	if _, err := r.col.DeleteMany(ctx, bson.M{"user_id": userId}); err != nil {
		return err
	}
	if len(menuIds) == 0 {
		return nil
	}
	docs := make([]interface{}, 0, len(menuIds))
	for _, mid := range menuIds {
		docs = append(docs, bson.M{"user_id": userId, "menu_id": mid})
	}
	_, err := r.col.InsertMany(ctx, docs)
	return err
}
