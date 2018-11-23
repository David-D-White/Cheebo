package io.github.daviddwhite.cheebo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Prototype/framework for the app. Complies and runs but it doesn't really do anything as of now
//This activity is more or less complete
//TODO: Check exit ops and program shutdown procedure
//TODO: Improve UI - custom action bar
public class MainActivity extends AppCompatActivity {
    public static String DEVICE_KEY = "Device_DAT";
    public Bitmap bmp;
    private final String IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.png";
    private final int REQUEST_ENABLE_BT = 1, REQUEST_ENABLE_FINE_LOCATION = 2, REQUEST_ENABLE_COARSE_LOCATION = 3, REQUEST_ENABLE_CAMERA = 4;
    private final int[] REQUEST_CODES = {REQUEST_ENABLE_FINE_LOCATION, REQUEST_ENABLE_COARSE_LOCATION, REQUEST_ENABLE_BT, REQUEST_ENABLE_BT, REQUEST_ENABLE_CAMERA};
    private CameraView cameraView;
    private BluetoothAdapter btAdapter;
    private Button start;
    private EditText addressText;
    private BluetoothDevice bDevice;
    private BluetoothSocket bSocket;
    private boolean ravioli = false;

    //region PERMISSIONS
    private void checkPermissions() {
        String[] permissions = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.CAMERA"};
        Boolean[] results = new Boolean[5];
        for (int i = 0; i < permissions.length; i++) {
            results[i] = this.checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED;
            if (!results[i]) {
                ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, REQUEST_CODES[i]);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_ENABLE_CAMERA);
        }
        cameraView = findViewById(R.id.camera);
        cameraView.addCameraListener(new CameraListener() {
            @SuppressLint("WrongThread")
            @Override
            public void onPictureTaken(byte[] jpeg) {
                    bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                    Log.d("IMAGE", getBoard(ravioli));

            }
        });
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        start = findViewById(R.id.Start);
        addressText = findViewById(R.id.editText);
    }

    public void onClick(View view) {
        String source = ((Button)view).getText().toString();
        switch (source) {
            case "Start":
                ravioli = isWhite();
                cameraView.capturePicture();
                /*start.setEnabled(false);
                String address = addressText.getText().toString();
                try{
                    bDevice = btAdapter.getRemoteDevice(address);
                    bSocket = bDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    bSocket.connect();
                    boolean ravioli = false; //Implement game state check
                    while(!ravioli){}
                }
                catch (IOException e){
                    Toast.makeText(this, "Device not found!", Toast.LENGTH_LONG).show();
                }
                catch (IllegalArgumentException e){
                    Toast.makeText(this, "That is not a valid address!", Toast.LENGTH_LONG).show();
                }*/
                break;

            case "Exit":
                finish();
                if(bSocket.isConnected()){
                    try {
                        bSocket.close();
                    }
                    catch(IOException ignored){}
                }
                System.exit(0);
                break;
        }
    }

    public String getBoard(boolean isWhite)
    {
        if(!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
            return "No CV";
        } else {
            Log.d("SUCCESS", "OpenCV loaded");

            String[][] board = new String[8][8];

            recognizePiece("p", new Scalar(110,185,110), new Scalar(180,280,255), board, false); // red = black pawn
            recognizePiece("r", new Scalar(90, 200, 100), new Scalar(100, 255, 255), board, false); // Yellow = black rook
            recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), board, false); // blue = white pawn
            recognizePiece("R", new Scalar(125, 40, 120), new Scalar(160, 90, 227), board, false); // Pink = white rook
            recognizePiece("p", new Scalar(110,185,110), new Scalar(180,280,255), board, true); // red = black pawn

            Log.d("SUCCESS", " Board: " + Arrays.deepToString(board).replace("], ", "]\n"));

            String FEN = "";
            int emptyCount = 0;
            if(isWhite == true)
            {
                for (int x = 7; x > 0; x--)
                {
                    for (int y = 7; y > 0; y--)
                    {
                        if(board[x][y] == null)
                        {
                            emptyCount++;
                        }else
                        {
                            if(emptyCount != 0)
                            {
                                FEN += emptyCount;
                                emptyCount = 0;
                            }
                            FEN += board[x][y];
                        }

                    }
                    if(emptyCount != 0)
                    {
                        FEN += emptyCount;
                        emptyCount = 0;
                    }
                    FEN += "/";
                }
                return FEN;
            }
            else
            {
                for (int x = 0; x < 8; x++)
                {
                    for (int y = 0; y < 8; y++)
                    {
                        if(board[x][y] == null)
                        {
                            emptyCount++;
                        }else
                        {
                            if(emptyCount != 0)
                            {
                                FEN += emptyCount;
                                emptyCount = 0;
                            }
                            FEN += board[x][y];
                        }

                    }
                    if(emptyCount != 0)
                    {
                        FEN += emptyCount;
                        emptyCount = 0;
                    }
                    FEN += "/";
                }
                Log.d("SUCCESS", " Board: " + FEN);
                return FEN;
            }
        }
    }


    public void recognizePiece(String Piece, Scalar colorLow , Scalar colorHigh, String[][] board, boolean isKing)
    {
        Bitmap bitmap = bmp;
        Mat hsv_image = new Mat();
        Utils.bitmapToMat(bitmap, hsv_image);

        Imgproc.medianBlur(hsv_image, hsv_image, 3);
        Log.d("SUCCESS", "Bitmap to Mat Compleated for: " + Piece);

        Imgproc.cvtColor(hsv_image, hsv_image, Imgproc.COLOR_BGR2HSV);
        Log.d("SUCCESS", "Bitmap to Mat Compleated for: " + Piece);

        Core.inRange(hsv_image, colorLow, colorHigh, hsv_image);

        Mat red_hue_image = new Mat();
        Size size = new Size(9, 9);
        Imgproc.GaussianBlur(hsv_image, red_hue_image, size, 2, 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(red_hue_image.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Rect box;

        // For each contour
        for (int i = 0; i < contours.size(); ++i) {
            // Get the bounding box
            box = Imgproc.boundingRect(contours.get(i));

            // Draw the box on the original image in red
            Imgproc.rectangle(red_hue_image, new Point(box.x, box.y), new Point(box.x + box.width, box.y + box.height), new Scalar(0, 0, 255), 5);
            Log.d("SUCCESS", " Contour Compleated " + box.x + ", " + box.y + "--" + box.width + ", " + box.height);

            for (int x = 1; x < 9; x++) {
                for (int y = 1; y < 9; y++) {
                    if ((box.x + (box.width-3)) < (red_hue_image.width() / 8) * x) {
                        if ((box.y + (box.width-3)) < (red_hue_image.height() / 8) * y) {
                            if(board[y-1][x-1] != null) {
                                if (board[y - 1][x - 1].equals("p") && Piece.equals("r") && isKing == false) //red yellow
                                {
                                    board[y - 1][x - 1] = "b";
                                } else if (board[y - 1][x - 1].equals("p") && Piece.equals("P") && isKing == false) // red blue
                                {
                                    board[y - 1][x - 1] = "B";
                                } else if (board[y - 1][x - 1].equals("p") && Piece.equals("R") && isKing == false) //red pink
                                {
                                    board[y - 1][x - 1] = "N";
                                } else if (board[y - 1][x - 1].equals("r") && Piece.equals("P") && isKing == false) // yellow blue
                                {
                                    board[y - 1][x - 1] = "n";
                                } else if (board[y - 1][x - 1].equals("r") && Piece.equals("R") && isKing == false) // yellow pink
                                {
                                    board[y - 1][x - 1] = "q";
                                } else if (board[y - 1][x - 1].equals("P") && Piece.equals("R") && isKing == false) // blue pink
                                {
                                    board[y - 1][x - 1] = "Q";
                                } else if (board[y - 1][x - 1].equals("n") && Piece.equals("p") && isKing == true) // red blue yellow??
                                {
                                    board[y - 1][x - 1] = "k";
                                } else if (board[y - 1][x - 1].equals("q") && Piece.equals("p") && isKing == true) // blue pink black??
                                {
                                    board[y - 1][x - 1] = "K";
                                }
                            }
                                    if (isKing == false) {
                                        board[y - 1][x - 1] = Piece;
                                    }

                            Log.d("SUCCESS", " Board: " + x + ", " + y);
                            y = 10;
                            x = 10;
                        }
                    }
                }
            }
        }
    /*
    Bitmap output1 = Bitmap.createBitmap(hsv_image.cols(), hsv_image.rows(), Bitmap.Config.ARGB_8888);

    Utils.matToBitmap(red_hue_image, output1);
    imgView.setImageBitmap(output1);

    Bitmap output2 = Bitmap.createBitmap(hsv_image.cols(), hsv_image.rows(), Bitmap.Config.ARGB_8888);
    Utils.matToBitmap(hsv_image, output2);
    imgView2.setImageBitmap(output2);
    */

    }

    public boolean isWhite()
    {
        Log.d("SUCCESS", "OpenCV loaded");

        String[][] board = new String[8][8];

        recognizePiece("p", new Scalar(110,185,110), new Scalar(180,280,255), board, false); // red = black pawn
        recognizePiece("r", new Scalar(90, 200, 100), new Scalar(100, 255, 255), board, false); // Yellow = black rook
        recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), board, false); // blue = white pawn
        recognizePiece("R", new Scalar(125, 40, 120), new Scalar(160, 90, 227), board, false); // Pink = white rook
        recognizePiece("p", new Scalar(110,185,110), new Scalar(180,280,255), board, true); // red = black pawn

        Log.d("SUCCESS", " Board: " + Arrays.deepToString(board).replace("], ", "]\n"));

        String FEN = "";
        int emptyCount = 0;
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                if(board[x][y] == null)
                {
                    emptyCount++;
                }else
                {
                    if(emptyCount != 0)
                    {
                        FEN += emptyCount;
                        emptyCount = 0;
                    }
                    FEN += board[x][y];
                }

            }
            if(emptyCount != 0)
            {
                FEN += emptyCount;
                emptyCount = 0;
            }
            FEN += "/";
        }

        if(FEN.split("/")[0].toLowerCase().equals(FEN.split("/")[0]))
        {
            return false;
        }else
        {
            return true;
        }
    }

    //region REQUIRED_OVERRIDES
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
    //endregion
}
