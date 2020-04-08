#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <FS.h>   // Include the SPIFFS library
#include <ESP8266mDNS.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266WebServer.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>
#include "ArduinoJson.h"
#include "Config.h"



// defines pins numbers for HY-SRF05
#define trigPin  D4 
#define echoPin  D3 
#define ledPin   LED_BUILTIN

#define board_ssid      "Burglar Alarm System" // The name of the Wi-Fi network that will be created
#define board_password  "11111111"            // The password required to connect to it, leave blank for an open
#define mdnsName  "burglaralarm"

const char* filename = "/config.json"; 
const String ServerURL = "http://192.168.1.102:2000";

const unsigned int BAUD_RATE=9600;
String device_id = "";

static bool EnableAlarm = false;

ESP8266WiFiMulti WiFiMulti;
ESP8266WebServer server(80);       // Create a webserver object that listens for HTTP request on port 80

String formatBytes(size_t);
String getContentType(String); // convert the file extension to the MIME type
void setServerListeners();
char* unconstchar(const char* s);
// defines variables for HY-SRF05
long duration;
int distance;
int max_distance;

void startServer();         
void startMDNS();
void startSPIFFS();
void connectToWifi(const char*,const char*);

void ReadDataFromSensor();
void saveConfig(const char*);
String getConfig();
String getValue(String , char , int );

// http requests
void registerUser(const char*);
void setActiveSensor(const char*);
bool isActiveAlarm(const char*);

void ledLoading();
int loopCounter;

void setup() {
  Serial.begin(BAUD_RATE);    // Starts the serial communication for HY-SRF05
  Serial.println("setup called!");
 
  delay(10);

  pinMode(trigPin, OUTPUT);   // Sets the trigPin as an Output for HY-SRF05
  pinMode(ledPin, OUTPUT);    // Sets the LED as an Output
  pinMode(echoPin, INPUT);    // Sets the echoPin as an Input for HY-SRF05

  // Wifi mode set both Station and client
  WiFi.mode(WIFI_AP_STA);
  WiFi.softAP(board_ssid, board_password);          // Start the access point
  Serial.print("Access Point \"");
  Serial.print(board_ssid);
  Serial.println("\" started");

  Serial.print("IP address:\t");
  Serial.println(WiFi.softAPIP());     // Send the IP address of the ESP8266 to the computer
  
  startSPIFFS();               // Start the SPIFFS and list all contents
  startMDNS();
  startServer();


  String config = getConfig();
  if(!config.isEmpty()){
    max_distance = getValue(config,' ',3).toInt();
    device_id = getValue(config,' ',0);
    connectToWifi(getValue(config,' ',1).c_str(),getValue(config,' ',2).c_str());
  } 
  else Serial.println("Cant find Config");
}


void startSPIFFS() { // Start the SPIFFS and list all contents
  SPIFFS.begin();                             // Start the SPI Flash File System (SPIFFS)
  Serial.println("SPIFFS started. Contents:");
  {
    Dir dir = SPIFFS.openDir("/");
    while (dir.next()) {                      // List the file system contents
      String fileName = dir.fileName();
      size_t fileSize = dir.fileSize();
      Serial.printf("\tFS File: %s, size: %s\r\n", fileName.c_str(), formatBytes(fileSize).c_str());
    }
    Serial.printf("\n");
  }
}

void startMDNS() { // Start the mDNS responder
  MDNS.begin(mdnsName);                        // start the multicast domain name server
  Serial.print("mDNS responder started: http://");
  Serial.print(mdnsName);
  Serial.println(".local");

  // Add service to MDNS
  MDNS.addService("http", "tcp", 80);
}

void startServer() { // Start a HTTP server with a file read handler and an upload handler
  setServerListeners();
  server.begin();                             // start the HTTP server
  Serial.println("HTTP server started.");
}

void connectToWifi(const char* ssid,const char* pass){
    int count;
    Serial.printf("ssid : %s , pass : %s \r\n",ssid,pass);
    WiFiMulti.addAP(ssid,pass);


    while ((WiFiMulti.run() != WL_CONNECTED)) 
    {
     delay(500);
     Serial.print(".");
    }

   Serial.println("");
   Serial.println("WiFi connected"); 
}


void loop() {
   if(device_id != nullptr && !device_id.isEmpty()){
       delay(100);
       if(loopCounter == 10){
           EnableAlarm = isActiveAlarm(device_id.c_str());
           loopCounter = 0;
       }
       if(EnableAlarm) ReadDataFromSensor();
       loopCounter++;
   }
   
   server.handleClient();                      // run the server
}


void registerUser(const char* device_id){
    Serial.print("registerUser called with : ");
    Serial.println(device_id);

    ledLoading();

    HTTPClient http;
    http.begin(ServerURL + "/board/register");
    http.addHeader("Content-Type", "application/json");


    int code = http.POST(String(device_id));
    Serial.println(code);
     
    http.end();
}

