package utils

import (
	"context"
	"github.com/apsdehal/go-logger"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"os"
)

var (
	Mongo   *mongo.Client
	NULL, _ = primitive.ObjectIDFromHex("000000000000000000000000")
)

func ConnectToMongo(logger *logger.Logger) {
	// Set client options
	clientOptions := options.Client().ApplyURI(os.Getenv("DATABASE"))
	// Connect to MongoDB
	c, err := mongo.Connect(context.TODO(), clientOptions)
	if err != nil {
		logger.Fatal("MongoDB:\tFailed -> " + err.Error())
		os.Exit(1)
	} else {
		// Check the connection
		err = c.Ping(context.TODO(), nil)
		if err != nil {
			logger.Fatal("MongoDB:\tFailed -> " + err.Error())
			os.Exit(1)
		}
		logger.Notice("MongoDB:\tSUCCESS")
		Mongo = c
	}
}

func DisconnectMongo(logger *logger.Logger) {

	err := Mongo.Disconnect(context.TODO())
	if err != nil {
		log.Fatal(err)
	}
	logger.Notice("MongoDB:\tDisconnected")
}
