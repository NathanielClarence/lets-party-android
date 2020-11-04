package com.example.letsparty.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityJoinGameBinding;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class JoinGame extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private String roomCode;
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    50); }

        setContentView(R.layout.activity_join_game);

        // set View and bindings
        ActivityJoinGameBinding binding = ActivityJoinGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnJoinRoom.setOnClickListener(view -> this.joinRoom(binding.edtRoom.getText().toString(), binding.edtName.getText().toString()));
        binding.btnScanQR.setOnClickListener(view -> this.qrCodeScan());

        try{
            Intent intent = getIntent();
            roomCode = (String) intent.getStringExtra("SCANRESULT");
            binding.edtRoom.setText(roomCode);
            roomCode = "";
        }catch (Exception e){
            Log.println(Log.ERROR, "EXC", e.toString());
        }

        // QRCode scan settings
        mScannerView = new ZXingScannerView(this);
    }

    private void joinRoom(String room, String playerName){
        roomCode = room;
        Log.println(Log.INFO, "JOIN ROOM: ", "joining "+roomCode);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("roomCode", roomCode);
        returnIntent.putExtra("playerName", playerName);
        setResult(Activity.RESULT_OK, returnIntent);

        this.finish();
    }

    private void qrCodeScan(){
        //open cam and start scan qrcode
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        // Prints scan results
        Log.println(Log.INFO, "RESULT", rawResult.getText());
        // Prints the scan format (qrcode, pdf417 etc.)
        //Logger.verbose("result", rawResult.getBarcodeFormat().toString());
        //If you would like to resume scanning, call this method below:
        roomCode = rawResult.getText();
        Log.println(Log.INFO, "NN", roomCode.substring(0,11));
        if (roomCode.substring(0,11).equals("letsparty::")){
            //send scan result to this class if qrcode is valid
            Intent joinIntent = new Intent(this, JoinGame.class);
            joinIntent.putExtra("SCANRESULT", roomCode.substring(11));
            startActivity(joinIntent);
        }else {
            //invalid qrcode result
            Log.println(Log.INFO, "QRCODE:", "Invalid");
            mScannerView.resumeCameraPreview(this);
        }
    }
}