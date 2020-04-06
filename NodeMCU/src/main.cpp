#include <Arduino.h>
#include <ESP8266WiFi.h>
#include "ArduinoJson_ID64/ArduinoJson.h"


// defines pins numbers for HY-SRF05
#define trigPin  D4 
#define echoPin  D3 
#define ledPin   LED_BUILTIN

#define ssid      "Burger Alarm System" // The name of the Wi-Fi network that will be created
#define password  "1234658"             // The password required to connect to it, leave blank for an open
#define HostName  "BugerAlarm.system"

const unsigned int BAUD_RATE=9600;

static bool EnableAlarm = false;
static bool sensorEnabled = false;



// defines variables for HY-SRF05
long duration;
int distance;

void ReadDataFromSensor();


void setup() {
  Serial.begin(BAUD_RATE);    // Starts the serial communication for HY-SRF05
  Serial.println("setup called!");

  pinMode(trigPin, OUTPUT);   // Sets the trigPin as an Output for HY-SRF05
  pinMode(ledPin, OUTPUT);    // Sets the LED as an Output
  pinMode(echoPin, INPUT);    // Sets the echoPin as an Input for HY-SRF05

  // first time , Wifi mode is Station
  WiFi.softAP(ssid, password);          // Start the access point
  Serial.print("Access Point \"");
  Serial.print(ssid);
  Serial.println("\" started");

  Serial.print("IP address:\t");
  Serial.println(WiFi.softAPIP());     // Send the IP address of the ESP8266 to the computer
  
  WiFi.hostname(HostName);  // Set hostName for android application connect
   
}

void loop() {
   if(sensorEnabled) ReadDataFromSensor();
  
}

// read data from HY-SRF05 Sensor
void ReadDataFromSensor(){
    // Clears the trigPin
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);

    // Sets the trigPin on HIGH state for 10 micro seconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);

    // Reads the echoPin, returns the sound wave travel time in microseconds
    duration = pulseIn(echoPin, HIGH);

    // Calculating the distance
    distance= duration*0.034/2;
    // Prints the distance on the Serial Monitor
   if(duration==0)
       Serial.println("Warning: no pulse from sensor");
    else if (distance >= 200)
       Serial.println("Out of range");
    else{
         if (distance <= 80 && distance >= 0) {
            // object detected
            digitalWrite(ledPin, HIGH);
         } else {
           //no object
           digitalWrite(ledPin, LOW);
         }
        Serial.print("distance to nearest object:");
        Serial.print(distance);
        Serial.println(" cm");
    }
    delay(250);
}