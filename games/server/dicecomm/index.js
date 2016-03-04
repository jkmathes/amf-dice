/**********************************************
 * Module for communication with the dice, as
 * well as the various games
 *
 **********************************************/
var diceServiceUUID = "00000000000000000000000000001812";
var diceRollCharacteristicUUID = "00000000000000000000000000002ac5";

var r = {
  discoveredDice: {},
  noble: null,
  /**
   * Any rolls which have been received from the dice,
   * but not yet retrieved by the games
   */
  pendingRolls: [],

  /**
   * Send a single roll to the game. This will buffer
   * the roll so that the long poll can either return
   * immediately, or wait for the next poll
   */
  roll: function(which, value) {
    r.pendingRolls.push({'dice': which, 'value': value});
    if(r.pendingResponse) {
      var dr = r.pendingRolls.shift();
      r.respondRoll(r.pendingResponse.response, dr);
      r.pendingResponse = null;
    }
  },

  /**
   * Utility to send a roll payload to the game
   */
  respondRoll: function(response, payload) {
    response.json({'type': 'roll', 'payload': payload});
  },

  /**
   * Utility to send a NOP to the game. This occurs if
   * the game's long poll times out
   */
  respondNOP: function(response) {
    response.json({'type': 'nop'});
  },

  /**
   * If there is a queued roll to send, dequeue and return it.
   * Otherwise, if the poll has expired, return a NOP
   */
  unloadRoll: function() {
    if(r.pendingResponse) {
      /**
       * Set the long poll to 15 seconds
       */
      var exp = new Date().getTime() - 15000;
      if(r.pendingResponse.timestamp < exp) {
        if(r.pendingRolls.length > 0) {
          var dr = r.pendingRolls.shift();
          r.respondRoll(r.pendingResponse.response, dr);
        }
        else {
          r.respondNOP(r.pendingResponse.response);
        }
        r.pendingResponse = null;
      }
    }
  },

  /**
   * Initialize the dice communication system.
   * This listens as an HTTP server
   */
  init: function() {
    r.express = require('express');
    r.app = r.express();
    r.pendingResponse = null;
    r.app.get('/', function(req, res) {
      if(r.pendingRolls.length > 0) {
        var dr = r.pendingRolls.shift();
        r.respondRoll(res, dr);
      } else {
        r.pendingResponse = {response: res, timestamp: new Date().getTime()
        };
      }
    });

    /**
     * Check every second if we have timed out. If a roll comes in,
     * the long poll will terminate immediately, but this interval check is
     * purely an expiration validation
     */
    setInterval(function() {
      r.unloadRoll();
    }, 1000);

    r.app.listen(8000, function() {
      console.log('Listening for dice comms on port 8000');
    });

    /**
     * Initialize the BLE system
     */
    r.noble = require('noble');
    r.noble.on('stateChange', function(state) {
      console.log('State changed to: ' + state);
      if(state === 'poweredOn') {
        r.noble.startScanning();
      }
      else {
        r.noble.stopScanning();
      }
    });

    r.noble.on('discover', function(p) {
      var ad = p.advertisement;
      var name = ad.localName;
      var uuid = ad.serviceUuids;

      console.log('Found peripheral: ' + name);
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
                    console.log('Roll of ' + value + ' from dice #' + did);
                    r.roll(did, value);
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
