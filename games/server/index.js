var dicecomm = require('./dicecomm');
var gamecomm = require('./gamecomm');
var noble = require('noble');
var async = require('async');

gamecomm.init(8000);
dicecomm.init(function(dice, value) {
  console.log('Found roll of [' + value + '] from dice #' + dice);
  gamecomm.roll(dice, value);
});
