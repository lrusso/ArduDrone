# ArduDrone

Work in progress of a drone designed in Arduino and using an Android App.

![alt screenshot](https://raw.githubusercontent.com/lrusso/ArduDrone/main/ArduDrone.png)

## About the case

* The STL model is included to print it in any 3D printer.

* The SCENE file can be edited with https://lrusso.github.io/3DObjectMaker/3DObjectMaker.htm.

## About the App

* The App connects to the Arduino board using a Bluetooth connection.

* The App sends and receives Strings to the board in order to handle the events.

## About the Sketch

* The Sketch uses a Bluetooth module HC-05.

* The pairing code in this kind of modules usually is 1234.

## Current status

| STATUS  | DETAILS |
| :------------: |:--------------- |
| DONE | The Android App connects to the board. |
| DONE | The Android App controls a propeller. |
| DONE | Designing a 3D case for the drone. |
| DONE | The Arduino board can receive data from the App. |
| DONE | The Arduino board can receive the remote order to move a propeller. |
| PENDING | Designing a more lightweight case. |
| PENDING | Implementing a more lightweight board. |
| PENDING | Implementing a code to handle the four propellers. |
| PENDING | Implementing a VGA Serial camera. |
| PENDING | The Android App receiving a VGA snapshot from the drone. |
