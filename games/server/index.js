var dicecomm = require('./dicecomm');

dicecomm.init();

// Roll a dice every 3 seconds
setInterval(function() {
  dicecomm.roll(randomInt(0, 3), randomInt(1, 6));
}, 3000);

function randomInt(low, high) {
  return Math.floor(Math.random() * (high - low + 1) + low);
}
