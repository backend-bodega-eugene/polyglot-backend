package repo

import (
	"context"
	"eugene-go-starter/internal/model"
	"strings"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type menuRepoMongo struct{ col *mongo.Collection }

func NewMenuRepoMongo(db *mongo.Database) MenuRepo {
	return &menuRepoMongo{col: db.Collection("menus")}
}

func (r *menuRepoMongo) ListAll(ctx context.Context) ([]model.Menu, error) {
	opts := options.Find().
		SetProjection(bson.M{
			"menu_id": 1, "parent_id": 1, "name": 1, "path": 1, "icon": 1,
			"sort": 1, "status": 1, "component": 1, "visible": 1,
		}).
		SetSort(bson.D{{Key: "sort", Value: 1}, {Key: "menu_id", Value: 1}})
	cur, err := r.col.Find(ctx, bson.M{}, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)
	var out []model.Menu
	for cur.Next(ctx) {
		var m model.Menu
		if err := cur.Decode(&m); err != nil {
			return nil, err
		}
		out = append(out, m)
	}
	return out, cur.Err()
}

func (r *menuRepoMongo) ListByUser(ctx context.Context, userID uint64) ([]model.Menu, error) {
	// 通过 user_menu 关系集合聚合
	pipeline := mongo.Pipeline{
		{{Key: "$match", Value: bson.M{"user_id": userID}}},
		{{Key: "$lookup", Value: bson.M{"from": "menus", "localField": "menu_id", "foreignField": "menu_id", "as": "menus"}}},
		{{Key: "$unwind", Value: "$menus"}},
		{{Key: "$replaceRoot", Value: bson.M{"newRoot": "$menus"}}},
		{{Key: "$sort", Value: bson.D{{Key: "sort", Value: 1}, {Key: "menu_id", Value: 1}}}},
	}
	cur, err := r.col.Database().Collection("user_menu").Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)
	var out []model.Menu
	for cur.Next(ctx) {
		var m model.Menu
		if err := cur.Decode(&m); err != nil {
			return nil, err
		}
		out = append(out, m)
	}
	return out, cur.Err()
}

func (r *menuRepoMongo) Create(ctx context.Context, m *model.Menu) (uint64, error) {
	_, err := r.col.InsertOne(ctx, m)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return 0, ErrNameDuplicate
		}
		return 0, err
	}
	return m.MenuID, nil
}

func (r *menuRepoMongo) Update(ctx context.Context, m *model.Menu) error {
	update := bson.M{"$set": bson.M{
		"parent_id": m.ParentID,
		"name":      m.Name,
		"path":      m.Path,
		"icon":      m.Icon,
		"sort":      m.Sort,
		"status":    m.Status,
		"component": m.Component,
		"visible":   m.Visible,
	}}
	res, err := r.col.UpdateOne(ctx, bson.M{"menu_id": m.MenuID}, update)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return ErrNameDuplicate
		}
		return err
	}
	if res.MatchedCount == 0 {
		return mongo.ErrNoDocuments
	}
	return nil
}

func (r *menuRepoMongo) Delete(ctx context.Context, menuID uint64) error {
	// 先检查是否有子节点
	cnt, err := r.col.CountDocuments(ctx, bson.M{"parent_id": menuID})
	if err != nil {
		return err
	}
	if cnt > 0 {
		return ErrHasChildren
	}
	res, err := r.col.DeleteOne(ctx, bson.M{"menu_id": menuID})
	if err != nil {
		return err
	}
	if res.DeletedCount == 0 {
		return mongo.ErrNoDocuments
	}
	return nil
}
