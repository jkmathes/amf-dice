var r = {
  pendingRolls: [],
  roll: function(which, value) {
    r.pendingRolls.push({
      'dice': which,
      'value': value
    });
    if(r.pendingResponse) {
      var dr = r.pendingRolls.shift();
      r.pendingResponse.response.json({'type': 'roll', 'payload': dr});
      r.pendingResponse = null;
    }
  },
  unloadRoll: function() {
    if(r.pendingResponse) {
      var exp = new Date().getTime() - 5000;
      if(r.pendingResponse.timestamp < exp) {
        if(r.pendingRolls.length > 0) {
          var dr = r.pendingRolls.shift();
          r.pendingResponse.response.json({'type': 'roll', 'payload': dr});
        }
        else {
          r.pendingResponse.response.json({'type': 'nop'});
        }
        r.pendingResponse = null;
      }
    }
  },
  init: function() {
    r.express = require('express');
    r.app = r.express();
    r.pendingResponse = null;
    r.app.get('/', function(req, res) {
      if(r.pendingRolls.length > 0) {
        var dr = r.pendingRolls.shift();
        res.json({'type': 'roll', 'payload': dr});
      } else {
        r.pendingResponse = {
          response: res,
          timestamp: new Date().getTime()
        };
      }
    });

    setInterval(function() {
      r.unloadRoll();
    }, 1000);

    r.app.listen(3000, function() {
      console.log('Listening for dice comms on port 3000');
    })
  }
};

module.exports = r;
