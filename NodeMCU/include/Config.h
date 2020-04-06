class Config{
   public:
    char* device_id;
    char* ssid;
    char* password;
    int   distance;
    bool  ok;

  Config(char* deviceid,char* _ssid,char* _password,int _distance);
  Config();
};

Config::Config(char* deviceid,char* _ssid,char* _password,int _distance){
      device_id = deviceid;
      ssid = _ssid;
      password = _password;
      distance = _distance;
      ok = true;
   }

Config::Config(){
     ok = false;
   }