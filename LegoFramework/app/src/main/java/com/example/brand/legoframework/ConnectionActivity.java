package com.example.brand.legoframework;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConnectionActivity extends AppCompatActivity {
    private BluetoothAdapter bAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        bAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void onClick (View view) {
        String address = ((EditText)findViewById(R.id.Mac_Add)).getText().toString();
    }
}
