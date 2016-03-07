/**********************************************
 * Module for communication with the dice
 *
 **********************************************/
var diceServiceUUID = "00000000000000000000000000001812";
var diceRollCharacteristicUUID = "00000000000000000000000000002ac5";

var r = {
  dice: [],
  noble: null,
  maxDice: 4,
  diceMapping: {},

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
              });
            });
          });
        });
      });
    });
  }
};

module.exports = r;
