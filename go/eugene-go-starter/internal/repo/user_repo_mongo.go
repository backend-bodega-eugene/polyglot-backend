package repo

import (
	"context"
	"errors"
	"eugene-go-starter/internal/model"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"golang.org/x/crypto/bcrypt"
)

type userRepoMongo struct{ col *mongo.Collection }

func NewUserRepoMongo(db *mongo.Database) UserRepo {
	return &userRepoMongo{col: db.Collection("users")}
}

func (r *userRepoMongo) FindBySiteAndUsername(ctx context.Context, username string) (*model.User, error) {
	var u model.User
	opt := options.FindOne().SetProjection(bson.M{
		"user_id":       1,
		"site_id":       1,
		"username":      1,
		"password_hash": 1,
		"status":        1,
		"last_login_at": 1,
		"updated_at":    1,
	})
	err := r.col.FindOne(ctx, bson.M{"username": username}, opt).Decode(&u)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return &u, nil
}

func (r *userRepoMongo) Create(ctx context.Context, u *model.User) error {
	u.UpdatedAt = time.Now()
	_, err := r.col.InsertOne(ctx, u)
	return err
}

func (r *userRepoMongo) UpdateLoginAt(ctx context.Context, userID uint64) error {
	_, err := r.col.UpdateOne(ctx, bson.M{"user_id": userID}, bson.M{"$set": bson.M{"last_login_at": time.Now(), "updated_at": time.Now()}})
	return err
}

func (r *userRepoMongo) UpdatePassword(ctx context.Context, userID uint64, oldPassword string, newPassword string) error {
	var u model.User
	if err := r.col.FindOne(ctx, bson.M{"user_id": userID}).Decode(&u); err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return ErrUserNotFound
		}
		return err
	}
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(oldPassword)); err != nil {
		return ErrOldPasswordIncorrect
	}
	hash, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		return ErrHashWrong
	}
	res, err := r.col.UpdateOne(ctx, bson.M{"user_id": userID}, bson.M{"$set": bson.M{"password_hash": string(hash), "updated_at": time.Now()}})
	if err != nil {
		return ErrUpdateFailed
	}
	if res.MatchedCount == 0 {
		return mongo.ErrNoDocuments
	}
	return nil
}

func (r *userRepoMongo) UpdateStatus(ctx context.Context, userID uint64, status int) error {
	res, err := r.col.UpdateOne(ctx, bson.M{"user_id": userID}, bson.M{"$set": bson.M{"status": status}})
	if err != nil {
		return ErrUpdateStatusFailed
	}
	if res.MatchedCount == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoMongo) ListAll(ctx context.Context, page, size int, keywords string) ([]model.User, error) {
	if page < 1 {
		page = 1
	}
	if size <= 0 || size > 200 {
		size = 10
	}
	filter := bson.M{}
	if kw := strings.TrimSpace(keywords); kw != "" {
		filter["username"] = bson.M{"$regex": primitive.Regex{Pattern: kw, Options: "i"}}
	}
	opts := options.Find().SetLimit(int64(size)).SetSkip(int64((page - 1) * size))
	cur, err := r.col.Find(ctx, filter, opts)
	if err != nil {
		return nil, err
	}
	defer cur.Close(ctx)
	users := make([]model.User, 0, size)
	for cur.Next(ctx) {
		var u model.User
		if err := cur.Decode(&u); err != nil {
			return nil, err
		}
		users = append(users, u)
	}
	return users, cur.Err()
}

func (r *userRepoMongo) GetByID(ctx context.Context, userID uint64) (*model.User, error) {
	var u model.User
	err := r.col.FindOne(ctx, bson.M{"user_id": userID}).Decode(&u)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &u, nil
}

func (r *userRepoMongo) DdleteByID(ctx context.Context, userID uint64) error {
	res, err := r.col.DeleteOne(ctx, bson.M{"user_id": userID})
	if err != nil {
		return err
	}
	if res.DeletedCount == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoMongo) UpdateByID(ctx context.Context, user *model.User) error {
	update := bson.M{"$set": bson.M{"username": user.Username, "status": user.Status, "updated_at": time.Now()}}
	res, err := r.col.UpdateOne(ctx, bson.M{"user_id": user.UserID}, update)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return ErrConflict
		}
		return err
	}
	if res.MatchedCount == 0 {
		return ErrNotFound
	}
	return nil
}

func (r *userRepoMongo) AddUser(ctx context.Context, user *model.User) error {
	user.UpdatedAt = time.Now()
	_, err := r.col.InsertOne(ctx, user)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "duplicate") {
			return ErrConflict
		}
		return err
	}
	return nil
}
