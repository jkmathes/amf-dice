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


// ************* BLE **************************************************
BLEPeripheral blePeripheral;       // BLE Peripheral Device (the board you're programming)
BLEService batteryService("180F"); // BLE Battery Service

// BLE Battery Level Characteristic"
BLECharacteristic batteryLevelChar("2A19",  // standard 16-bit characteristic UUID
    BLERead | BLENotify, 20);     // remote clients will be able to
// get notifications if this characteristic changes

int oldBatteryLevel = 0;  // last battery level reading from analog input
long previousMillis = 0;  // last time the battery level was checked, in ms

char blePayload[20];

// ************* IMU **************************************************
int16_t ax, ay, az;         // accelerometer values
int16_t gx, gy, gz;         // gyrometer values

const int ledPin = 13;      // activity LED pin
boolean blinkState = false; // state of the LED

int16_t vval[6];
int vmin[6];
int vmax[6];
int vlast[6];
int16_t va[6];
boolean gConnect = false;

void setup() {
  //Serial.begin(9600); // initialize Serial communication
  //while (!Serial);    // wait for the serial port to open
  SERIAL_SETUP(9600);


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
  //batteryLevelChar.setValue(oldBatteryLevel);   // initial value for this characteristic
  batteryLevelChar.setValue((unsigned char *)vval,6);   // initial value for this characteristic

  /* Now activate the BLE device.  It will start continuously transmitting BLE
     advertising packets and will be visible to remote BLE central devices
     until it receives a new connection */
  blePeripheral.begin();
  SERIAL_PRINTLN("Bluetooth device active, waiting for connections...");





  // initialize device
  SERIAL_PRINTLN("Initializing IMU device...");
  CurieImu.initialize();

  // verify connection
  SERIAL_PRINTLN("Testing device connections...");
  if (CurieImu.testConnection()) {
    SERIAL_PRINTLN("CurieImu connection successful");
  } else {
    SERIAL_PRINTLN("CurieImu connection failed");
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

  SERIAL_PRINT("Size of int is: ");
  SERIAL_PRINTLN(sizeof(int));
  SERIAL_PRINT("Size of long is: ");
  SERIAL_PRINTLN(sizeof(long));
  SERIAL_PRINT("Size of float is: ");
  SERIAL_PRINTLN(sizeof(float));
  SERIAL_PRINT("Size of double is: ");
  SERIAL_PRINTLN(sizeof(double));
  SERIAL_PRINTLN("About to calibrate. Make sure your board is stable and upright");
  delay(5000);
  
  // The board must be resting in a horizontal position for 
  // the following calibration procedure to work correctly!
  SERIAL_PRINT("Starting Gyroscope calibration...");
  CurieImu.autoCalibrateGyroOffset();
  SERIAL_PRINTLN(" Done");
  SERIAL_PRINT("Starting Acceleration calibration...");
  CurieImu.autoCalibrateXAccelOffset(0);
  CurieImu.autoCalibrateYAccelOffset(0);
  CurieImu.autoCalibrateZAccelOffset(1);
  SERIAL_PRINTLN(" Done");

  SERIAL_PRINTLN("Internal sensor offsets AFTER calibration...");
  SERIAL_PRINT(CurieImu.getXAccelOffset());
  SERIAL_PRINT("\t"); // -76
  SERIAL_PRINT(CurieImu.getYAccelOffset());
  SERIAL_PRINT("\t"); // -2359
  SERIAL_PRINT(CurieImu.getZAccelOffset());
  SERIAL_PRINT("\t"); // 1688
  SERIAL_PRINT(CurieImu.getXGyroOffset());
  SERIAL_PRINT("\t"); // 0
  SERIAL_PRINT(CurieImu.getYGyroOffset());
  SERIAL_PRINT("\t"); // 0
  SERIAL_PRINTLN(CurieImu.getZGyroOffset());

  SERIAL_PRINTLN("Enabling Gyroscope/Acceleration offset compensation");
  CurieImu.setGyroOffsetEnabled(true);
  CurieImu.setAccelOffsetEnabled(true);

  // configure Arduino LED for activity indicator
  pinMode(ledPin, OUTPUT);

  //CurieImu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
  CurieImu.getMotion6(&vval[0], &vval[1], &vval[2], &vval[3], &vval[4], &vval[5]);



  for (int i=0; i<6; i++) {
    vmin[i] = vval[i];
    vmax[i] = vval[i];
    vlast[i] = vval[i];
  }

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
  blePeripheral.poll(); // currently does delay(1);
  // listen for BLE peripherals to connect:
  //BLECentral central = blePeripheral.central();
    // if a central is connected to peripheral:

  CurieImu.getMotion6(&vval[0], &vval[1], &vval[2], &vval[3], &vval[4], &vval[5]);
  
  for (int i=0; i<6; i++) va[i] = vval[i];
  atten(va, 6, 10);

  
  if (gConnect) {
    SERIAL_PRINT("Sending: ");
    batteryLevelChar.setValue((unsigned char*) va, 6);
  }
  if (1) {
    for (int i=0; i<6; i++) {
      SERIAL_PRINT(va[i]);
      SERIAL_PRINT("\t");
    }
    //SERIAL_PRINTLN("");
  }

  SERIAL_PRINT(" ---- ");


  for (int i=0; i<6; i++) {
    SERIAL_PRINT(abs(vlast[i]-vval[i]));
    SERIAL_PRINT("\t");
    vlast[i] = vval[i];
  }
  SERIAL_PRINTLN("");

  
#if 0  
  // read raw accel/gyro measurements from device
  //CurieImu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
  CurieImu.getMotion6(&vval[0], &vval[1], &vval[2], &vval[3], &vval[4], &vval[5]);

  atten(vval, 6, 10);

/*
  int newRange = 0;
  for (int i=0; i<6; i++) {
    if (vval[i] > vmax[i]) {
      vmax[i] = vval[i];
      newRange = 1;
    }
    if (vval[i] < vmin[i]) {
      vmin[i] = vval[i];
      newRange = 1;
    }
  }

  if (newRange) {
    for (int i=0; i<6; i++) {
      Serial.print(vmin[i]);
      Serial.print(":");
      Serial.print(vmax[i]);
      Serial.print("=");
      Serial.print(vmax[i]-vmin[i]);
      Serial.print("\t");
    }
    Serial.println("");
  }
*/
  int show=0;
  for (int i=0; i<3; i++) {
    if (vval[i] != vlast[i]) {
      vlast[i]=vval[i];
      show=1;
    }
  }
  if (show) {
      for (int i=0; i<6; i++) {
      SERIAL_PRINT(vlast[i]);
      SERIAL_PRINT("\t");
    }
    SERIAL_PRINTLN("");
  }

  
  delay(20);
  // these methods (and a few others) are also available
  //CurieImu.getAcceleration(&ax, &ay, &az);
  //CurieImu.getRotation(&gx, &gy, &gz);

  // display tab-separated accel/gyro x/y/z values
  //Serial.print("a/g:\t");
  //Serial.print(ax);
  //Serial.print("\t");
  //Serial.print(ax);
  //Serial.print("\t");
  //Serial.print(ax);
  //Serial.print("\t");
  //Serial.print(gx);
  //Serial.print("\t");
  //Serial.print(gy);
  //Serial.print("\t");
  //Serial.println(gz);

  // blink LED to indicate activity
  blinkState = !blinkState;
  digitalWrite(ledPin, blinkState);
#endif
}
