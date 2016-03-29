# Curie Dice for Austin Maker Faire

A demo for the Intel Arduino 101 where curie boards housed in dice move cars along a game track when they are rolled.

## Demo layout
<pre>
         +-----------------+
         |  Game thick     |
         |  client         |
         +--------+--------+
                  |
                  |
         +--------+--------+
         |                 |
         |  node server w/ |
         |  BLE drivers    |
   +-----+                 +-----+
   |     +--+-----------+--+     |
   |        |           |        |
   |        |           |        |
   |        |           |        |
+--+--+  +--+--+     +--+--+  +--+--+
|Dice1|  |Dice2|     |Dice3|  |Dice4|
+-----+  +-----+     +-----+  +-----+
</pre>

Each CurieDice will detect its current roll and transmit that data to the server over BLE. The server processes this data and transmits that data to the game itself - a thick client written in Java.

## Componentry
The dice component can be emulated through API into the node server. To simulate a roll of '6' from dice '1':

<pre><code>
curl -u amf:amf -X POST -H "Content-type: application/json" -d '{"dice": 1, "value": 6}' http://<serverhost>:8080/roll
</code></pre>

This can be done to test connectivity without having live BLE-connected dice.

To simulate sending a command '6' to dice '1':

<pre><code>
curl -u amf:amf -X POST -H "Content-type: application/json" -d '{"dice": 1, "value": 6}' http://<serverhost>:8080/command
</code></pre>

This can be done to test connectivity without having live BLE-connected dice.
