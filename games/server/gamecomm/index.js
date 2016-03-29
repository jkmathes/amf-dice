var r = {
  net: require('net'),
  init: function(port, callback) {
    r.server = r.net.createServer(function(socket) {
      socket.pipe(socket);
      r.socket = socket;

      r.socket.on('end', function() {
        console.log('Game disconnected');
      });

      r.socket.on('data', function(d) {
        console.log('Received ' + JSON.stringify(JSON.parse(d)));
        callback(JSON.parse(d));
      });

      r.socket.on('error', function(e) {
        console.log(e);
      });
    })
    r.server.listen(port);
  },

  roll: function(which, value) {
    var d = {
      'type': 'roll',
      'payload': {
        'dice': which,
        'value': value
      }
    };

    if(r.socket && r.socket.remoteAddress) {
      r.socket.write(JSON.stringify(d) + '\n');
    }
  }
}

module.exports = r;
