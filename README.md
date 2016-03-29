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
The dice component can be emulated through API into the node server. To simulate a roll:

