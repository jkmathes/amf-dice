/**********************************************
 * Module for communication with the dice
 *
 **********************************************/
var diceServiceUUID = "00000000000000000000000000001812";
var diceRollCharacteristicUUID = "00000000000000000000000000002ac5";
var diceCommandCharacteristicUUID = "00000000000000000000000000002a9f";

var r = {
  commands: {
    COMMAND_REGISTER: 1,
    COMMAND_REGISTER_ACK: 2,
    COMMAND_LED_ON: 3,
    COMMAND_LED_OFF: 4
  },
  dice: [],
  noble: null,
  maxDice: 4,
  diceMapping: {},

  sendCommand: function(did, command) {
    if(r.diceMapping[did] != null) {
      var payload = new Buffer(1);
      payload.writeUInt8(command, 0);
      r.diceMapping[did].write(payload, true, function(e) {
        if(e) {
          console.log(e);
        }
      });
    }
  },

  init: function(rollHandler) {
    /**
     * Initialize the BLE system
     */
    r.noble = require('noble');
    r.noble.on('stateChange', function(state) {
      console.log('State changed to: ' + state);
      if(state === 'poweredOn') {
        r.noble.startScanning([diceServiceUUID], false);
      } else {
        r.noble.stopScanning();
      }
    });

    r.noble.on('discover', function(p) {
      var ad = p.advertisement;
      var name = ad.localName;
      var uuid = ad.serviceUuids;

      console.log('Found peripheral: ' + name);
      r.diceMapping[uuid] = name;
      p.on('disconnect', function() {
        console.log('Disconnected from: ' + p);
        r.noble.startScanning();
      });

      p.connect(function(e) {
        p.discoverServices([diceServiceUUID], function(e, services) {
          services.forEach(function(service) {
            console.log('Found service: ' + service.uuid);
            service.discoverCharacteristics([], function(e, characteristics) {
              characteristics.forEach(function(c) {
                if(c.uuid === diceRollCharacteristicUUID) {
                  c.on('read', function(data, isNotification) {
                    var did = data[0] >> 4;
                    var value = data[0] & 0xf;
                    rollHandler(did, value);
                  });
                  c.notify(true, function(e) {
                    console.log('Listening for rolls');
                  });
                }
                else if(c.uuid = diceCommandCharacteristicUUID) {
                  c.on('read', function(data, isNotification) {
                    var did = data[0] >> 4;
                    var value = data[0] & 0xf;
                    if(value == r.commands.COMMAND_REGISTER) {
                      r.diceMapping[did] = c;
                      r.sendCommand(did, r.commands.COMMAND_REGISTER_ACK);
                      console.log('Registered dice #' + did);  
                    }
                  });
                  c.notify(true, function(e) {
                    if(e) {
                      console.log(e);
                    }
                  });
                }
              });
            });
          });
        });
      });
    });
  }
};

module.exports = r;
