#include <CurieBle.h>

BLEPeripheral blePeripheral;

// 0x1812 GATT Service == Human Interface Device
// org.bluetooth.service.human_interface_device
BLEService diceService("0x1812");

// 0x2AC5 GATT Characteristic == Object Action Control Point
// org.bluetooth.characteristic.object_action_control_point
BLEUnsignedCharCharacteristic diceRollCharacteristic("0x2AC5", BLERead | BLEWrite | BLENotify);

// 0x2A9F GATT Characteristic == User Control Point
// org.bluetooth.characteristic.user_control_point
BLEUnsignedCharCharacteristic diceCommandCharacteristic("0x2A9F", BLERead | BLEWrite | BLENotify);

/**
 * Commands for the dice to emit and recv
 */
#define COMMAND_REGISTER 1
#define COMMAND_REGISTER_ACK 2
#define COMMAND_LED_ON 3
#define COMMAND_LED_OFF 4

/**
 * Dice identifier - set later via the command header
 */
int diceID;

/**
 * Whether or not this dice is yet registered with the command server
 */
boolean registered = false;

unsigned long last = millis();
boolean led = false;

void setup() {  
  /**
   * Identify diceID based on jumper
   */
  diceID = 2;
  
  blePeripheral.setLocalName("CurieDice");
  blePeripheral.setAdvertisedServiceUuid(diceService.uuid());
  blePeripheral.addAttribute(diceService);
  blePeripheral.addAttribute(diceCommandCharacteristic);
  blePeripheral.addAttribute(diceRollCharacteristic);

  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  diceCommandCharacteristic.setEventHandler(BLEWritten, commandHandler);
  
  blePeripheral.begin();
}

void loop() {
  BLECentral central = blePeripheral.central();
  registered = false;
  if(central) {
    while(central.connected()) {
      if(!registered) {
        registerDice();
      }
      
      blePeripheral.poll();

      /**
       * This simulates a roll every 5 seconds
       */
      unsigned long now = millis();
      if(now - last > (5 * 1000)) {
        last = now;
        roll(random(1, 7));
      }


      if(led) {
        digitalWrite(13, HIGH);
      }
      else {
        digitalWrite(13, LOW);
      }
    }
  }
}

void roll(int value) {
  diceRollCharacteristic.setValue((diceID << 4) | value);
}

void registerDice() {
  diceCommandCharacteristic.setValue((diceID << 4) | COMMAND_REGISTER);
}

/**
 * Handle commands coming from the server
 */
void commandHandler(BLECentral &central, BLECharacteristic &characteristic) {
  int cvalue = diceCommandCharacteristic.value();

  if(cvalue == COMMAND_LED_ON) {
    led = true;
  }
  else if(cvalue == COMMAND_LED_OFF) {
    led = false;
  }
  else if(cvalue == COMMAND_REGISTER_ACK) {
    registered = true;
  }
}

/**
 * On connect
 */
void blePeripheralConnectHandler(BLECentral &central) {
}

/**
 * On disconnect
 */
void blePeripheralDisconnectHandler(BLECentral &central) {
}

