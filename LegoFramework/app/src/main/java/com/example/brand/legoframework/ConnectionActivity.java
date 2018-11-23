package com.example.brand.legoframework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

//Java class (tentatively)complete
//TODO: Create user friendly UI - implement custom action bar for app nav
public class ConnectionActivity extends AppCompatActivity {
    private BluetoothDevice selectedDevice;
    private ArrayList<String> devices = new ArrayList<>();
    private BluetoothAdapter bAdapter;
    private Handler mHandler = new Handler();
    private ArrayList<BluetoothDevice> bDevices = new ArrayList<>();
    private ArrayAdapter<String> viewAdapter;
    private ListView deviceList;
    private boolean isScan = false;
    private Button button;
    private static final long SCAN_PERIOD = 10000;


    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String temp = device.getName() + "\n" + device.getAddress();
                            if (!devices.contains(temp)) {
                                devices.add(temp);
                                bDevices.add(device);
                                Log.d("test", temp);
                                viewAdapter.notifyDataSetChanged();
                                deviceList.setAdapter(viewAdapter);
                            }
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        button = findViewById(R.id.button);
        setContentView(R.layout.activity_connection);
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = findViewById(R.id.deviceList);
        deviceList.setAdapter(viewAdapter);
        viewAdapter = new ArrayAdapter<>(this, R.layout.list_item, devices);
        deviceList.setTextFilterEnabled(true);
        deviceList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScan) {
                    onClick(null);
                }
                try {
                    OutputStream fout = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.txt");
                    BluetoothDevice b = bDevices.get(position);
                    fout.write(b.getName().getBytes());
                    fout.write(32);
                    fout.write(b.getAddress().getBytes());
                    fout.flush();
                    fout.close();
                }
                catch(IOException ignored) {
                }
            }
        });
    }

    public void onClick (View view) {
        if (bAdapter.isEnabled()) {
            if (button.getText().equals(getString(R.string.scan_button_1))) {
                button.setText(R.string.scan_button_2);
                scanLeDevice(true);
            } else {
                button.setText(R.string.scan_button_1);
                bAdapter.stopLeScan(leScanCallback);
                scanLeDevice(false);
            }
        } else {
            Toast.makeText(this, "Bluetooth is not enabled! Enable bluetooth to continue.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bAdapter.stopLeScan(leScanCallback);
                    isScan = false;
                    Log.d("test", "timer");
                    button.setText("Begin Scan");
                }
            }, SCAN_PERIOD);
            bAdapter.startLeScan(leScanCallback);
            isScan = true;
        } else {
            bAdapter.stopLeScan(leScanCallback);
            isScan = false;
        }
    }
}
