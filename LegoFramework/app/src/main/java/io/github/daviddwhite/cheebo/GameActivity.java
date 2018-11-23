package io.github.daviddwhite.cheebo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

//TODO: Implement the actual game once algorithms and such are complete - Most dev will be here
//TODO: Implement camera
//TODO: Populate empty UI - Custom action bar - App nav
public class GameActivity extends AppCompatActivity  {
    private BluetoothAdapter bAdapter;
    private BluetoothDevice currentDevice;
    private CameraKitView cameraKitView;
    private BluetoothSocket btSocket;
    public static final int GAME_SUCCESS = 0, GAME_FAILURE = 1, BT_FAILURE = 2, TIMEOUT = 3, REQUEST_CLOSE = 4;
    private ProcessingThread processingTask = new ProcessingThread();

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
            if (b.getAddress().equals(data[1])) {
                currentDevice = b;
            }
        }

        try{
            btSocket = currentDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            btSocket.connect();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        cameraKitView = findViewById(R.id.camera);
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] photo) {
                File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
                Bitmap temp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "photo.jpg");
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(photo);
                    outputStream.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    Log.e("CKDemo", "Exception in photo callback");
                }
            }
        });

    }
    //region OVERRIDES
    @Override
    protected void onStart(){
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        try{
            btSocket.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //endregion
}
