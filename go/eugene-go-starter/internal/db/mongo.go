package db

import (
	"context"
	"os"
	"time"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

// NewMongoFromEnv initializes a Mongo database using env:
// MONGO_URI (e.g. mongodb://user:pass@host:27017), MONGO_DB (database name)
func NewMongoFromEnv() (*mongo.Database, func(context.Context) error, error) {
	uri := os.Getenv("MONGO_URI")
	if uri == "" {
		uri = "mongodb://localhost:27017"
	}
	dbName := os.Getenv("MONGO_DB")
	if dbName == "" {
		dbName = "eugene"
	}
	client, err := mongo.NewClient(options.Client().ApplyURI(uri))
	if err != nil {
		return nil, nil, err
	}
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := client.Connect(ctx); err != nil {
		return nil, nil, err
	}
	cleanup := func(ctx context.Context) error { return client.Disconnect(ctx) }
	return client.Database(dbName), cleanup, nil
}
