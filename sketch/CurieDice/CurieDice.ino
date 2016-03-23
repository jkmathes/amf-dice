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
#include <CurieBLE.h>
#include "CurieIMU.h"
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

// ****************************************************************
// ************** Constants ***************************************
// ****************************************************************
signed char orientable[][4] = {
  // x, y, z, rollVal
  { 10,   0,  10, 1},
  { 10,   0, -13, 2},
  { -2, -17,   0, 3},
  { -2,  15,  -1, 4},
  {-14,  -1,  8, 5},
  {-15,  -2, -11, 6}
};
int orientDisp[] = { 0xaa, 0x1, 0x3, 0x7, 0x17, 0x37, 0x77 };

// ************* I/O Pin assignments **********************
#define PIXEL_PIN       6     // Where the NeoPixel DIN pin is connected
#define VIBE_PIN        7     // Where vibrate motot is connected
#define DICE_ID0       10     // Used to jumper DiceId
#define DICE_ID1       11     // Used to jumper DiceId
#define DICE_ID2       12     // Used to jumper DiceId
#define ONBOARD_LED    13     // activity LED pin

// **************** Other Constants **********************************
#define NUMPIXELS       8     // Number of LEDS on NeoPixel

#define COMMAND_REGISTER      1   // dice <--> server cmds
#define COMMAND_REGISTER_ACK  2
#define COMMAND_BUZZ          5
// *******************************************************************
// ************** Globals ********************************************
// *******************************************************************
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIXEL_PIN, NEO_GRB + NEO_KHZ800);

BLEPeripheral blePeripheral;        // BLE Peripheral Device (the board you're programming)
BLEService diceService("0x1812");   // BLE Service
BLEUnsignedCharCharacteristic diceRollCharacteristic("0x2AC5", BLERead|BLEWrite|BLENotify);
BLEUnsignedCharCharacteristic diceCommandCharacteristic("0x2A9F", BLERead|BLEWrite|BLENotify);


unsigned int  onboardLED_interval = 1000;
bool          onboardLED_state = 0;
unsigned long onboardLED_t0 = millis();
unsigned char diceId = 1;

boolean bleConnect = false;         // ble link level connection
bool dice_registered = false;       // dice <--> server state
// ****************************************************************
// ************** Functions ***************************************
// ****************************************************************
void setupOnboardLED() { pinMode(ONBOARD_LED, OUTPUT); }
void updateOnboardLED() {
  if ((millis()-onboardLED_t0) > onboardLED_interval) {
    onboardLED_state = !(onboardLED_state);
    digitalWrite(ONBOARD_LED, onboardLED_state);
    onboardLED_t0 = millis();
  }
}

void setupDiceId() { pinMode(DICE_ID2, INPUT_PULLUP); pinMode(DICE_ID1, INPUT_PULLUP);  pinMode(DICE_ID0, INPUT_PULLUP);}
unsigned char readDiceId() {
  int a = (digitalRead(DICE_ID2)<<2) | (digitalRead(DICE_ID1)<<1) |digitalRead(DICE_ID0);
  switch (a) {
    case 7: diceId = 0; break;            // no jumpers
    case 3: diceId = 1; break;            // jumper GND -> PIN12
    case 5: diceId = 2; break;            // jumper GND -> PIN11
    case 6: default: diceId = 3; break;   // jumper GND -> PIN10
  }
  return diceId;
}

// ************* NeoPixel ********************************************
class Pixels {
  private:
    int buildColor(int color) {
      int r = (color>>8)&0xf;
      int g = (color>>4)&0xf;
      int b = color&0xf;
      return (r<<16) | (g<<8) | (b);
    }
  public:
    Pixels() { }
    void setup() {
      pixels.begin();
    }
    void set(int p, int color, int dlay) {
      int c = buildColor(color);
      pixels.setPixelColor(p,c);
      pixels.show();
      delay(dlay);
    }
    void disp(int mask, int color, int dlay) {
      int r = (color>>8)&0xf;
      int g = (color>>4)&0xf;
      int b = color&0xf;
      int c = (r<<16) | (g<<8) | (b);  
      for (int i=0; i<NUMPIXELS; i++)  {
        pixels.setPixelColor(i,(mask&(1<<i))?c:0);
      }
      pixels.show();
      delay(dlay);
    }
} pix;

void idleShow()
{
  static int imask = 0x1;
  static int dir = 1;
  static unsigned long prev = 0;
  unsigned long p = (millis()>>7) % 16;
  if (p!=prev) {
    prev = p;
    if (imask==0x01) dir = 1;
    if (imask==0x80) dir = 0;
    if (dir) imask = imask << 1;
    else imask = imask >> 1;
    pix.disp(imask,0x00f,0);
  }
}
// ************* Vibrate ********************************************
void setupVibe() { pinMode(VIBE_PIN, OUTPUT); digitalWrite(VIBE_PIN,0);}
void vibrate(int dur) {
    digitalWrite(VIBE_PIN,1); delay(dur);
    digitalWrite(VIBE_PIN,0); delay(dur);
    digitalWrite(VIBE_PIN,1); delay(dur);
    digitalWrite(VIBE_PIN,0);
}

