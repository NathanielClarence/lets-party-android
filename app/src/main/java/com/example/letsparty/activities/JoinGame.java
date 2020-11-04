package com.example.letsparty.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityJoinGameBinding;
import com.example.letsparty.serverconnector.ServerConnector;
//import com.example.letsparty.serverconnector.ServerUtil;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class JoinGame extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private String roomCode;
    private String uname;
    private ZXingScannerView mScannerView;
    private AlertDialog.Builder dialog;

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

        binding.btnJoinRoom.setOnClickListener(view -> this.joinRoom(binding.edtRoom.getText().toString(),
                binding.txtNickname.getText().toString()));
        binding.btnScanQR.setOnClickListener(view -> this.qrCodeScan());
        //warning message if nick and room empty
        dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Nickname and Room Code cannot be empty.");
        dialog.setNeutralButton("OK", null);
        //binding.txtNickname.addTextChangedListener(view -> this.setUserNickname(binding.txtNickname.getText().toString()));

        try{
            Intent intent = getIntent();
            //set room codee
            roomCode = (String) intent.getStringExtra("SCANRESULT");
            binding.edtRoom.setText(roomCode);
            //set username if exists
            uname = (String) intent.getStringExtra("USERNICK");
            binding.txtNickname.setText(uname);
        }catch (Exception e){
            Log.println(Log.ERROR, "EXC", e.toString());
        }finally{
            roomCode = "";
            uname = "";
        }

        // QRCode scan settings
        mScannerView = new ZXingScannerView(this);
    }

    private void joinRoom(String room, String playerName){
        try {
            roomCode = room;
            uname = playerName;
            //Log.println(Log.INFO, "JOIN ROOM: ", "joining "+roomCode);

            if (roomCode.equals("") || uname.equals("")) {
                AlertDialog aDialog = dialog.create();
                aDialog.show();
                throw new Exception("No Nickname/user detected");
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("roomCode", roomCode);
            returnIntent.putExtra("playerName", uname);
            setResult(Activity.RESULT_OK, returnIntent);

            this.finish();
        }catch (Exception e){
            Log.println(Log.INFO, "EXCEPTION", e.toString());
        }
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
            joinIntent.putExtra("USERNICK", uname);
            startActivity(joinIntent);
        }else {
            //invalid qrcode result
            Log.println(Log.INFO, "QRCODE:", "Invalid");
            mScannerView.resumeCameraPreview(this);
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}