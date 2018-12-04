#pragma config(Sensor, S2,     xTouch,         sensorTouch)
#pragma config(Sensor, S1,     yTouch,         sensorTouch)
#pragma config(Motor,  motorB,          xMotor,        tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorC,          yMotor,        tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorA,          clawMotor,     tmotorNormal, PIDControl, encoder)

const float SQUARE_DIST = 5.7;
const float CLAW_CONSTANT = 340;
const float ENC_CONST = 40.0 / 360.0 * 3.2 / 5.0 / 10.0;
const float OFFSETY = 8.9;
const float HALF_SQUARE = SQUARE_DIST/2/ENC_CONST + .5;
const int SPEED = 70;
const int ZERO_SPEED = SPEED*0.9;
const int MESSAGE_MAX = 20;
const int TURN_TIME = 1000*60;
typedef struct
{
  int length;
  ubyte array [20];
}tByteArr;

int dataChk (tByteArr &arr){
  int length = -1;
  //Check for start byte
  if(arr.array[0] != 0x02){
    return length;
  }
  //Look for end byte
  for(int i = 0; i < 20; i++){
    if(arr.array[i] == 0x04){
      return i + 3;
    }
  }
  return length;
}

void waitForConfirm ()
{
  while(nNxtButtonPressed != 3){}
  while(nNxtButtonPressed != -1){}
}

//Zero the motors using the touch sensors
void resetMotors(){
  motor[xMotor] = -ZERO_SPEED;
  motor[yMotor] = -ZERO_SPEED;
  while(!SensorValue(xTouch) || !SensorValue(yTouch)){
    if(SensorValue(xTouch)){
      motor[xMotor] = 0;
    }
    if(SensorValue(yTouch)){
      motor[yMotor] = 0;
    }
  }
  motor[xMotor] = 0;
  motor[yMotor] = 0;
  wait1Msec(500);
  nMotorEncoder[xMotor] = 0;
  nMotorEncoder[yMotor] = 0;
}

//Move the claw to a given position without interfering with other pieces
void moveToPos (int xCoord, int yCoord)
{
  //Calculate offsetty for moving between squares
  float halfX = 0, halfY = 0;
  if (nMotorEncoder[xMotor]*ENC_CONST/SQUARE_DIST > 1)
    halfX = -HALF_SQUARE;
  else
    halfX = HALF_SQUARE;

  //Offset if necessary
  if (!SensorValue (yTouch)){
    halfY = -HALF_SQUARE;
    float yOff = nMotorEncoder [yMotor] + halfY;
    motor[yMotor] = -SPEED;
    while (nMotorEncoder[yMotor] > yOff){}
    motor[yMotor] = 0;
  }
  wait1Msec(50);

  float xOff = nMotorEncoder [xMotor] + halfX;
  if (xOff > nMotorEncoder[xMotor]){
    motor[xMotor] = SPEED;
    while (nMotorEncoder[xMotor] < xOff){}
    } else {
    motor[xMotor] = -SPEED;
    while (nMotorEncoder[xMotor] > xOff){}
  }
  motor[xMotor] = 0;
  wait1Msec(50);

  //Calculate necessary travel targets
  float targetX = (xCoord * SQUARE_DIST) / ENC_CONST;
  if (xCoord > 0)
    targetX -= HALF_SQUARE;
  else
    targetX += HALF_SQUARE;
  float targetY = (yCoord * SQUARE_DIST + OFFSETY) / ENC_CONST + halfY;

  //Move to specified X
  if (nMotorEncoder[xMotor] < targetX){
    motor[xMotor] = SPEED;
    while (nMotorEncoder[xMotor] < targetX){}
  }
  else {
    motor[xMotor] = -SPEED;
    while (nMotorEncoder[xMotor] > targetX){}
  }
  motor[xMotor] = 0;
  wait1Msec(50);

  //Move to specified Y
  if (nMotorEncoder[yMotor] < targetY){
    motor[yMotor] = SPEED;
    while (nMotorEncoder[yMotor] < targetY){}
  }
  else {
    motor[yMotor] = -SPEED;
    while (nMotorEncoder[yMotor] > targetY){}
  }
  motor[yMotor] = 0;
  wait1Msec(50);

  //Undo offset
  if (halfY != 0)
  {
    float yOff = (yCoord * SQUARE_DIST + OFFSETY) / ENC_CONST;
    motor[yMotor] = SPEED;
    while (nMotorEncoder[yMotor] < yOff){}
    motor[yMotor] = 0;
  }
  wait1Msec(50);

  xOff = (xCoord * SQUARE_DIST) / ENC_CONST;
  if (xOff > nMotorEncoder[xMotor]){
    motor[xMotor] = SPEED;
    while (nMotorEncoder[xMotor] < xOff){}
    } else {
    motor[xMotor] = -SPEED;
    while (nMotorEncoder[xMotor] > xOff){}
  }
  motor[xMotor] = 0;
  wait1Msec(50);

  wait1Msec(1000);
}

