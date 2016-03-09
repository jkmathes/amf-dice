/*
 * ==============================================
 * CurieDice
 * Copyright (c) 2016 by jkmathes and how2000 @ github
 * ==============================================
 */
 
/*
  ===============================================
  Example sketch for CurieImu library for Intel(R) Curie(TM) devices.
  Copyright (c) 2015 Intel Corporation.  All rights reserved.

  Based on I2C device class (I2Cdev) demonstration Arduino sketch for MPU6050
  class by Jeff Rowberg: https://github.com/jrowberg/i2cdevlib

  ===============================================
  I2Cdev device library code is placed under the MIT license
  Copyright (c) 2011 Jeff Rowberg

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  ===============================================
*/
#include <CurieBle.h>
#include "CurieImu.h"
#include <Adafruit_NeoPixel.h>

// SERIAL_DEBUG Macro
// During normal run, set debug to 0.
// System will wait indefinitely on SERIAL_SETUP() if set to 1 without serial hookup.
#define SERIAL_DEBUG 0
#if SERIAL_DEBUG
#define SERIAL_SETUP(baud)  Serial.begin(baud);while (!Serial)
#define SERIAL_PRINT        Serial.print
#define SERIAL_PRINTLN      Serial.println
#else
#define SERIAL_SETUP(baud)
#define SERIAL_PRINT(...)
#define SERIAL_PRINTLN(...)
#endif

unsigned long ledLapse;
uint8_t ledState = LOW;

#define VIBE_PIN        7
// ************* NeoPixel ********************************************
#define PIXEL_PIN       6     // Where the NeoPixel DIN pin is connected
#define NUMPIXELS       8     // Number of LEDS on NeoPixel
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIXEL_PIN, NEO_GRB + NEO_KHZ800);

void dispLed(int mask, int color, int dlay) {
  int r = (color>>8)&0xf;
  int g = (color>>4)&0xf;
  int b = color&0xf;
  int c = (r<<16) | (g<<8) | (b);  
  for (int i=0; i<NUMPIXELS; i++)
  {
    if (mask&(1<<i)) pixels.setPixelColor(i,c);
  }
  pixels.show();
  delay(10);
  pixels.show();  // 2nd time helps fix a bug with bright LED0 issue.
  delay(10);
  delay(dlay);
}

// ************* BLE **************************************************
BLEPeripheral blePeripheral;        // BLE Peripheral Device (the board you're programming)
BLEService diceService("0x1812");  // BLE Service
BLEUnsignedCharCharacteristic diceRollCharacteristic("0x2AC5", BLERead|BLEWrite|BLENotify);//20); // payload size
BLEUnsignedCharCharacteristic diceCommandCharacteristic("0x2A9F", BLERead|BLEWrite|BLENotify);

boolean gConnect = false;
unsigned char diceId = 1;
uint16_t  seqNum = 0;
uint16_t nextSeqNum() {
  seqNum++;
  return seqNum;
}
bool rollSent = false;
bool registered = false;
// ************* IMU **************************************************
int16_t vraw[10]; // raw readings from IMU and info for sending via BLE
                  // [0:5]=AxAyAzGxGyGz, [6]=movement, [7]=stillMs, [8]=diceId, [9]=seqNum
int vprev[6];     // previous reading

#define MOVE_THRESHOLD  32
#define GOOD_ROLL       4
int16_t movement=0;
int16_t maxMove[6];
int16_t moves=0;
unsigned long stillBegin;
unsigned long stillMillis=0;   // number of sequential zero movements


void idleShow()
{
  static int imask = 0x1;
  static int dir = 1;
  static int prev = 0;
  if (stillMillis < 5000) return;
  int p = stillMillis/150;
  if (p!=prev) {
    prev = p;
    if (imask==0x01) dir = 1;
    if (imask==0x80) dir = 0;
    if (dir) imask = imask << 1;
    else imask = imask >> 1;
    dispLed(0xff,0x000,0);
    dispLed(imask,0xff0,0);
  }
}

