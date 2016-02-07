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
  pixels.show();  // 2nd time helps fix a bug with bright LED0 issue.
  delay(dlay);
}

// ************* BLE **************************************************
BLEPeripheral blePeripheral;        // BLE Peripheral Device (the board you're programming)
BLEService batteryService("180F");  // BLE Service
BLECharacteristic batteryLevelChar( // BLE Characteristic
  "2A19",                 // standard 16-bit characteristic UUID
  BLERead | BLENotify,    // remote clients can get notifications if characteristic changes
  20);                    // payload size
boolean gConnect = false;
int16_t   diceId = 1;
uint16_t  seqNum = 0;
uint16_t nextSeqNum() {
  seqNum++;
  return seqNum;
}

// ************* IMU **************************************************
int16_t vraw[10]; // raw readings from IMU and info for sending via BLE
                  // [0:5]=AxAyAzGxGyGz, [6]=movement, [7]=stillMs, [8]=diceId, [9]=seqNum
int vprev[6];     // previous reading

int16_t movement=0;
unsigned long stillBegin;
unsigned long stillMillis=0;   // number of sequential zero movements


int16_t imuRead() {
  for (int i=0; i<6; i++) vprev[i] = vraw[i];
  CurieImu.getMotion6(&vraw[0], &vraw[1], &vraw[2], &vraw[3], &vraw[4], &vraw[5]);
  movement = 0;
  for (int i=0; i<6; i++) {
    int m = (abs(vprev[i]-vraw[i])) >> 8;
    if (m>movement) movement = m;
  }
  // movement is 8 bits
  int mask = 0x0;
  int t = 0x1;
  for (int i=0; i<8; i++) {
    if (movement > (t<<i)) mask = (mask << 1) | 1;
  }
  if (movement==0) {
    if (stillMillis==0) {
      stillBegin = millis();
      stillMillis = 1;
    }
    else
      stillMillis = millis() - stillBegin;
  }
  else stillMillis=0;
  
  SERIAL_PRINT(movement);
  SERIAL_PRINT("\t");
  
  SERIAL_PRINTLN(stillMillis);
  
  dispLed(0xff, 0x000, 0);
  dispLed(mask, 0x00f, 0);

  return movement;
}


const int ledPin = 13;      // activity LED pin
boolean blinkState = false; // state of the LED
// ************* setup **************************************************
void setup() {
  pixels.begin();
  pixels.show();
  dispLed(0xff, 0x0, 0);        // all off
  dispLed(0xff, 0xfff, 1000);   // all white 1-sec
  dispLed(0xff, 0xff0, 500);
  dispLed(0xff, 0x0f0, 500);
  dispLed(0xff, 0x000, 0);
  
  SERIAL_SETUP(9600);

  dispLed(0x01, 0xff0, 0);      // 0 - yellow BLE setup

// *********** BLE **************************************
  /* Set a local name for the BLE device
     This name will appear in advertising packets
     and can be used by remote devices to identify this BLE device
     The name can be changed but maybe be truncated based on space left in advertisement packet */
  blePeripheral.setLocalName("CurieDice");
  blePeripheral.setAdvertisedServiceUuid(batteryService.uuid());  // add the service UUID
  blePeripheral.addAttribute(batteryService);   // Add the BLE Battery service
  blePeripheral.addAttribute(batteryLevelChar); // add the battery level characteristic
  blePeripheral.setEventHandler(BLEConnected, connectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, disconnectHandler);
  batteryLevelChar.setValue((unsigned char *)vraw,20);   // initial value for this characteristic

  /* Now activate the BLE device.  It will start continuously transmitting BLE
     advertising packets and will be visible to remote BLE central devices
     until it receives a new connection */
  blePeripheral.begin();
  SERIAL_PRINTLN("Bluetooth device active, waiting for connections...");

  dispLed(0x01, 0x0f0, 0);      // 0 - green BLE setup done

  dispLed(0x02, 0xff0, 0);      // 1 - yellow IMU setup

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

  // To manually configure offset compensation values, 
  // use the following methods instead of the autoCalibrate...() methods below
  //    CurieImu.setXGyroOffset(220);
  //    CurieImu.setYGyroOffset(76);
  //    CurieImu.setZGyroOffset(-85);
  //    CurieImu.setXAccelOffset(-76);
  //    CurieImu.setYAccelOffset(-235);
  //    CurieImu.setZAccelOffset(168);

  dispLed(0x02, 0x00f, 0);      // 1 - blue IMU - orient dice for calibration

  //SERIAL_PRINTLN("About to calibrate. Make sure your board is stable and upright");
  //delay(5000);
  
  //// The board must be resting in a horizontal position for 
  //// the following calibration procedure to work correctly!
  //SERIAL_PRINT("Starting Gyroscope calibration...");
  //CurieImu.autoCalibrateGyroOffset();
  //SERIAL_PRINTLN(" Done");
  //SERIAL_PRINT("Starting Acceleration calibration...");
  //CurieImu.autoCalibrateXAccelOffset(0);
  //CurieImu.autoCalibrateYAccelOffset(0);
  //CurieImu.autoCalibrateZAccelOffset(1);
  //SERIAL_PRINTLN(" Done");

  //SERIAL_PRINTLN("Internal sensor offsets AFTER calibration...");
  //SERIAL_PRINT(CurieImu.getXAccelOffset());
  //SERIAL_PRINT("\t"); // -76
  //SERIAL_PRINT(CurieImu.getYAccelOffset());
  //SERIAL_PRINT("\t"); // -2359
  //SERIAL_PRINT(CurieImu.getZAccelOffset());
  //SERIAL_PRINT("\t"); // 1688
  //SERIAL_PRINT(CurieImu.getXGyroOffset());
  //SERIAL_PRINT("\t"); // 0
  //SERIAL_PRINT(CurieImu.getYGyroOffset());
  //SERIAL_PRINT("\t"); // 0
  //SERIAL_PRINTLN(CurieImu.getZGyroOffset());

  //SERIAL_PRINTLN("Enabling Gyroscope/Acceleration offset compensation");
  CurieImu.setGyroOffsetEnabled(true);
  CurieImu.setAccelOffsetEnabled(true);

  // configure Arduino LED for activity indicator
  pinMode(ledPin, OUTPUT);

  dispLed(0x02, 0x0f0, 0);      // 1 - green IMU setup done

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
  SERIAL_PRINT("Disconnected from central: ");
  gConnect = false;
  digitalWrite(13, LOW);  
}

void loop() {
  blePeripheral.poll();       // current implementation just does delay(1);

  imuRead();
    
  if (gConnect) {
    SERIAL_PRINT("Sending: ");
    vraw[6] = (int16_t) movement;
    int16_t stillDur = (stillMillis>32000)? 32000 : stillMillis;
    vraw[7] = stillDur;
    vraw[8] = diceId;
    vraw[9] = nextSeqNum();
    batteryLevelChar.setValue((unsigned char*) vraw, 20);
  }
}
