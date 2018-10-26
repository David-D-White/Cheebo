//I'm lazy
//Refer to comments on struct version
//Program flow should be more or less the same
task main ()
{
  string str1 = "";
  const int kMaxSizeOfMessage = 20;
  int length = 8;
  ubyte nReceiveBuffer[kMaxSizeOfMessage];
  byte messageByte[1];
  bool validMessage = false;

  setBluetoothRawDataMode();
  while(!bBTRawMode)
  {
    wait1Msec(1);
  }

  while(nNxtButtonPressed != 3){
    if(nBTCurrentStreamIndex >= 0)  // if there is currently an open Bluetooth connection:
    {
      nxtDisplayTextLine(1,"Success");
      while(nReceiveBuffer[0] == 0)
      {
        nxtReadRawBluetooth(nReceiveBuffer, kMaxSizeOfMessage);
      }
      do
      {
        if(nReceiveBuffer[0] == 2)
        {
          for(int i = 0; i < kMaxSizeOfMessage && !validMessage; i++)
          {
            if(nReceiveBuffer[i] == 4)
            {
              validMessage = true;
            }
          }
        }
        if(!validMessage)
        {
          for(int i = 0; i < kMaxSizeOfMessage; i++)
          {
            nReceiveBuffer[i] = 0;
          }
          messageByte[0] = 1;
          nxtWriteRawBluetooth(messageByte, 1);
          while(nReceiveBuffer[0] == 0)
          {
            nxtReadRawBluetooth(nReceiveBuffer, kMaxSizeOfMessage);
          }
        }
      }while(!validMessage);
      messageByte[0] = 0;
      nxtWriteRawBluetooth(messageByte, 1);
      for(int i = 0; i < kMaxSizeOfMessage; i++){
        str1 += nReceiveBuffer[i];
      }
      nxtDisplayTextLine(2,"%s",str1);
      str1 = "";
      for(int i = 0; i < kMaxSizeOfMessage; i++)
      {
        nReceiveBuffer[i] = 0;
      }
      validMessage = false;
    }
    else{
      nxtDisplayTextLine(1,"Fail");
      nxtDisplayTextLine(5,"%3d, %3d",cCmdBTCheckStatus(nBTCurrentStreamIndex));
    }

    wait1Msec(20);

  }
  wait1Msec(10);
  btDisconnectAll();
  wait1Msec(10);

}