int16_t imuRead() {
  for (int i=0; i<6; i++) vprev[i] = vraw[i];
  CurieImu.getMotion6(&vraw[0], &vraw[1], &vraw[2], &vraw[3], &vraw[4], &vraw[5]);
  movement = 0;

  for (int i=0; i<6; i++) {
    int m = (abs(vprev[i]-vraw[i])) >> 8;
    if (m>movement) movement = m;
    if (maxMove[i]==0 && m>MOVE_THRESHOLD) { maxMove[i]=m; moves++;}
  }
  // movement is 8 bits
  int mask = 0x0;
  int t = 0x1;
  for (int i=0; i<8; i++) {
    if (movement > (t<<i)) mask = (mask << 1) | 1;
  }
  if (movement==0) { // no movement
    if (stillMillis==0) {
      stillBegin = millis();
      stillMillis = 1;
      rollSent = false;
    }
    else {
      stillMillis = millis() - stillBegin;
      if (stillMillis > 250) {
        if (moves < GOOD_ROLL) {
          dispLed(0xff, 0x000, 0);
          dispLed(0xff, 0xf00, 0);
          moves=0;
          for (int i=0; i<6; i++) maxMove[i]=0;
        }
        else if (!rollSent) {
          int a = vraw[0]>>8;
          int b = vraw[1]>>8;
          int c = vraw[2]>>8;
          unsigned char roll=0;
          int disp=0;
          if (a>0x1d && a<0x3d && b>-10 && b<13 && c>0x12 && c<0x32)
            {roll=1; dispLed(0x1,0x0f0, 30);}
          else if (a>0x13 && a<0x37 && b>-13 && b<13 && c>(signed char)0xb3 && c<(signed char)0xd6)
            {roll=2; disp=0x3;}
          else if (a>(signed char)0xee && a<10 && b>(signed char)0xb1 && b<(signed char)0xd1 && c>(signed char)0xe6 && c<10)
            {roll=3; disp=0x7;}
          else if (a>(signed char)0xed && a<10 && b>0x30 && b<0x50 && c>(signed char)0xeb && c<10)
            {roll=4; disp=0x17;}
          else if (a>(signed char)0xc5 && a<(signed char)0xe0 && b>-10 && b<10 && c>0x1a && c<0x36)
            {roll=5; disp=0x37;}
          else if (a>(signed char)0xbb && a<(signed char)0xdb && b>-10 && b<10 && c>(signed char)0xbb && c<(signed char)0xdb)
            {roll=6; disp=0x77;}
          if (roll!=0) {
            dispLed(disp, 0x0f0, 50);
            diceRollCharacteristic.setValue((diceId << 4) | roll);            
            rollSent = true;
          }
          else
          {
            moves=0;
            for (int i=0; i<6; i++) maxMove[i]=0;
          }
        } else { // rollSent
          idleShow();
        }
      }
    }
  }
  else
  {
    if (rollSent==true) {
      moves=0;
      for (int i=0; i<6; i++) maxMove[i]=0;
    }
    stillMillis=0;
  }
  
  SERIAL_PRINT(movement);
  SERIAL_PRINT("\t");
  
  SERIAL_PRINTLN(stillMillis);

  if (stillMillis <=250) {
    dispLed(0xff, 0x000, 0);
    dispLed(mask, 0x00f, 0);
  }

  if (movement > 64)
  {
    digitalWrite(VIBE_PIN,1);
    delay(50);
    digitalWrite(VIBE_PIN,0);
    delay(50);
    digitalWrite(VIBE_PIN,1);
    delay(50);
    digitalWrite(VIBE_PIN,0);
  }
  return movement;
}

unsigned char readDiceId()
{
  int a12 = digitalRead(12);
  int a11 = digitalRead(11);
  int a10 = digitalRead(10);
  int a = (a12<<2)|(a11<<1)|a10;
  switch (a) {
    case 7: diceId = 0; break;
    case 3: diceId = 1; break;
    case 5: diceId = 2; break;
    case 6:
    default: diceId = 3; break;
  }
  return diceId;
}

