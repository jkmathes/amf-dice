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

int diceID = 3;
unsigned long last = millis();

void setup() {
  delay(5000);
  pinMode(13, OUTPUT);

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
  if(central) {
    while(central.connected()) {
      blePeripheral.poll();
      unsigned long now = millis();
      if(now - last > (5 * 1000)) {
        last = now;
        roll(random(1, 7));
      }    
    }
  }
}

void roll(int value) {
  diceRollCharacteristic.setValue((diceID << 4) | value);
}

void commandHandler(BLECentral &central, BLECharacteristic &characteristic) {  
}

void blePeripheralConnectHandler(BLECentral &central) {
}

void blePeripheralDisconnectHandler(BLECentral &central) {
}

