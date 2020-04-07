package core

import (
	"BurglarAlarm/ServerApp/models"
	"BurglarAlarm/ServerApp/utils"
	"context"
	"encoding/json"
	"github.com/apsdehal/go-logger"
	"go.mongodb.org/mongo-driver/bson"
	"io/ioutil"
	"net/http"
	"os"
)

var _logger *logger.Logger

func HandleRequests(logger *logger.Logger) {
	_logger = logger

	// app handlers
	http.HandleFunc("/app/setAlarmStatus", setAlarmStatus)
	http.HandleFunc("/app/getAlarmStatus", getAlarmStatus)

	// board handlers
	http.HandleFunc("/board/setAlarmActive", setAlarmStatus)
	http.HandleFunc("/board/getAlarmStatus", getAlarmStatus)
	http.HandleFunc("/board/register", registerUser)

	_ = http.ListenAndServe(":"+os.Getenv("PORT"), nil)
}

func setAlarmStatus(w http.ResponseWriter, r *http.Request) {
	data, err := ioutil.ReadAll(r.Body)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte("internal error"))
	} else {
		var alarmStatus *models.AlarmStatus
		_ = json.Unmarshal(data, &alarmStatus)

		if ok := setUserWithDeviceID(alarmStatus.Status, alarmStatus.DeviceID); ok {
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte("saved"))
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write([]byte("not found"))
		}
	}
}

func getAlarmStatus(w http.ResponseWriter, r *http.Request) {
	deviceId, ok := r.URL.Query()["did"]
	if ok {
		if len(deviceId) > 0 {
			if isAlarmActive, ok := getUserWithDeviceID(deviceId[0]); ok {
				response := models.Response{
					Status:  isAlarmActive,
					Message: "ok",
				}
				data, err := json.Marshal(response)
				if err == nil {
					w.WriteHeader(http.StatusOK)
					_, _ = w.Write(data)
				} else {
					w.WriteHeader(http.StatusInternalServerError)
					_, _ = w.Write([]byte("internal error"))
				}
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write([]byte("not found"))
		}
	} else {
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write([]byte("not found"))
	}
}

func registerUser(w http.ResponseWriter, r *http.Request) {
	data, err := ioutil.ReadAll(r.Body)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte("internal error"))
	} else {
		deviceId := string(data)
		if addUserWithDeviceID(deviceId) {
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte("register successfully"))
		} else {
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = w.Write([]byte("internal error"))
		}
	}
}

func getUserWithDeviceID(deviceId string) (bool, bool) {
	collection := utils.Mongo.Database(os.Getenv("DBNAME")).Collection("users")
	var user *models.User
	err := collection.FindOne(context.TODO(), bson.M{"device_id": deviceId}).Decode(&user)
	if err != nil {
		_logger.Critical("getUserWithDeviceID err :" + err.Error())
		return false, false
	} else {
		return user.AlarmActive, true
	}
}
func setUserWithDeviceID(isActive bool, deviceId string) bool {
	collection := utils.Mongo.Database(os.Getenv("DBNAME")).Collection("users")
	_, err := collection.UpdateOne(context.TODO(), bson.M{"device_id": deviceId}, bson.D{{"$set",
		bson.D{
			{"alarm_active", isActive},
		}}})
	if err != nil {
		_logger.Critical("setUserWithDeviceID err :" + err.Error())
		return false
	} else {
		return true
	}
}
func addUserWithDeviceID(deviceId string) bool {
	if _, ok := getUserWithDeviceID(deviceId); !ok {
		collection := utils.Mongo.Database(os.Getenv("DBNAME")).Collection("users")
		_, err := collection.InsertOne(context.TODO(), models.User{DeviceID: deviceId})
		if err != nil {
			_logger.Critical("addUserWithDeviceID err :" + err.Error())
			return false
		} else {
			return true
		}
	} else {
		return true
	}
}
