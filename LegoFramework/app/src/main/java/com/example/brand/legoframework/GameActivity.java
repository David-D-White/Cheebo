package com.example.brand.legoframework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Set;

//TODO: Implement the actual game once algorithms and such are complete - Most dev will be here
//TODO: Populate empty UI - Custom action bar - App nav
public class GameActivity extends AppCompatActivity {
    private BluetoothAdapter bAdapter;
    private BluetoothDevice currentDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        String deviceData = getIntent().getExtras().getParcelable(MainActivity.DEVICE_KEY).toString();
        String[] data = deviceData.split(" ");
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = bAdapter.getBondedDevices();
        for (BluetoothDevice b :
                devices) {
            if (b.getAddress().equals(data[1])){
                currentDevice = b;
            }
        }
    }
}
