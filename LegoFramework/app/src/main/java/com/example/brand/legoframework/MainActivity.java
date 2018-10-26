package com.example.brand.legoframework;

import android.annotation.TargetApi;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static BluetoothDevice bDevice;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_ENABLE_FINE_LOCATION = 2;
    private final int REQUEST_ENABLE_COARSE_LOCATION = 3;
    private final int[] REQUEST_CODES = {REQUEST_ENABLE_FINE_LOCATION, REQUEST_ENABLE_COARSE_LOCATION, REQUEST_ENABLE_BT, REQUEST_ENABLE_BT};

    //region PERMISSIONS
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
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onClick (View view) {
        String source = ((Button) view).getText().toString();
        switch (source) {
            case "Start":
                //TODO: Implement game
                break;

            case "Connect":
                //TODO: Interface with connection screen
                Intent connectIntent = new Intent(MainActivity.this, ConnectionActivity.class);
                TaskStackBuilder screenStack = TaskStackBuilder.create(MainActivity.this);
                screenStack.addNextIntent(new Intent (MainActivity.this, MainActivity.class));
                screenStack.addNextIntent(connectIntent);
                break;

            case "Exit":
                //TODO: Implement exit ops
                break;
        }
    }


}
