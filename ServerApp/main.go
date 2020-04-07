package main

import (
	"BurglarAlarm/ServerApp/utils"
	"fmt"
	"github.com/apsdehal/go-logger"
	"github.com/joho/godotenv"
	"log"
	"os"
)

func main() {

	Logger, err := logger.New("MainLogger", 1, os.Stdout)
	if err != nil {
		panic(err) // Check for error
	}

	err = godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env file")
	}

	Logger.Notice(os.Getenv("Burglar Alarm Server") + " v1.0\tpid:" + fmt.Sprintf("%d", os.Getpid()))
	Logger.Notice("-------Status---------")

	utils.ConnectToMongo(Logger)

}
