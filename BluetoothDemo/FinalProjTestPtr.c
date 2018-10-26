//Byte array type
//Used to allow param pass of array
typedef struct
{
  int length;
  byte array [20];
}tByteArr;

//Validate data and determine length
int dataChk (tByteArr arr)
{
  int length = -1;
  //Check for start byte
  if(arr.array[0] != 0x02)
  {
    return -1;
  }
  //Look for end byte
  for(int i = 0; i < 20; i++)
  {
    if(arr.array[i] == 0x04)
    {
      return i + 1;
    }
  }
  return length;
}

//Main
task main ()
{
  //Message string
  string str1 = "";
  //Max length of string in RobotC
  const int MESSAGE_MAX = 20;
  //Default length (-1 for error checking)
  int length = -1;
  //Byte array for input
  ubyte nReceiveBuffer[MESSAGE_MAX];
  //Message byte
  byte messageByte[1];
  setBluetoothRawDataMode();
  while(!bBTRawMode)
  {
    wait1Msec(1);
  }

  while(nNxtButtonPressed != 3)
    {
    //If exists a connection
    if(nBTCurrentStreamIndex >= 0)
    {
      //While no input has been received
      while(nReceiveBuffer[0] == 0x00)
      {
        nxtReadRawBluetooth(nReceiveBuffer, MESSAGE_MAX);
      }
      //Copy data to array struct for param pass
      tByteArr dataArr;
      for(int i = 0; i < MESSAGE_MAX; i++)
      {
        dataArr.array[i] = nReceiveBuffer[i];
      }
      //Check data
      length = dataChk(dataArr);
      //If data is bad request new transmission
      while(length == -1)
      {
        for(int i = 0; i < length; i++)
        {
          nReceiveBuffer[i] = 0x00;
        }
        messageByte[0] = 0x01;
        //Send request message
        nxtWriteRawBluetooth(messageByte, 1);
        while(nReceiveBuffer[0] == 0x00)
        {
          nxtReadRawBluetooth(nReceiveBuffer, length);
        }
        length = dataChk(dataArr);
      }
      //Send success message
      messageByte[0] = 0x00;
      nxtWriteRawBluetooth(messageByte, 1);
      //Update message string
      for(int i = 0; i < length; i++){
        str1 += nReceiveBuffer[i];
      }
      //Display string
      nxtDisplayTextLine(2,"%s",str1);
      str1 = "";
      //Clear byte arr
      memset(nReceiveBuffer, 0, sizeof(nReceiveBuffer));
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
