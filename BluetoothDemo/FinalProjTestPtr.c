typedef struct
{
  int length;
  byte array [20];
}tByteArr;

int dataChk (tByteArr arr)
{
  bool endMsg = false;
  int length = -1;
  if(arr.array[0] != 0x02)
  {
    return -1;
  }
  for(int i = 0; i < 20 && !endMsg; i++)
  {
    if(arr.array[i] == 0x04 && !endMsg)
    {
      endMsg = true;
      length = i + 1;
    }
  }
  return length;
}

task main ()
{
  string str1 = "";
  const int MESSAGE_MAX = 20;
  int length = -1;
  ubyte nReceiveBuffer[MESSAGE_MAX];
  byte messageByte[1];

  setBluetoothRawDataMode();
  while(!bBTRawMode)
  {
    wait1Msec(1);
  }

  while(nNxtButtonPressed != 3)
    {
    if(nBTCurrentStreamIndex >= 0)
    {
      while(nReceiveBuffer[0] == 0x00)
      {
        nxtReadRawBluetooth(nReceiveBuffer, MESSAGE_MAX);
      }

      tByteArr dataArr;
      for(int i = 0; i < MESSAGE_MAX; i++)
      {
        dataArr.array[i] = nReceiveBuffer[i];
      }
      length = dataChk(dataArr);
      while(length == -1)
      {
        for(int i = 0; i < length; i++)
        {
          nReceiveBuffer[i] = 0x00;
        }
        messageByte[0] = 0x01;
        nxtWriteRawBluetooth(messageByte, 1);
        while(nReceiveBuffer[0] == 0x00)
        {
          nxtReadRawBluetooth(nReceiveBuffer, length);
        }
        length = dataChk(dataArr);
      }
      messageByte[0] = 0x00;
      nxtWriteRawBluetooth(messageByte, 1);
      for(int i = 0; i < length; i++){
        str1 += nReceiveBuffer[i];
      }
      nxtDisplayTextLine(2,"%s",str1);
      str1 = "";
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
