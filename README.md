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

* The Sketch uses a Bluetooth module **HC-05**.

* The pairing code in this kind of modules usually is **1234**.

## Current status

| STATUS  | DETAILS |
| :------------: |:--------------- |
| DONE | Connecting the Android App to the board. |
| DONE | Controlling a propeller from the Android App. |
| DONE | Sending Serial data using Bluetooth from the App. |
| DONE | Receiving Serial data using Bluetooth in the board. |
| DONE | Executing the remote order to start moving a propeller. |
| DONE | Designing a 3D case for the drone. |
| PENDING | Designing a more lightweight case. |
| PENDING | Implementing a more lightweight board. |
| PENDING | Implementing a code to handle the four propellers. |
| PENDING | Implementing a VGA Serial camera. |
| PENDING | Receiving a VGA snapshot from the drone. |