// ************* BLE **************************************************
void bleSetup() {
  blePeripheral.setLocalName("CurieDice");
  blePeripheral.setAdvertisedServiceUuid(diceService.uuid());
  blePeripheral.addAttribute(diceService);
  blePeripheral.addAttribute(diceCommandCharacteristic);
  blePeripheral.addAttribute(diceRollCharacteristic);
  blePeripheral.setEventHandler(BLEConnected, connectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, disconnectHandler);
  diceCommandCharacteristic.setEventHandler(BLEWritten, commandHandler);
  blePeripheral.begin();
}
void commandHandler(BLECentral &central, BLECharacteristic &characteristic) {
  int cvalue = diceCommandCharacteristic.value();
  switch(cvalue) {
    case COMMAND_REGISTER_ACK: dice_registered = true; break;
    case COMMAND_BUZZ:         vibrate(50);       break;
  }
}
void registerDice() {
  diceCommandCharacteristic.setValue((diceId << 4) | COMMAND_REGISTER);
}
void connectHandler(BLECentral& central) {
  bleConnect = true;
  SERIAL_PRINT("Connected to central: ");
  // print the central's MAC address:
  SERIAL_PRINTLN(central.address());
  // turn on the LED to indicate the connection:
  digitalWrite(ONBOARD_LED, HIGH);  
}
void disconnectHandler(BLECentral& central) {
  bleConnect = false;
  SERIAL_PRINT("Disconnected from central: ");
  digitalWrite(ONBOARD_LED, LOW);
}
void sendDiceRoll(int roll) {
  diceRollCharacteristic.setValue((diceId << 4) | roll);  
}





// ************* IMU **************************************************
int vcurr[6];      // raw readings from IMU [0:5]=AxAyAzGxGyGz
int vprev[6];     // previous reading
int movement=0;
#define TSLOT_SHIFT    5     // 32ms chunks
int noMotionCount=0;
int loMotionCount=0;
int hiMotionCount=0;
#define TSLOTS 20           // storage for chunks
int loCounts[TSLOTS]={};
int hiCounts[TSLOTS]={};
int tIndex=0;
int noMoSlots=0;    // number of consecutive noMotion slots
int dispPause=0;
#define ORIENTATION_TOL   9

void resultShow(int ori) {
  pix.disp(orientDisp[ori], (ori)?0x0f0:0xf00, 0);
}

int calcOrientation()
{
  int x = vcurr[0]>>8;
  int y = vcurr[1]>>8;
  int z = vcurr[2]>>8;
  SERIAL_PRINT(x);
  SERIAL_PRINT("\t");
  SERIAL_PRINT(y);
  SERIAL_PRINT("\t");
  SERIAL_PRINTLN(z);
  for (int i=0; i<6; i++) {
    signed char *t = orientable[i];
    if (abs(x-t[0])<ORIENTATION_TOL && abs(y-t[1])<ORIENTATION_TOL && abs(z-t[2])<ORIENTATION_TOL) return t[3];
  }
  return 0;
}

void diceCal()
{
  // Calibration procedure:
  //  Die display will request user to orient die with requested face up.
  //  User will have 6 seconds to do this for each face.  The display will go from RED -> YELLOW -> GREEN.
  //  After the 6 seconds, the orientation is read and assigned to that face.
  //  Tip: Notice the sequence being requested below in face[].
  //       It is easiest to power on dice with logo & display facing you, upright so top value is 5 which is the first requested face.
  //       Then, rotate die counter clockwise with logo continue to face you.  That would be 4, 2, 3.
  //       Then, logo up is 1.
  //       Then, logo down is 6.
    
  int face[] = {5, 4, 2, 3, 1, 6};
  for (int i=0; i<6; i++) {
    int f = face[i];
    pix.disp(orientDisp[f], 0xf00, 3000);
    pix.disp(orientDisp[f], 0xf60, 2000);
    CurieIMU.readAccelerometer(vcurr[0], vcurr[1], vcurr[2]);
    pix.disp(orientDisp[f], 0x0f0, 1000);
    signed char *t = orientable[i];
    t[0] = (signed char)(vcurr[0]>>8);
    t[1] = (signed char)(vcurr[1]>>8);
    t[2] = (signed char)(vcurr[2]>>8);
    t[3] = (signed char) f;
  }
}

