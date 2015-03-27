/*
  Author: Kevin Clerix
  Used technologies: Bluetooth, Ping))) sensor, servo motors  
*/

#include <Servo.h>  // Include servo library

Servo servoLeft;  // Declare left servo signal
Servo servoRight; // Declare right servo signal
String sign = ""; // variable to receive data from the serial port
const int pingPin = 11;
const int dangerThresh = 15; //threshold for obstacles (in cm)

int leftDistance, rightDistance, frontDistance=30; //distances on either side
Servo panMotor; //ping his servo motor  
long duration; //time it takes to recieve PING))) signal

void setup()                                 // Built in initialization block
{ 
  
  Serial.begin(9600);                        // Set data rate to 9600 bps
  servoLeft.attach(13);                      // Attach left signal to P13 
  servoRight.attach(12);

  panMotor.attach(10); //attach motors to proper pins
  panMotor.write(90); //set PING))) pan to center
}  
 
void loop()   // Main loop auto-repeats
{ 
  
  sign = Serial.readString();
  
  //sends out a ping to determine the distance
  int distanceFwd = ping();
  
  //if the distance between the object and the "Wall-E"
  //is still big enough then he will move forward
  if (distanceFwd>dangerThresh) 
  {
    servoLeft.writeMicroseconds(1700);
    servoRight.writeMicroseconds(1300);
   
    
    panMotor.write(60); 
    delay(200);
    rightDistance = ping(); //scan to the right
    //If the rightdistance is less than 20 
    //then he will turn left 
    if(rightDistance<20){
      servoLeft.writeMicroseconds(1460);
      servoRight.writeMicroseconds(1460); //turn left
      delay(200);
    }
    
    panMotor.write(120);
    delay(300);
    leftDistance = ping(); //scan to the left
    //if the leftdistance if less then 20
    //then he will turn right
    if(leftDistance<20){
      servoLeft.writeMicroseconds(1540);
      servoRight.writeMicroseconds(1540); //turn left
      delay(200);
     
    }
    
    panMotor.write(90); //return to center
    delay(100);
    }
    
  
  else //if path is blocked
  {
    servoLeft.writeMicroseconds(1500);
    servoRight.writeMicroseconds(1500); 
    panMotor.write(0); 
    delay(500);
    rightDistance = ping(); //scan to the right
    delay(500);
    panMotor.write(180);
    delay(700);
    leftDistance = ping(); //scan to the left
    delay(500);
    panMotor.write(90); //return to center
    delay(100);
    compareDistance();
  }
  
  //HERE STARTS THE SECTION THAT YOU ONLY NEED 
  //WHEN USING BLUETOOTH CONNECTION
  
  if(sign.equals("z")){
   servoLeft.writeMicroseconds(1700);
   servoRight.writeMicroseconds(1300);
  }
  
  if(sign.equals("q")){
   servoLeft.writeMicroseconds(1450);
   servoRight.writeMicroseconds(1450);
  }
  
  if(sign.equals("s")){
   servoLeft.writeMicroseconds(1300);
   servoRight.writeMicroseconds(1700);
  }
  
  if(sign.equals("d")){
   servoLeft.writeMicroseconds(1550);
   servoRight.writeMicroseconds(1550);
  }
  
  if(sign.equals("a")){
   servoLeft.writeMicroseconds(1500);
   servoRight.writeMicroseconds(1500);
  }
  
  //HERE THE SECTION FOR BLUETOOTH STOPS
  
}

//This function will compare the distance between Right and Left
//then he will see which side is best to ride to.

void compareDistance()
{
  if (leftDistance>rightDistance) //if left is less obstructed 
  {
    servoLeft.writeMicroseconds(1460);
    servoRight.writeMicroseconds(1460); //turn left
    delay(200);  
  
  }
  else if (rightDistance>leftDistance) //if right is less obstructed
  {
    servoLeft.write(180);
    servoRight.write(180); //turn right
    delay(200);
    
  }
   else //if they are equally obstructed
  {
    servoLeft.writeMicroseconds(1300);
    servoRight.writeMicroseconds(1300); //turn 180 degrees
    delay(200);
  }
}

//This function will send out a ultrasonic signal 
//when it collides with an object it will return
//the ping))) sensor will catch it and after that
//this function will determine the distance between
//himself and the object.
long ping()
{
  // Send out PING))) signal pulse
  pinMode(pingPin, OUTPUT);
  digitalWrite(pingPin, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin, LOW);
  
  //Get duration it takes to receive echo
  pinMode(pingPin, INPUT);
  duration = pulseIn(pingPin, HIGH);
  
  //Convert duration into distance
  return duration / 29 / 2;
}
 
