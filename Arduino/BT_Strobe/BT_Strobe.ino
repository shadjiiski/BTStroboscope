const int LED_PIN = 6; //the output pin (where the LED is connected)
const int LED_ECHO = 13; //echo will be outputed on built-in LED (pin 13)
unsigned long whole_period = 33333; //micros, determined by the frequency, default is 30 Hz
unsigned long light_period = 1667; //micros, determined by the duty cycle, default is 5%
bool emission;
unsigned long last_time;

void setup() {
  Serial.begin(9600); // Strobe delays will be set via serial communication over bluetooth
  pinMode(LED_PIN, OUTPUT);
  pinMode(LED_ECHO, OUTPUT);
  initStrobe();
}

void loop() {
  unsigned long delta = micros() - last_time;
  if(delta < 0) //overflowed...
    initStrobe();
  else if(emission && (delta > light_period) && (delta < whole_period)){ // don't turn off if already too late
    digitalWrite(LED_PIN, LOW);
    digitalWrite(LED_ECHO, LOW);
    emission = false;
  } else if(!emission && (delta > whole_period) && (delta < whole_period + light_period)){ // don't tur on if already too late
    digitalWrite(LED_PIN, HIGH);
    digitalWrite(LED_ECHO, HIGH);
    emission = true;
    last_time += whole_period;
  }
}

void serialEvent(){
  if(Serial.available() < 7) // in this order: 3 bytes for whole_period, 3 bytes for light_period and 1 byte checksum
    return;
  unsigned int b, check = 0;
  unsigned long wp = 0, lp = 0;
  for(int i = 0; i < 6; ++i){
    b = Serial.read();
    if (i < 3)
      wp = b | wp << 8;
    else
      lp = b | lp << 8;
    check += b;
  }
  check = (~check) & 0xFF; // target check sum: the last byte of the one's complement of the sum of the first four received bytes
  b = Serial.read(); // received checksum
/*
  Serial.print("wp=");
  Serial.print(wp);
  Serial.print("\tlp=");
  Serial.print(lp);
  Serial.print("\tcheck=");
  Serial.print(b);
  Serial.print("expected=");
  Serial.println(check);
*/
  if(b == check){
    if(checkAndSetPeriods(wp, lp))
      Serial.print("ACK"); // Acknowledge
    else
      Serial.print("ERR"); // Wrong params
  }
  else
    Serial.print("SUM"); // wrong checksum
}

void initStrobe(){
  emission = true;
  last_time = micros();
  digitalWrite(LED_PIN, HIGH);
  digitalWrite(LED_ECHO, HIGH);
}

bool checkAndSetPeriods(unsigned long wp, unsigned long lp){
  if(wp < 10000) //max freq is 100 Hz
    return false;
  if(wp > 1000000) //min frequency is 1 Hz
    return false;
  if(lp < (unsigned long) (0.5 + 0.01 * wp)) //min 1% duty cycle
    return false;
  if(lp > (unsigned long)(0.5 + 0.99 * wp)) //max 99% duty cycle
    return false;
  whole_period = wp;
  light_period = lp;
  initStrobe();
  return true;
}

