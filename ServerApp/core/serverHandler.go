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
	http.HandleFunc("/app/getSensorStatus", getSensorStatus)

	// board handlers
	http.HandleFunc("/board/setSensorStatus", setSensorStatus)
	http.HandleFunc("/board/getAlarmStatus", getAlarmStatus)
	http.HandleFunc("/board/register", registerUser)

	_ = http.ListenAndServe(":"+os.Getenv("PORT"), nil)
}

func setAlarmStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		return
	}

	data, err := ioutil.ReadAll(r.Body)
	_logger.Info("setAlarmStatus, with data : " + string(data))
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write(createErrorResponse("internal error"))
	} else {
		var alarmStatus *models.Status
		_ = json.Unmarshal(data, &alarmStatus)

		if ok := setAlarmWithDeviceID(alarmStatus.Status, alarmStatus.DeviceID); ok {
			response := models.Response{
				Status:  true,
				Message: "saved",
			}

			data, err := json.Marshal(response)
			if err == nil {
				w.WriteHeader(http.StatusOK)
				_, _ = w.Write(data)
			} else {
				w.WriteHeader(http.StatusInternalServerError)
				_, _ = w.Write(createErrorResponse("internal error"))
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write(createErrorResponse("not found"))
		}
	}
}

func getAlarmStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		return
	}
	deviceId, ok := r.URL.Query()["did"]

	if ok {
		if len(deviceId) > 0 {
			_logger.Info("getAlarmStatus, with did : " + deviceId[0])
			if user, ok := getUserWithDeviceID(deviceId[0]); ok {
				response := models.Response{
					Status:  user.AlarmActive,
					Message: "ok",
				}
				data, err := json.Marshal(response)
				if err == nil {
					w.WriteHeader(http.StatusOK)
					_, _ = w.Write(data)
				} else {
					w.WriteHeader(http.StatusInternalServerError)
					_, _ = w.Write(createErrorResponse("internal error"))
				}
			} else {
				w.WriteHeader(http.StatusNotFound)
				_, _ = w.Write(createErrorResponse("not found"))
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write(createErrorResponse("not found"))
		}
	} else {
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write(createErrorResponse("not found"))
	}
}

func getSensorStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		return
	}
	deviceId, ok := r.URL.Query()["did"]

	if ok {
		if len(deviceId) > 0 {
			_logger.Info("getSensorStatus, with did : " + deviceId[0])
			if user, ok := getUserWithDeviceID(deviceId[0]); ok {
				response := models.Response{
					Status:  user.SensorActive,
					Message: "ok",
				}
				data, err := json.Marshal(response)
				if err == nil {
					w.WriteHeader(http.StatusOK)
					_, _ = w.Write(data)
				} else {
					w.WriteHeader(http.StatusInternalServerError)
					_, _ = w.Write(createErrorResponse("internal error"))
				}
			} else {
				w.WriteHeader(http.StatusNotFound)
				_, _ = w.Write(createErrorResponse("not found"))
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write(createErrorResponse("not found"))
		}
	} else {
		w.WriteHeader(http.StatusNotFound)
		_, _ = w.Write(createErrorResponse("not found"))
	}
}

func setSensorStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		return
	}

	data, err := ioutil.ReadAll(r.Body)
	_logger.Info("setSensorStatus, with data : " + string(data))
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write(createErrorResponse("internal error"))
	} else {
		var alarmStatus *models.Status
		_ = json.Unmarshal(data, &alarmStatus)

		if ok := setSensorWithDeviceID(alarmStatus.Status, alarmStatus.DeviceID); ok {
			response := models.Response{
				Status:  true,
				Message: "saved",
			}

			data, err := json.Marshal(response)
			if err == nil {
				w.WriteHeader(http.StatusOK)
				_, _ = w.Write(data)
			} else {
				w.WriteHeader(http.StatusInternalServerError)
				_, _ = w.Write(createErrorResponse("internal error"))
			}
		} else {
			w.WriteHeader(http.StatusNotFound)
			_, _ = w.Write(createErrorResponse("not found"))
		}
	}
}

func registerUser(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		return
	}

	data, err := ioutil.ReadAll(r.Body)
	_logger.Info("registerUser, with data :  " + string(data))
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write(createErrorResponse("internal error"))
	} else {
		deviceId := string(data)
		if addUserWithDeviceID(deviceId) {
			response := models.Response{
				Status:  true,
				Message: "register successfully",
			}

			data, err := json.Marshal(response)
			if err == nil {
				w.WriteHeader(http.StatusOK)
				_, _ = w.Write(data)
			} else {
				w.WriteHeader(http.StatusInternalServerError)
				_, _ = w.Write(createErrorResponse("internal error"))
			}
		} else {
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = w.Write(createErrorResponse("internal error"))
		}
	}
}

func createErrorResponse(err string) []byte {
	response := models.Response{
		Status:  false,
		Message: err,
	}
	data, _ := json.Marshal(response)
	return data
}

func getUserWithDeviceID(deviceId string) (models.User, bool) {
	collection := utils.Mongo.Database(os.Getenv("DBNAME")).Collection("users")
	var user models.User
	err := collection.FindOne(context.TODO(), bson.M{"device_id": deviceId}).Decode(&user)
	if err != nil {
		_logger.Critical("getUserWithDeviceID err :" + err.Error())
		return user, false
	} else {
		return user, true
	}
}

func setAlarmWithDeviceID(isActive bool, deviceId string) bool {
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
func setSensorWithDeviceID(isActive bool, deviceId string) bool {
	collection := utils.Mongo.Database(os.Getenv("DBNAME")).Collection("users")
	_, err := collection.UpdateOne(context.TODO(), bson.M{"device_id": deviceId}, bson.D{{"$set",
		bson.D{
			{"sensor_active", isActive},
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
