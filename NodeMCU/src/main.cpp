#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>
#include <FS.h>   // Include the SPIFFS library
#include "WebSocketsServer.h"
#include "ArduinoJson.h"


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

ESP8266WebServer server(80);    // Create a webserver object that listens for HTTP request on port 80

String getContentType(String filename); // convert the file extension to the MIME type


// defines variables for HY-SRF05
long duration;
int distance;



void ReadDataFromSensor();
bool handleFileRead(String path);       // send the right file to the client (if it exists)
void AddServerListeners();


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
  
  if (MDNS.begin("esp8266"))               // Start the mDNS responder for esp8266.local
       Serial.println("mDNS responder started");
   else 
       Serial.println("Error setting up MDNS responder!");
  

  SPIFFS.begin();                           // Start the SPI Flash Files System   

  AddServerListeners();
  server.begin();                           // Actually start the server
  Serial.println("HTTP server started");

}

void loop() {
   if(sensorEnabled) ReadDataFromSensor();
  
}

void AddServerListeners(){
    server.onNotFound([]() {                              // If the client requests any URI
    if (!handleFileRead(server.uri()))                  // send it if it exists
      server.send(404, "text/plain", "404: Not Found"); // otherwise, respond with a 404 (Not Found) error
  });
}


String getContentType(String filename){
  if(filename.endsWith(".htm")) return "text/html";
  else if(filename.endsWith(".html")) return "text/html";
  else if(filename.endsWith(".css")) return "text/css";
  else if(filename.endsWith(".js")) return "application/javascript";
  else if(filename.endsWith(".png")) return "image/png";
  else if(filename.endsWith(".gif")) return "image/gif";
  else if(filename.endsWith(".jpg")) return "image/jpeg";
  else if(filename.endsWith(".ico")) return "image/x-icon";
  else if(filename.endsWith(".xml")) return "text/xml";
  else if(filename.endsWith(".pdf")) return "application/x-pdf";
  else if(filename.endsWith(".zip")) return "application/x-zip";
  else if(filename.endsWith(".gz")) return "application/x-gzip";
  return "text/plain";
}


bool handleFileRead(String path){  // send the right file to the client (if it exists)
  Serial.println("handleFileRead: " + path);
  if(path.endsWith("/")) path += "index.html";           // If a folder is requested, send the index file
  String contentType = getContentType(path);             // Get the MIME type
  String pathWithGz = path + ".gz";
  if(SPIFFS.exists(pathWithGz) || SPIFFS.exists(path)){  // If the file exists, either as a compressed archive, or normal
    if(SPIFFS.exists(pathWithGz))                          // If there's a compressed version available
      path += ".gz";                                         // Use the compressed version
    File file = SPIFFS.open(path, "r");                    // Open the file
    size_t sent = server.streamFile(file, contentType);    // Send it to the client
    file.close();                                          // Close the file again
    Serial.println(String("\tSent file: ") + path);
    return true;
  }
  Serial.println(String("\tFile Not Found: ") + path);
  return false;                                          // If the file doesn't exist, return false
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

