#include <SoftwareSerial.h>

SoftwareSerial bluetooth(10,7);

int     MOTOR_1_PIN = 3;
int     MOTOR_2_PIN = 11;

void setup()
    {
    bluetooth.begin(9600);

    analogWrite(MOTOR_1_PIN, 255);
    analogWrite(MOTOR_2_PIN, 0);
    }

void loop()
    {
    int incomingByte = 0;
    String content = "";
    char character;

    while(bluetooth.available())
        {
        character = bluetooth.read();
        if (String(character)=="W")
          {
          analogWrite(MOTOR_2_PIN, 255);
          delay(50);
          analogWrite(MOTOR_2_PIN, 0);
          }
        content.concat(character);
        }

    if (content!="")
        {
        bluetooth.print(content);
        }
    delay(100);
    }
