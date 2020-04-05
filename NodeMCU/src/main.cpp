#include <Arduino.h>

// defines pins numbers for HY-SRF05
const int trigPin = 2;  //D4
const int echoPin = 0;  //D3


const unsigned int BAUD_RATE=9600;

// defines variables for HY-SRF05
long duration;
int distance;

void ReadDataFromSensor();


void setup() {
  Serial.begin(BAUD_RATE);    // Starts the serial communication for HY-SRF05
  Serial.println("setup called!");

  pinMode(trigPin, OUTPUT);   // Sets the trigPin as an Output for HY-SRF05
  pinMode(echoPin, INPUT);    // Sets the echoPin as an Input for HY-SRF05
 }

void loop() {
   ReadDataFromSensor();
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
    else{
        Serial.print("distance to nearest object:");
        Serial.print(distance);
        Serial.println(" cm");
    }
    delay(100);
}