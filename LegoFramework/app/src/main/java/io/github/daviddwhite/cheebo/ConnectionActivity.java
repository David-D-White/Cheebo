package io.github.daviddwhite.cheebo;

import android.bluetooth.BluetoothDevice;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.otaliastudios.cameraview.CameraView;

import java.util.Set;

//Java class (tentatively)complete
//TODO: Create user friendly UI - implement custom action bar for app nav
public class ConnectionActivity extends AppCompatActivity {
    private Set<BluetoothDevice> devices;
    private BluetoothDevice selectedDevice;
    CameraView cameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CameraView camera = findViewById(R.id.camera);
    }

    public void onClick (View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
