package com.example.brand.androidtonxt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String NXT = "00:16:53:09:B5:41";
    private BluetoothSocket nxtSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_FINE_LOCATION = 2;
    private static final int REQUEST_ENABLE_COARSE_LOCATION = 3;
    private static final int[] REQUEST_CODES = {REQUEST_ENABLE_FINE_LOCATION, REQUEST_ENABLE_COARSE_LOCATION, REQUEST_ENABLE_BT, REQUEST_ENABLE_BT};

    private void checkPermissions() {
        String[] permissions = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"};
        Boolean[] results = new Boolean[4];
        for (int i = 0; i < permissions.length; i++) {
            results[i] = this.checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED;
            if (!results[i]) {
                ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, REQUEST_CODES[i]);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_COARSE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void sendMessage (byte[] msg)
    {
        try
        {
            char messageIn = ' ';
            int messageLength;
            do
            {
                OutputStream btout = nxtSocket.getOutputStream();
                btout.write(msg);
                btout.flush();
                do
                {
                    byte[] temp = new byte[1];
                    InputStream btin = nxtSocket.getInputStream();
                    messageLength = btin.read(temp);
                    messageIn = (char)temp[0];
                } while(messageLength == 0);
            }while(messageIn == ' ' || messageIn == (char)1);
        }
        catch (IOException e)
        {
            Log.d("Message Error: ", "Message failed to be sent." + e.getMessage());
            Toast.makeText(this, "Could not send message!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothAdapter localAdapter = BluetoothAdapter.getDefaultAdapter();
        checkPermissions();
        if (!localAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        List<BluetoothDevice> list = new ArrayList<>(localAdapter.getBondedDevices());
        BluetoothDevice nxt = localAdapter.getRemoteDevice(list.get(0).getAddress());
        try {
            nxtSocket = nxt.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            nxtSocket.connect();
            Log.d("Connection Success", nxt.getName() + " : " + nxt.getAddress());
        } catch (IOException e) {
            Log.d("Connection Error: ", "Could not connect to NXT.");
            Toast.makeText(this,"Could not connect!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy ()
    {
        try {
            nxtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void buttonClick(View view) {
        String viewName = ((Button)view).getText().toString();
        switch (viewName)
        {
            case "Send":
                String dataIn = ((EditText)findViewById(R.id.numText)).getText().toString();
                StringBuilder str = new StringBuilder();
                str.append((char) 0x2);
                str.append(dataIn);
                while(str.length() < 19){
                    str.append(" ");
                }
                str.append((char) 0x4);
                byte[] data = str.toString().getBytes();
                sendMessage(data);
                break;
        }
    }
}