const int ledPin = 13;      // activity LED pin
boolean blinkState = false; // state of the LED
// ************* setup **************************************************
void setup() {
  // diceId read pins
  pinMode(12, INPUT_PULLUP);
  pinMode(11, INPUT_PULLUP);
  pinMode(10, INPUT_PULLUP);
  
  // onboard LED
  pinMode(ledPin, OUTPUT);
  
  pinMode(VIBE_PIN, OUTPUT);
  digitalWrite(VIBE_PIN,0);
  
  pixels.begin();
  pixels.show();
  dispLed(0xff, 0x0, 0);        // all off
  dispLed(0xff, 0xfff, 1000);   // all white 1-sec
  dispLed(0xff, 0xff0, 500);
  dispLed(0xff, 0x0f0, 500);
  dispLed(0xff, 0x000, 0);
  
  SERIAL_SETUP(9600);

  dispLed(0x01, 0xff0, 0);      // 0 - yellow BLE setup

// *********** BLE Setup **************************************
  /* Set a local name for the BLE device
     This name will appear in advertising packets
     and can be used by remote devices to identify this BLE device
     The name can be changed but maybe be truncated based on space left in advertisement packet */
  blePeripheral.setLocalName("CurieDice");
  blePeripheral.setAdvertisedServiceUuid(diceService.uuid());
  blePeripheral.addAttribute(diceService);
  blePeripheral.addAttribute(diceCommandCharacteristic);
  blePeripheral.addAttribute(diceRollCharacteristic);
  blePeripheral.setEventHandler(BLEConnected, connectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, disconnectHandler);
  diceCommandCharacteristic.setEventHandler(BLEWritten, commandHandler);
  blePeripheral.begin();
  SERIAL_PRINTLN("Bluetooth device active, waiting for connections...");

  dispLed(0x01, 0x0f0, 500);      // 0 - green BLE setup done

  dispLed(0x02, 0xff0, 500);      // 1 - yellow IMU setup

  // *************** IMU Setup ***************************************
  // initialize device
  SERIAL_PRINTLN("Initializing IMU device...");
  CurieImu.initialize();

  // verify connection
  SERIAL_PRINTLN("Testing device connections...");
  if (CurieImu.testConnection()) {
    SERIAL_PRINTLN("CurieImu connection successful");
  } else {
    SERIAL_PRINTLN("CurieImu connection failed");
    dispLed(0x80,0xf00,0);      // 8 - red - err connecting to IMU
  }
  
  // use the code below to calibrate accel/gyro offset values
  SERIAL_PRINTLN("Internal sensor offsets BEFORE calibration...");
  SERIAL_PRINT(CurieImu.getXAccelOffset()); 
  SERIAL_PRINT("\t"); // -76
  SERIAL_PRINT(CurieImu.getYAccelOffset()); 
  SERIAL_PRINT("\t"); // -235
  SERIAL_PRINT(CurieImu.getZAccelOffset()); 
  SERIAL_PRINT("\t"); // 168
  SERIAL_PRINT(CurieImu.getXGyroOffset()); 
  SERIAL_PRINT("\t"); // 0
  SERIAL_PRINT(CurieImu.getYGyroOffset()); 
  SERIAL_PRINT("\t"); // 0
  SERIAL_PRINT(CurieImu.getZGyroOffset());
  
  dispLed(0x02, 0x00f, 200);      // 1 - blue IMU - orient dice for calibration

  CurieImu.setGyroOffsetEnabled(true);
  CurieImu.setAccelOffsetEnabled(true);

  dispLed(0x02, 0x0f0, 0);      // 1 - green IMU setup done
  readDiceId();

  for (int i=0; i<6; i++) maxMove[i]=0;
  ledLapse = millis();
}



void atten(int16_t *a, int len, int shiftBy)
{
  for (int i=0; i<len; i++) {
    a[i] = a[i] >> shiftBy;
  }
}

void connectHandler(BLECentral& central)
{
  gConnect = true;
  SERIAL_PRINT("Connected to central: ");
  // print the central's MAC address:
  SERIAL_PRINTLN(central.address());
  // turn on the LED to indicate the connection:
  digitalWrite(13, HIGH);  
}

void disconnectHandler(BLECentral& central)
{
  gConnect = false;
  SERIAL_PRINT("Disconnected from central: ");
  digitalWrite(13, LOW);  
}

#define COMMAND_REGISTER      1
#define COMMAND_REGISTER_ACK  2
#define COMMAND_BUZZ          5

void commandHandler(BLECentral &central, BLECharacteristic &characteristic)
{
  int cvalue = diceCommandCharacteristic.value();
  switch(cvalue) {
    case COMMAND_REGISTER_ACK: registered = true; break;
    case COMMAND_BUZZ:
      digitalWrite(VIBE_PIN,1);
      delay(50);
      digitalWrite(VIBE_PIN,0);
      delay(50);
      digitalWrite(VIBE_PIN,1);
      delay(50);
      digitalWrite(VIBE_PIN,0);
      break;
  }
}

void registerDice() {
  diceCommandCharacteristic.setValue((diceId << 4) | COMMAND_REGISTER);
}

void loop() {
  BLECentral central = blePeripheral.central();
  if (central) {
    while (central.connected()) {
      if (!registered) {
        registerDice();
      }

      blePeripheral.poll();

      imuRead();
      
      if ((millis()-ledLapse) > 1000) {
        ledLapse = millis();
        ledState = !ledState;
        digitalWrite(13,ledState);
      }
    }
  }
}
