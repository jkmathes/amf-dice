var dicecomm = require('./dicecomm');
var gamecomm = require('./gamecomm');
var passport = require('passport');
var BasicStrategy = require('passport-http').BasicStrategy;

/**
 * Initialize the comm system between the game and the server
 *
 * We also specify the callback here for events flowing from the game
 */
gamecomm.init(8000, function(gameEvent) {
  console.log('Game event received: ' + JSON.stringify(gameEvent));
  if(gameEvent.type && gameEvent.type === 'win') {
    var car = gameEvent.data.car;
    console.log('  Car ' + car + ' wins!');
    dicecomm.sendCommand(car, dicecomm.commands.COMMAND_WIN);
  }
});

/**
 * Initialize the comm system between the dice and the server
 *
 * We also specify the callback here for events flowing from the dice
 */
dicecomm.init(function(dice, value) {
  console.log('Found roll of [' + value + '] from dice #' + dice);
  gamecomm.roll(dice, value);
});

var app = gamecomm.app;

passport.use(new BasicStrategy(
  function(username, password, done) {
    if(username === 'amf' && password === 'amf') {
      return done(null, {name: 'amf'});
    }
    return done(null, false, {
      message: 'Invalid'
    });
  }
));

app.post('/roll', passport.authenticate('basic', { session: false }), function(req, res) {
  var dice = req.query.dice;
  var value = req.query.value;
  console.log(dice + ' - ' + value);
  gamecomm.roll(dice, value);
  res.json({status: 'ok'});
});

app.post('/command', passport.authenticate('basic', { session: false }), function(req, res) {
  var dice = req.query.dice;
  var value = req.query.value;
  console.log('Sending ' + value + ' to dice #' + dice);
  dicecomm.sendCommand(dice, value);
  res.json({status: 'ok'});
});

app.use(gamecomm.express.static('public'), passport.authenticate('basic', { session: false }));