void closeClaw(){
  nMotorEncoder[clawMotor] = 0;
  motor[clawMotor] = 20;
  while(nMotorEncoder[clawMotor] < CLAW_CONSTANT){}
  motor[clawMotor] = 0;
}

void openClaw(){
  nMotorEncoder[clawMotor] = 0;
  motor[clawMotor] = -20;
  while(nMotorEncoder[clawMotor] > -CLAW_CONSTANT){}
  motor[clawMotor] = 0;
}

void removePiece (int xCoord, int yCoord){
  xCoord = 7-xCoord;
  moveToPos(xCoord, yCoord);
  wait1Msec(50);
  closeClaw();
  wait1Msec(50);
  moveToPos(xCoord, -1);
  wait1Msec(50);
  openClaw();
  wait1Msec(50);
}

//Move a piece from one coordinate to another
void movePiece (int xFrom, int yFrom, int xTo, int yTo){
  xFrom = 7-xFrom;
  xTo = 7-xTo;
  moveToPos(xFrom, yFrom);
  wait1Msec(50);
  closeClaw();
  wait1Msec(50);
  moveToPos(xTo, yTo);
  wait1Msec(50);
  openClaw();
  wait1Msec(50);
}

bool waitTurnTimer ()
{
  time1[T1] = 0;
  while(nNxtButtonPressed != 3 && time1[T1] < TURN_TIME){}
  while(nNxtButtonPressed != -1 && time1[T1] < TURN_TIME){}
  return time1[T1] < TURN_TIME;
}

//Calibration process to set board alignment
void calibrate()
{
  int calibratePos [16][2] = {
    {0,0},{0,7},{7,7},{7,0},
    {1,1},{1,6},{6,6},{6,1},
    {2,2},{2,5},{5,5},{5,2},
    {3,3},{3,4},{4,4},{4,3}};
  for (int pos = 0; pos <16; pos++)
  {
    resetMotors();
    moveToPos(calibratePos[pos][0],calibratePos[pos][1]);
    waitForConfirm();
  }
}

void commandToAction(tByteArr &charCommand)
{
  int index = 0;
  bool isWhite = false;

  //Skip error bits
  for (int errBit = 0; errBit < charCommand.length; errBit ++)
  {
    if (charCommand.array[errBit] != 0x02)
    {
      index = errBit;
      break;
    }
  }

  //Check white or black
  if(charCommand.array[index] == 'W')
    isWhite = true;
  index ++;

  //Continue to end of command
  while(charCommand.array[index] != 0x00 && charCommand.array[index] != 0x04 && index < 20)
  {
	//Move Command
    if(charCommand.array[index] == 'M')
    {
      resetMotors();
      int xFrom = 0, yFrom = 0, xTo = 0, yTo = 0;
      if (!isWhite)
      {
        xFrom = 7-(charCommand.array[index + 1] - '0');
        yFrom = 7-(charCommand.array[index + 2] - '0');
        xTo = 7-(charCommand.array[index + 3] - '0');
        yTo = 7-(charCommand.array[index + 4] - '0');
      }
      else
      {
        xFrom = charCommand.array[index + 1] - '0';
        yFrom = charCommand.array[index + 2] - '0';
        xTo = charCommand.array[index + 3] - '0';
        yTo = charCommand.array[index + 4] - '0';
      }
      movePiece(xFrom, yFrom, xTo, yTo);
      index += 5;
    }
    else if(charCommand.array[index] == 'R') // Remove Command
    {
      resetMotors();
      int xRemove = 0, yRemove = 0;
      if (!isWhite)
      {
        xRemove = 7-(charCommand.array[index + 1] - '0');
        yRemove = 7-(charCommand.array[index + 2]- '0');
      }
      else
      {
        xRemove = charCommand.array[index + 1] - '0';
        yRemove = charCommand.array[index + 2] - '0';
      }
      removePiece(xRemove, yRemove);
      index += 3;
    }
    else if(charCommand.array[index] == 'P') // Promotion Commnd
    {
      string piece = "";
      if(charCommand.array[index+1] == 'q')
      {
        piece = "queen";
      }
      else if(charCommand.array[index+1] == 'b')
      {
        piece = "bishop";
      }
      else if(charCommand.array[index+1] == 'n')
      {
        piece = "knight";
      }
      else if(charCommand.array[index+1] == 'r')
      {
        piece = "rook";
      }
      nxtDisplayTextLine(3, "Promote to %s.", piece);
      waitForConfirm();
      index += 2;
    }
    else
    {
      nxtDisplayTextLine(3, "Error: %c", charCommand.array[index]);
    }
  }
            resetMotors();
}


