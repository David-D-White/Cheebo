package io.github.daviddwhite.cheebo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessingThread extends AsyncTask<CameraView, String, Boolean> {
    AsyncResponse delegate = null;

    @Override
    protected Boolean doInBackground(CameraView... cameraViews) {
        cameraViews[0].addCameraListener(new CameraListener() {
            @SuppressLint("WrongThread")
            @Override
            public void onPictureTaken(byte[] jpeg) {
                CameraUtils.decodeBitmap(jpeg, Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        });
        return true;
    }

    @Override
    protected void onPreExecute(){

    }

    @Override
    protected void onProgressUpdate(String... progress){

    }

    @Override
    protected void onPostExecute(Boolean result){
        delegate.processFinish(result);
    }
}
