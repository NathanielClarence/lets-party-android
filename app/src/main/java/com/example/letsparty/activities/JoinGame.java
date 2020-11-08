package com.example.letsparty.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.letsparty.PlayerUtil;
import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityJoinGameBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class JoinGame extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private String roomCode;
    private String uname;
    private String token;
    private ZXingScannerView mScannerView;
    private AlertDialog.Builder dialog;
    private ImageView title;
    private ActivityJoinGameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    50); }

        setContentView(R.layout.activity_join_game);

        // set View and bindings
        binding = ActivityJoinGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnJoinRoom.setOnClickListener(view -> this.joinRoom(binding.edtRoom.getText().toString(),
                binding.txtNickname.getText().toString()));
        binding.btnScanQR.setOnClickListener(view -> this.qrCodeScan());
        //warning message if nick and room empty
        dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Nickname and Room Code cannot be empty.");
        dialog.setNeutralButton("OK", null);
        //binding.txtNickname.addTextChangedListener(view -> this.setUserNickname(binding.txtNickname.getText().toString()));

        Intent intent = getIntent();
        try{
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
            token = (String) intent.getStringExtra(MainActivity.TOKEN);
            Log.e("tkn", this.token);
        }

        // QRCode scan settings
        mScannerView = new ZXingScannerView(this);

        title = (ImageView) this.findViewById(R.id.gameTitle);
        titleAnimate(title);
    }

    private void joinRoom(String roomC, String playerName){
        try {
            roomCode = roomC;
            uname = playerName;
            //Log.println(Log.INFO, "JOIN ROOM: ", "joining "+roomCode);

            if (roomCode.equals("") || uname.equals("")) {
                AlertDialog aDialog = dialog.create();
                aDialog.show();
                throw new Exception("No Nickname/user detected");
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            ServerConnector sc = ServerUtil.getServerConnector(this);
            Player player = new Player(PlayerUtil.getPlayerId(), uname, token);
            sc.joinRoom(roomCode, player)
                    .addOnCompleteListener(task -> binding.progressBar.setVisibility(View.INVISIBLE))
                    .addOnSuccessListener(
                            room -> {
                                Intent lobbyIntent = new Intent(this, Lobby.class);
                                lobbyIntent.putExtra(MainActivity.ROOM, room);
                                lobbyIntent.putExtra(MainActivity.PLAYER, player);
                                lobbyIntent.putExtra(MainActivity.TOKEN, token);
                                lobbyIntent.putExtra("TYPE", "guest");
                                startActivity(lobbyIntent);
                                //Log.d("JOINROOM", "joining...");
                                this.finish();
                            })
                    .addOnFailureListener(exception -> Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show());

            //this.finish();
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
            joinIntent.putExtra(MainActivity.TOKEN, token);
            startActivity(joinIntent);
            this.finish();
        }else {
            //invalid qrcode result
            Log.println(Log.INFO, "QRCODE:", "Invalid");
            mScannerView.resumeCameraPreview(this);
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    public void titleAnimate(ImageView title) {
        Animation animUpDown;
        animUpDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.title_float);
        title.startAnimation(animUpDown);
    }
}