bool isActiveAlarm(const char* device_id){
   
    HTTPClient http;
    http.begin(ServerURL + "/board/getAlarmStatus?did=" + String(device_id));
    http.addHeader("Content-Type", "application/json");

    int httpCode = http.GET();                                                                  //Send the request
 
    if (httpCode == 200) { //Check the returning code
         ledLoading();
         String payload = http.getString();           //Get the request response payload
         //Serial.println(payload);                     //Print the response payload

         StaticJsonDocument<100> recv_doc;
         deserializeJson(recv_doc,payload);
         http.end();

         return recv_doc["status"].as<bool>();
      }
      else Serial.println("isActiveAlarm err : " + http.errorToString(httpCode));

    http.end();
    return false;
}

void setActiveSensor(const char* device_id){
    ledLoading();

    HTTPClient http;
    http.begin(ServerURL + "/board/setSensorStatus");
    http.addHeader("Content-Type", "application/json");


    String json_string;
    StaticJsonDocument<50> send_doc;
    send_doc["device_id"] = device_id;
    send_doc["status"] = true;
    serializeJsonPretty(send_doc, json_string);

    int code = http.POST(json_string);
    if (code == 200) EnableAlarm = false;
    http.end();
}

String formatBytes(size_t bytes) { // convert sizes in bytes to KB and MB
  if (bytes < 1024) {
    return String(bytes) + "B";
  } else if (bytes < (1024 * 1024)) {
    return String(bytes / 1024.0) + "KB";
  } else if (bytes < (1024 * 1024 * 1024)) {
    return String(bytes / 1024.0 / 1024.0) + "MB";
  }
  return String("");
}

void saveConfig(String data){
        //Format File System
        ledLoading();
      if(SPIFFS.format())
        Serial.println("File System Formated");
      
      else
        Serial.println("File System Formatting Error");
      
    
      //Create New File And Write Data to It
      //w=Write Open file for writing
      File f = SPIFFS.open(filename, "w");
      
      if (!f) 
        Serial.println("file open failed");
      
      else
      {
          //Write data to file
          Serial.println("Writing Data to File");
          f.print (data);
          Serial.println("Writing Done!");
          f.close();  //Close file
      }
}

String getConfig(){
       ledLoading();
      //w=Write Open file for writing
      File f = SPIFFS.open(filename, "r");
      
      if (!f) 
        Serial.println("file open failed");
      
      else
      {
      Serial.println("Reading Data from File:");
      //Data from file
      String data = "";
      for(int i=0;i<f.size();i++) //Read upto complete file size
        data += (char) f.read();
      
      f.close();  //Close file
      Serial.println("File Closed");
      Serial.println("Data is : " + data);
       
      return data;
     }
    return "";
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
         if (distance <= max_distance && distance >= 0) {
            // object detected
            digitalWrite(ledPin, HIGH);
            setActiveSensor(device_id.c_str());
         } else {
           //no object
           digitalWrite(ledPin, LOW);
         }
        Serial.print("distance to nearest object:");
        Serial.print(distance);
        Serial.println(" cm");
    }
}


void setServerListeners(){
    server.onNotFound([](){
      server.send(404, "text/plain", "404: Not Found");
    });

    server.on("/config",HTTP_POST,[](){

        ledLoading();
        StaticJsonDocument<300> recv_doc;
        deserializeJson(recv_doc,server.arg("plain"));

        const char* did = recv_doc["device_id"];
        const char* ssid = recv_doc["ssid"];
        const char* pass = recv_doc["pass"];
        const int distance = recv_doc["max_distance"].as<int>();

        String config = String(did)    + String(" ") 
                        + String(ssid) + String(" ") 
                        + String(pass) + String(" ") 
                        + String(distance) ;
        Serial.println("New Config is :" + config);

        String json_string;
        StaticJsonDocument<100> send_doc;
        send_doc["status"] = true;
        send_doc["msg"] = "added Done";
        serializeJsonPretty(send_doc, json_string);

        Serial.println("Send Json : " + json_string);
        server.send(200,"application/json",json_string);

        connectToWifi(ssid,pass);
        registerUser(did);

        max_distance = distance;
        device_id = String(did);

        saveConfig(config);
    });
}


char* unconstchar(const char* s) {
    int i;
    char* res;
    for (i = 0; s[i] != '\0'; i++) {
        res[i] = s[i];
    }
    res[i] = '\0';
    return res;
}

String getValue(String data, char separator, int index)
{
    int found = 0;
    int strIndex[] = { 0, -1 };
    int maxIndex = data.length() - 1;

    for (int i = 0; i <= maxIndex && found <= index; i++) {
        if (data.charAt(i) == separator || i == maxIndex) {
            found++;
            strIndex[0] = strIndex[1] + 1;
            strIndex[1] = (i == maxIndex) ? i+1 : i;
        }
    }
    return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}

void ledLoading(){
    digitalWrite(ledPin, HIGH);
    delay(50);
    digitalWrite(ledPin, LOW);
}