int16_t imuRead() {
  static unsigned long currTimeQuant = 0;
  for (int i=0; i<6; i++) vprev[i] = vcurr[i];
  //CurieIMU.readMotionSensor(vcurr[0], vcurr[1], vcurr[2], vcurr[3], vcurr[4], vcurr[5]);
  CurieIMU.readAccelerometer(vcurr[0], vcurr[1], vcurr[2]);
  
  movement = 0;
  for (int i=0; i<3; i++) {
    //int m = (abs(vprev[i]-vcurr[i])) >> 9;
    int m = abs(vprev[i]-vcurr[i]) >> 7;
    if (m>movement) movement = m;
  }

  unsigned long tq = millis()>>TSLOT_SHIFT;
  if (tq == currTimeQuant) {
    if (movement >= 32) hiMotionCount++;
    else if (movement >= 8) loMotionCount++;
    else
      noMotionCount++;
  }
  else {
    // end of a time slot
    loCounts[tIndex]=loMotionCount;
    hiCounts[tIndex]=hiMotionCount;
    if (dispPause) {
      dispPause--;
    }
    else {
      int pxi = tIndex;
      for (int pxc=7; pxc>=0; pxc--) {
        int color = 0x0;
        if (hiCounts[pxi]) color=0x00f;
        else if (loCounts[pxi]) color=0x0f0;
        pix.set(pxc, color, 0);
        if (pxi > 0)  pxi--;
        else          pxi = TSLOTS-1;
      }
    }
    tIndex = (tIndex==(TSLOTS-1))?0:tIndex+1;
    if ((loMotionCount+hiMotionCount)==0) {
      // this slot had no motion
      noMoSlots++;
      if (noMoSlots == 5) {
        // eval for possible throw
        int i = tIndex - (5+1);
        if (i<0) i+= TSLOTS;
        int his=0;
        int lows=0;
        pix.disp(0xff, 0x000, 0);
        for (int n=0; n<15; n++) {  // eval last n slots
          SERIAL_PRINT(i);
          SERIAL_PRINT("hist: ");

          SERIAL_PRINT(loCounts[i]);
          SERIAL_PRINT("\t");
          SERIAL_PRINTLN(hiCounts[i]);
          if (hiCounts[i]>0) {
            his++;
            //pix.set(n,0xf00,250);
          }
          else if (loCounts[i]>0) {
            lows++;
            //pix.set(n,0xaa0,250);
          }
          i = (i==0)? TSLOTS-1:i-1;
        }
        if ((his+lows)>6 && his>0) {
          int roll = calcOrientation();
          SERIAL_PRINTLN(roll);
          resultShow(roll);
          sendDiceRoll(roll);
          dispPause = 50;
        }
      }
    } else {
      // this slot had motion
      noMoSlots = 0;
    }

    // reset for next tslot
    noMotionCount=0;
    loMotionCount=0;
    hiMotionCount=0;
    currTimeQuant = millis()>>TSLOT_SHIFT;
  }
}


// **********************************************************************
// ************* setup **************************************************
// **********************************************************************
void setup() {
  setupOnboardLED();
  setupDiceId();

  
  pix.setup();
  setupVibe();

  pix.disp(0xff, 0x0, 0);        // all off
  pix.disp(0xff, 0xfff, 1000);   // all white 1-sec
  pix.disp(0xff, 0xff0, 500);
  pix.disp(0xff, 0x0f0, 500);
  pix.disp(0xff, 0x000, 0);
  
  SERIAL_SETUP(9600);
  pix.set(0, 0xff0, 0);      // 0 - yellow BLE setup
  bleSetup();
  SERIAL_PRINTLN("Bluetooth device active, waiting for connections...");
  pix.set(1, 0x0f0, 500);      // 0 - green BLE setup done


  readDiceId();
  
  // *************** IMU Setup ***************************************
  // initialize device
  SERIAL_PRINTLN("Initializing IMU device...");
  int rc = CurieIMU.begin();
  
  pix.set(2, 0xff0, 500);      // 1 - yellow IMU setup
  
  // verify connection
  SERIAL_PRINTLN("Testing device connections...");
  if (CurieIMU.testConnection()) {
    SERIAL_PRINTLN("CurieImu connection successful");
  } else {
    SERIAL_PRINTLN("CurieImu connection failed");
    pix.set(7,0xf00,0);      // 8 - red - err connecting to IMU
  }

  CurieIMU.setDetectionThreshold(CURIE_IMU_SHOCK, 1500);
  CurieIMU.setDetectionDuration(CURIE_IMU_SHOCK, 50);

  diceCal();

  pix.set(4, 0x0f0, 0);      // 1 - green IMU setup done

}



void loop() {
  BLECentral central = blePeripheral.central();
  if (central && central.connected() && !dice_registered) registerDice();

  blePeripheral.poll();
  imuRead();
  updateOnboardLED();

}

