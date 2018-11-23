package io.github.daviddwhite.cheebo;

import android.bluetooth.BluetoothSocket;

public class DataClass {
    private byte[] picBytes;
    private BluetoothSocket btSocket;
    public DataClass(byte[] picBytes, BluetoothSocket btSocket){
        this.picBytes = picBytes;
        this.btSocket = btSocket;
    }

    public byte[] getPicBytes() {
        return picBytes;
    }

    public void setPicBytes(byte[] picBytes) {
        this.picBytes = picBytes;
    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }
}