task main (){
  nMotorEncoder[clawMotor] = 0;
  int menuSelect = 0, prevSelect = 0;
  int forLoopCount = 0;
  string picText[3] = {"CHEEBO_PLAY.ric", "CHEEBO_SETTINGS.ric", "CHEEBO_EXIT.ric"};
  ubyte receiveBuffer [MESSAGE_MAX];
  ubyte messageByte [1] = {0x01};
  tByteArr dataArr;
  setBluetoothRawDataMode();
  while(!bBTRawMode){
    wait1Msec(1);
  }

  nxtDisplayRICFile(0, 0, picText[0]);

  while (menuSelect != 2)
  {
    while(nNxtButtonPressed != 3){
      if(nNxtButtonPressed == 1){
        menuSelect++;
        while(nNxtButtonPressed != -1){}
      }
      else if(nNxtButtonPressed == 2){
        menuSelect--;
        while(nNxtButtonPressed != -1){}
      }
      if(menuSelect != prevSelect){
        if(menuSelect == 3){
          menuSelect = 0;
        }
        else if (menuSelect == -1){
          menuSelect = 2;
        }
        prevSelect = menuSelect;
        nxtDisplayRICFile(0, 0, picText[menuSelect]);
      }
    }

    if (menuSelect == 0){
      eraseDisplay();
      resetMotors();
      while(true){
        nxtDisplayTextLine(1, "%d", nBTCurrentStreamIndex);
        if(nBTCurrentStreamIndex >=0){
          resetMotors();
          while(receiveBuffer[0] == 0x00){
            nxtReadRawBluetooth(receiveBuffer, MESSAGE_MAX);
          }
          /*bool handshake = true;
          for(int i = 2; i < MESSAGE_MAX - 2; i++){
          if(receiveBuffer[i] != 0x03){
          handshake = false;
          }
          }
          byte handshakeArr[2] = {0x03, 0x03};
          nxtWriteRawBluetooth(handshakeArr, 2);*/
          for(forLoopCount = 0; forLoopCount < MESSAGE_MAX; forLoopCount++){
            dataArr.array[forLoopCount] = receiveBuffer[forLoopCount];
          }
          bool end = false;
          for(forLoopCount = 0; forLoopCount < MESSAGE_MAX; forLoopCount++){
            end = dataArr.array[forLoopCount] == '_';
          }
          if(end){
            break;
          }
          dataArr.length = dataChk(dataArr);
          while(dataArr.length == -1){
            for(forLoopCount = 0; forLoopCount < MESSAGE_MAX; forLoopCount++){
              receiveBuffer[forLoopCount] = 0x00;
            }
            messageByte[0] = 0x01;
            nxtWriteRawBluetooth(messageByte, 1);
            while(receiveBuffer[0] == 0x00){
              nxtReadRawBluetooth(receiveBuffer, MESSAGE_MAX);
            }
            for(forLoopCount = 0; forLoopCount < MESSAGE_MAX; forLoopCount++){
              dataArr.array[forLoopCount] = receiveBuffer[forLoopCount];
            }
            dataArr.length = dataChk(dataArr);
          }
          messageByte[0] = 0x00;
          nxtWriteRawBluetooth(messageByte, 1);
          commandToAction(dataArr);
          memset(receiveBuffer, 0, sizeof(receiveBuffer));
          for(forLoopCount = 0; forLoopCount < dataArr.length; forLoopCount++){
            dataArr.array[forLoopCount] = 0x00;
          }
          dataArr.length = 0x00;
          if(!waitTurnTimer()){

            for(forLoopCount = 0; forLoopCount < MESSAGE_MAX; forLoopCount++){
              dataArr.array[forLoopCount] = 0x00;
            }
            ubyte temp[8] = {'T', 'I', 'M', 'E', '_', 'O', 'U', 'T'};
            for(forLoopCount = 0; forLoopCount < 8; forLoopCount++){
              dataArr.array[forLoopCount] = temp[forLoopCount];
            }
            break;
          }
          nxtDisplayTextLine(3, "Game Over");
          if(receiveBuffer[1] == 0x41){

          }
          ubyte temp[1] = {0x00};
          nxtWriteRawBluetooth(temp, 1);
          wait1Msec(500);
          receiveBuffer[0] = 0x01;
          do{
            memset(receiveBuffer, 0, sizeof(receiveBuffer));
            nxtReadRawBluetooth(receiveBuffer, MESSAGE_MAX);
          }while(receiveBuffer[0] != 0x00);
          resetMotors();
        }

      }

    }
    else if (menuSelect == 1){
      eraseDisplay();
      while(nNxtButtonPressed != -1){}
      nxtDisplayTextLine(1, "00:16:53:0A:BC:AD");
      calibrate();
      while(nNxtButtonPressed == -1){}
      nxtDisplayRICFile(0, 0, picText[menuSelect]);
      while(nNxtButtonPressed != -1){}
    }
  }
}
