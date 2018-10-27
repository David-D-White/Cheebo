package com.example.brand.legoframework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

//Java class (tentatively)complete
//TODO: Create user friendly UI - implement custom action bar for app nav
public class ConnectionActivity extends AppCompatActivity {
    private Set<BluetoothDevice> devices;
    private BluetoothDevice selectedDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = bAdapter.getBondedDevices();
    }

    public void onClick (View view) {
        String address = ((EditText)findViewById(R.id.Mac_Add)).getText().toString();
        for (BluetoothDevice b :
                devices) {
            if(b.getAddress().equals(address)) {
                selectedDevice = b;
            }
        }
        try {
            FileOutputStream fileOut;
            int length = selectedDevice.getName().getBytes().length + selectedDevice.getAddress().length() + 1;
            fileOut = openFileOutput(getResources().getString(R.string.current_device), 0);
            fileOut.write(length);
            fileOut.write(32);
            fileOut.write(selectedDevice.getName().getBytes());
            fileOut.write(32);
            fileOut.write(selectedDevice.getAddress().getBytes());
            fileOut.close();

        }
        catch(IOException e) {
            Log.d("IO ERROR", "Failed to write data file.");
            e.printStackTrace();
        }
    }
}
