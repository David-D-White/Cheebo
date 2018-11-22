#pragma config(Sensor, S2,     xTouch,         sensorTouch)
#pragma config(Sensor, S1,     yTouch,         sensorTouch)
#pragma config(Motor,  motorB,          xMotor,        tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorC,          yMotor,        tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorA,          clawMotor,     tmotorNormal, PIDControl, encoder)

const float SQUARE_DIST = 5.6;
const float CLAW_CONSTANT = 290;
const float ENC_CONST = 40.0 / 360.0 * 3.2 / 5.0 / 10.0;
const float ENC_TOL = 0.01;
const float OFFSETY = 9.9;
const float HALF_SQUARE = SQUARE_DIST/1.8/ENC_CONST;
const int SPEED = 70;

void resetMotors(){
  motor[xMotor] = -SPEED;
  motor[yMotor] = -SPEED;
  while(!SensorValue(xTouch) || !SensorValue(yTouch)){
    if(SensorValue(xTouch) != 0){
      motor[xMotor] = 0;
    }
    if(SensorValue(yTouch) != 0){
      motor[yMotor] = 0;
    }
  }
  motor[xMotor] = 0;
  motor[yMotor] = 0;
  wait1Msec(500);
  nMotorEncoder[xMotor] = 0;
  nMotorEncoder[yMotor] = 0;
}

void moveToPos(int xCoord, int yCoord){
  //Calculate current coordinates
  float curX = nMotorEncoder[xMotor] * ENC_CONST;
  float curY = nMotorEncoder[yMotor] * ENC_CONST;

  //Calculate necessary travel distances
  float deltaX = (float) xCoord * SQUARE_DIST - curX;
  float deltaY = (float)yCoord * SQUARE_DIST- curY;

  float startEncoder = 0, currentEncoder = 0;
  bool atStart = false;
  int dir = 0;

  if(SensorValue(yTouch)){
    deltaY += OFFSETY;
    atStart = true;
  }
  else{
    if(xCoord > 1){
      dir = 1;
    }
    else{
      dir = -1;
    }
    motor[xMotor] = -SPEED * dir;
    startEncoder = nMotorEncoder[xMotor] * ENC_CONST;
    while(abs(nMotorEncoder[xMotor] * ENC_CONST - startEncoder + dir * SQUARE_DIST / 1.8) > ENC_TOL){}
    motor[xMotor] = 0;

    motor[yMotor] = SPEED;
    startEncoder = nMotorEncoder[yMotor] * ENC_CONST;
    while(abs(nMotorEncoder[yMotor] * ENC_CONST - startEncoder - SQUARE_DIST / 1.8) > ENC_TOL){}
    motor[yMotor] = 0;
  }

  motor[xMotor] = SPEED * sgn(deltaX);
  startEncoder = nMotorEncoder[xMotor] * ENC_CONST;
  currentEncoder = startEncoder;
  while(abs(currentEncoder - startEncoder - deltaX) > ENC_TOL){
    currentEncoder = abs(nMotorEncoder[xMotor] * ENC_CONST);
  }
  motor[xMotor] = 0;

  motor[yMotor] = SPEED * sgn(deltaY);
  startEncoder = nMotorEncoder[yMotor] * ENC_CONST;
  currentEncoder = startEncoder;
  while(abs(currentEncoder - startEncoder - deltaY) > ENC_TOL){
    currentEncoder = abs(nMotorEncoder[yMotor] * ENC_CONST);
  }
  motor[yMotor] = 0;

  if(atStart){
    nMotorEncoder[yMotor] = 0;
  }
  else{
    motor[xMotor] = SPEED * dir;
    startEncoder = nMotorEncoder[xMotor] * ENC_CONST;
    while(abs(nMotorEncoder[xMotor] * ENC_CONST - startEncoder * dir - SQUARE_DIST / 1.8) > ENC_TOL){}
    motor[xMotor] = 0;

    motor[yMotor] = -SPEED;
    startEncoder = nMotorEncoder[yMotor] * ENC_CONST;
    while(abs(nMotorEncoder[yMotor] * ENC_CONST - startEncoder + SQUARE_DIST / 1.8) > ENC_TOL){}
    motor[yMotor] = 0;
  }
}

void newMove (int xCoord, int yCoord)
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
    motor[yMotor] = -SPEED/2;
    while (nMotorEncoder[yMotor] > yOff){}
    motor[yMotor] = 0;
  }

  wait1Msec(1000);

  float xOff = nMotorEncoder [xMotor] + halfX;
  if (xOff > nMotorEncoder[xMotor]){
    motor[xMotor] = SPEED/2;
    while (nMotorEncoder[xMotor] < xOff){}
    } else {
    motor[xMotor] = -SPEED/2;
    while (nMotorEncoder[xMotor] > xOff){}
  }
  motor[xMotor] = 0;

  wait1Msec(1000);

  //Calculate necessary travel targets
  float targetX = (xCoord * SQUARE_DIST) / ENC_CONST;
  if (xCoord > 0){
    targetX -= HALF_SQUARE;
  }
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

  wait1Msec(1000);

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

  wait1Msec(1000);

  //Undo offset
  if (halfY != 0)
  {
    float yOff = (yCoord * SQUARE_DIST + OFFSETY) / ENC_CONST;
    motor[yMotor] = SPEED/2;
    while (nMotorEncoder[yMotor] < yOff){}
    motor[yMotor] = 0;
  }

  wait1Msec(1000);

  xOff = (xCoord * SQUARE_DIST) / ENC_CONST;
  if (xOff > nMotorEncoder[xMotor]){
    motor[xMotor] = SPEED/2;
    while (nMotorEncoder[xMotor] < xOff){}
    } else {
    motor[xMotor] = -SPEED/2;
    while (nMotorEncoder[xMotor] > xOff){}
  }
  motor[xMotor] = 0;

  wait1Msec(1000);
}

void openClaw(){
  motor[clawMotor] = 20;
  while(nMotorEncoder[clawMotor] < CLAW_CONSTANT){}
  motor[clawMotor] = 0;
}

void closeClaw(){
  motor[clawMotor] = -20;
  while(nMotorEncoder[clawMotor] > 0){}
  motor[clawMotor] = 0;
  nMotorEncoder[clawMotor] = 0;
}

task main (){
  int coords[20][2] = {{ 0, 0 },
    { 7, 0 },
    { 7, 7 },
    { 0, 7 },
    { 1, 1 },
    { 6, 1 },
    { 6, 6 },
    { 1, 6 },
    { 2, 2 },
    { 5, 2 },
    { 5, 5 },
    { 2, 5 },
    { 3, 3 },
    { 3, 4 },
    { 4, 4 },
    { 4, 3 }};

  int menuSelect = 0, prevSelect = 0;
  string picText[3] = {"CHEEBO_PLAY.ric", "CHEEBO_SETTINGS.ric", "CHEEBO_EXIT.ric"};
  nxtDisplayRICFile(0, 0, picText[0]);

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
    resetMotors();
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    /*for(int i = 1; i < 16; i++){
    moveToPos(coords[i][0], coords[i][1]);
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    }*/
    newMove(1,1);
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    newMove(7,7);
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    newMove(7,0);
    /*moveToPos(3, 3);
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    openClaw();
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    moveToPos(5, 5);
    while(nNxtButtonPressed != 3){}
    while(nNxtButtonPressed != -1){}
    closeClaw();*/
  }
  else{
    return;
  }
}
