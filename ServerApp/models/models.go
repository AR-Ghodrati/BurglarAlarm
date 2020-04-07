package models

type Response struct {
	Status  bool   `json:"status"`
	Message string `json:"msg"`
}

type AlarmStatus struct {
	Status   bool   `json:"status"`
	DeviceID string `json:"device_id"`
}

type User struct {
	DeviceID    string `json:"device_id,bson:device_id"`
	AlarmActive bool   `json:"alarm_active,bson:alarm_active"`
}
