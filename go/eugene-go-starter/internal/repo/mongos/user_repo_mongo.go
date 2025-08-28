package mongos

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"golang.org/x/crypto/bcrypt"
)

type userRepoMongo struct {
	coll *mongo.Collection
}

func NewUserRepoMongo(db *mongo.Database) repo.UserRepo {
	return &userRepoMongo{coll: db.Collection("users")}
}

// 强制重置密码为 "eugene"（Mongo 版本）
//
//	func ForceSetPasswordMongo(ctx context.Context, coll *mongo.Collection, username string) error {
//		hash, _ := bcrypt.GenerateFromPassword([]byte("eugene"), bcrypt.DefaultCost)
//		_, err := coll.UpdateOne(ctx,
//			bson.M{"username": username},
//			bson.M{"$set": bson.M{"password_hash": string(hash)}},
//		)
//		return err
//	}
func (r *userRepoMongo) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	// errs := ForceSetPasswordMongo(ctx, r.coll, "eugene")
	// if errs != nil {
	// 	//return errs
	// }
	// 明确投影出登录要用到的字段（省得有些环境默认屏蔽了敏感列）
	opts := options.FindOne().SetProjection(bson.M{
		"_id":           0,
		"user_id":       1,
		"site_id":       1,
		"username":      1,
		"password_hash": 1, // ← 关键！给 handler 做 bcrypt 校验
		"status":        1,
		"last_login_at": 1,
		"updated_at":    1,
	})

	var u model.User
	err := r.coll.FindOne(ctx, bson.M{"username": username}, opts).Decode(&u)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, nil
	}
	return &u, err
}

func (r *userRepoMongo) Create(ctx context.Context, u *model.User) error {
	u.UpdatedAt = time.Now()
	_, err := r.coll.InsertOne(ctx, u)
	return err
}

func (r *userRepoMongo) UpdateLoginAt(ctx context.Context, userID uint64) error {
	_, err := r.coll.UpdateOne(ctx,
		bson.M{"user_id": userID},
		bson.M{"$set": bson.M{"last_login_at": time.Now(), "updated_at": time.Now()}},
	)
	return err
}

func (r *userRepoMongo) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	var u model.User
	if err := r.coll.FindOne(ctx, bson.M{"user_id": userID}).Decode(&u); err != nil {
		return repo.ErrUserNotFound
	}

	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return repo.ErrOldPasswordIncorrect
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return repo.ErrHashWrong
	}

	res, err := r.coll.UpdateOne(ctx,
		bson.M{"user_id": userID},
		bson.M{"$set": bson.M{"password_hash": string(hash), "updated_at": time.Now()}},
	)
	if err != nil {
		return repo.ErrUpdateFailed
	}
	if res.MatchedCount == 0 {
		return repo.ErrUserNotFound
	}
	return nil
}

func (r *userRepoMongo) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	res, err := r.coll.UpdateOne(ctx,
		bson.M{"user_id": userID},
		bson.M{"$set": bson.M{"status": status, "updated_at": time.Now()}},
	)
	if err != nil {
		return repo.ErrUpdateStatusFailed
	}
	if res.MatchedCount == 0 {
		return repo.ErrUserNotFound
	}
	return nil
}

// ====== 下面是你新加的 CRUD 方法 ======

func (r *userRepoMongo) ListAll(ctx context.Context, page int, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	filter := bson.M{}
	if kw := strings.TrimSpace(keywords); kw != "" {
		filter["username"] = bson.M{"$regex": kw, "$options": "i"}
	}

	opts := options.Find().
		SetSkip(int64((page - 1) * size)).
		SetLimit(int64(size)).
		SetSort(bson.M{"user_id": -1})

	cur, err := r.coll.Find(ctx, filter, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)

	var users []model.User
	if err := cur.All(ctx, &users); err != nil {
		return nil, err
	}
	return users, nil
}

func (r *userRepoMongo) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	var u model.User
	err := r.coll.FindOne(ctx, bson.M{"user_id": userID}).Decode(&u)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, repo.ErrNotFound
	}
	return &u, err
}

func (r *userRepoMongo) DdleteByID(ctx context.Context, userID uint64) error {
	res, err := r.coll.DeleteOne(ctx, bson.M{"user_id": userID})
	if err != nil {
		return err
	}
	if res.DeletedCount == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoMongo) UpdateByID(ctx context.Context, user *model.User) error {
	update := bson.M{
		"$set": bson.M{
			"username":   user.Username,
			"status":     user.Status,
			"updated_at": time.Now(),
		},
	}
	res, err := r.coll.UpdateOne(ctx, bson.M{"user_id": user.UserID}, update)
	if err != nil {
		// mongo 的唯一索引冲突会报 duplicate key error
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return err
	}
	if res.MatchedCount == 0 {
		return repo.ErrNotFound
	}
	return nil
}

func (r *userRepoMongo) AddUser(ctx context.Context, user *model.User) error {
	user.UpdatedAt = time.Now()
	_, err := r.coll.InsertOne(ctx, user)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return repo.ErrConflict
		}
		return err
	}
	return nil
}
