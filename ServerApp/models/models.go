package models

type Response struct {
	Status  bool   `json:"status"`
	Message string `json:"msg"`
}

type Status struct {
	Status   bool   `json:"status"`
	DeviceID string `json:"device_id"`
}

type User struct {
	DeviceID     string `json:"device_id" bson:"device_id"`
	AlarmActive  bool   `json:"alarm_active" bson:"alarm_active"`
	SensorActive bool   `json:"sensor_active" bson:"sensor_active"`
}
