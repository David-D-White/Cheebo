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
import android.graphics.Matrix;
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
import org.petero.cuckoo.engine.chess.ComputerPlayer;
import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.HumanPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ca.daviddwhite.deep_chess.ChessNet;

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
    private boolean isWhite = false;
    private final Boolean HUMUS = false;
    private String fen = "";
    private int count = 0;

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
                    fen = getBoard(isWhite);
                    Log.d("test", "test3");


            }
        });
        try{
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            List<BluetoothDevice> list = new ArrayList<>(btAdapter.getBondedDevices());
            bDevice = btAdapter.getRemoteDevice(list.get(0).getAddress());
            bSocket = bDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bSocket.connect();
        Log.d("Connection Success", bDevice.getName() + " : " + bDevice.getAddress());
    }
        catch (IOException e){e.printStackTrace();}
        cameraView.start();
        start = findViewById(R.id.Start);
        addressText = findViewById(R.id.editText);
    }

    private void sendMessage(String message, BluetoothSocket bSocket){
        StringBuilder str = new StringBuilder(20);
        str.append((char)0x02);
        str.append((char)0x02);
        str.append((char)0x02);
        str.append(message);
        str.append((char)0x04);
        str.append((char)0x04);
        str.append((char)0x04);
        try{
            Log.d("test", "test");
            OutputStream out = bSocket.getOutputStream();
            out.write(str.toString().getBytes());
            out.flush();
        }
        catch(IOException e){e.printStackTrace();}
    }

    private void msgLoop(String message, BluetoothSocket bSocket){
        int length = 0;
        try {
            char result = ' ';
            do {
                sendMessage(message, bSocket);
                try {
                    Thread.sleep(50);
                }
                catch(InterruptedException e){ e.printStackTrace();}
                do {
                    byte[] input = new byte[1];
                    InputStream in = bSocket.getInputStream();
                    while(in.available() == 0){}
                    in.read(input);
                    result = (char)input[0];
                } while (result == ' ');
            }while (result == ' ' || result != 0);
        }
        catch (IOException ignored){}
    }

    public void onClick(View view) {
        String source = ((Button)view).getText().toString();
        switch (source) {
            case "Start":
                isWhite = false;
                cameraView.capturePicture();
                start.setEnabled(false);
                try{
                    HumanPlayer h = new HumanPlayer();
                    ComputerPlayer net = new ComputerPlayer();
                    net.timeLimit(4000,4000,false);
                    net.useBook(true);
                    CheeboGame game;
                    isWhite = false;

                    if(isWhite){
                        game = new CheeboGame(net, h);
                        String command = net.getCommand(game.pos, false, game.getHistory());
                        String message = game.moveToBluetoothString(command);
                        game.processString(command);
                        msgLoop(message, bSocket);
                    }
                    else{
                        game = new CheeboGame(h, net);
                    }
                    Log.d("test", "test");
                    InputStream in = bSocket.getInputStream();
                    OutputStream out = bSocket.getOutputStream();
                    while(game.getGameState() == Game.GameState.ALIVE){
                        try{
                            Log.d("test", "test1");
                            while(in.available() == 0){}
                            String oldFen = fen;
                            cameraView.capturePicture();
                            Log.d("", fen);
                            while(oldFen == fen){}
                            if(game.processFEN(fen)){
                                Log.d("test", "test2");
                                if(game.getGameState() == Game.GameState.ALIVE){
                                    Log.d("test", "test2");

                                    String command = net.getCommand(game.pos, HUMUS, game.getHistory());
                                    String message = game.moveToBluetoothString(command);
                                    game.processString(command);
                                    msgLoop(message, bSocket);
                                    Log.d("test", "test4");

                                    out.write(new byte[]{0x02, 0x41, 0x04});
                                }
                            }
                            else
                            {
                                out.write(new byte[] {0x02, 0x42, 0x04});
                            }
                        }
                        catch (IOException e){}
                    }

                    StringBuilder str = new StringBuilder(20);
                    str.append(game.getGameState());
                    try {
                        out.write(str.toString().getBytes());
                    }
                    catch(IOException e){}
                    /*if(count == 0)
                        //addressText.getText().toString();
                        msgLoop(addressText.getText().toString(), bSocket);
                    /*else if (count == 1)
                        msgLoop("BM0704R76", bSocket);
                    else
                        msgLoop("BR37", bSocket);

                    count++;*/
                }
                catch (IOException e){
                    Toast.makeText(this, "Device not found!", Toast.LENGTH_LONG).show();
                }
                catch (IllegalArgumentException e){
                    Toast.makeText(this, "That is not a valid address!", Toast.LENGTH_LONG).show();
                }
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

    //region COMPUTER VISION
    public String getBoard(boolean isWhite)
    {
        if(!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
            return "No CV";
        } else {
            Log.d("SUCCESS", "OpenCV loaded");

            String[][] board = new String[8][8];

            Bitmap bitmap = bmp;
            Matrix matrix = new Matrix();
            //matrix.setRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            recognizePiece("p", new Scalar(110,165,110), new Scalar(180,280,255), bitmap, board, false); // red = black pawn
            recognizePiece("r", new Scalar(90, 200, 100), new Scalar(100, 255, 255), bitmap, board, false); // Yellow = black rook
            recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), bitmap, board, false); // blue = white pawn
            recognizePiece("R", new Scalar(125, 40, 120), new Scalar(160, 90, 227), bitmap, board, false); // Pink = white rook
            recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), bitmap, board, true); // blue = white pawn
            recognizePiece("R", new Scalar(135, 40, 155), new Scalar(137, 47, 157), bitmap, board, true); // Pink = white rook


            Log.d("SUCCESS", " Board: " + Arrays.deepToString(board).replace("], ", "]\n"));

            String FEN = "";
            int emptyCount = 0;
            if(isWhite == false)
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


    public void recognizePiece(String Piece, Scalar colorLow , Scalar colorHigh, Bitmap bitmap, String[][] board, boolean isKing)
    {
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
                                } else if (board[y - 1][x - 1].equals("b") && Piece.equals("P") && isKing == true) // red blue yellow??
                                {
                                    board[y - 1][x - 1] = "k";
                                } else if (board[y - 1][x - 1].equals("B") && Piece.equals("R") && isKing == true) // blue pink red??
                                {
                                    board[y - 1][x - 1] = "K";
                                }
                            }else
                            {
                                if(isKing == false)
                                {
                                    board[y - 1][x - 1] = Piece;
                                }
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


    public boolean isWhite()          //Something Something Ravioli
    {
        Log.d("SUCCESS", "OpenCV loaded");

        String[][] board = new String[8][8];

        Bitmap bitmap = bmp;
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        recognizePiece("p", new Scalar(110,165,110), new Scalar(180,280,255), bitmap, board, false); // red = black pawn
        recognizePiece("r", new Scalar(90, 200, 100), new Scalar(100, 255, 255), bitmap, board, false); // Yellow = black rook
        recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), bitmap, board, false); // blue = white pawn
        recognizePiece("R", new Scalar(125, 40, 120), new Scalar(160, 90, 227), bitmap, board, false); // Pink = white rook
        recognizePiece("P", new Scalar(0,70,100), new Scalar(30,360,360), bitmap, board, true); // blue = white pawn
        recognizePiece("R", new Scalar(135, 40, 155), new Scalar(137, 47, 157), bitmap, board, true); // Pink = white rook

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

    //endregion

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
        try {
            bSocket.close();
        }
        catch (IOException ignored){}
        super.onDestroy();
        cameraView.destroy();
    }
    //endregion
}
