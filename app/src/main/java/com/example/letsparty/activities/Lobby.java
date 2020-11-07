package com.example.letsparty.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;


import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Lobby extends AppCompatActivity {

    private Room room;
    private Player player;
    private List<String> gameIds;
    private Bitmap bitmap;
    private ImageView qrImage;
    private ActivityLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());

        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        String roomCode = room.getRoomCode();

        binding.textView.setText(roomCode);
        generateQRCode(roomCode);

        this.player = (Player) intent.getSerializableExtra(MainActivity.PLAYER);
        boolean isHost = room.getHost().equals(this.player);
        binding.startButton.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        //binding.readyButton.setVisibility(isHost ? View.INVISIBLE : View.VISIBLE);

        //binding.editTextTextPersonName.on
        binding.startButton.setOnClickListener(view -> startMatch());
        //binding.readyButton.setOnClickListener(view -> readyForMatch());

        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!room.getHost().equals(this.player)) {
            readyForMatch();
        }
    }

    private void startMatch() {
        ServerConnector sc = ServerUtil.getServerConnector(this);
        //tell server the match has started and obtain list of games from server
        sc.startMatch(room.getRoomCode());
        readyForMatch();
    }

    private void readyForMatch() {
        binding.startButton.setEnabled(false);
        //binding.readyButton.setEnabled(false);
        waitForMatchStart()
            .addOnSuccessListener(gameIds -> {
                Intent intent = new Intent(this, GameRunner.class);
                intent.putStringArrayListExtra("gameIds",new ArrayList<>(gameIds));
                intent.putExtra(MainActivity.ROOM, this.room);
                intent.putExtra(MainActivity.PLAYER, this.player);
                startActivity(intent);
            })
            .addOnFailureListener(ex -> {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                binding.startButton.setEnabled(true);
                //binding.readyButton.setEnabled(true);
            });
    }


    private Task<List<String>> waitForMatchStart() {
        TaskCompletionSource<List<String>>  tcs= new TaskCompletionSource<>();

        //use broadcast receiver to receive messages to start the match
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //check message that all players are ready
                List<String> gameIds = intent.getStringArrayListExtra("gameIds");
                Log.d("broadcast", "start match received");
                tcs.setResult(gameIds);

                lbm.unregisterReceiver(this);
            }
        };
        IntentFilter filter = new IntentFilter("players_ready");
        lbm.registerReceiver(br, filter);
        Log.d("broadcast", "start match registered");

        //the following code is a stub for testing purposes
        //List<String> gameIds = Stream.of("ClearDanger", "Landscape", "MeasureVoice").collect(Collectors.toList());
        //new Handler().postDelayed(() -> tcs.setResult(gameIds), 5000)

        return tcs.getTask();
    }

    private void generateQRCode(String RoomCode){
        qrImage = binding.imageView2;
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = Math.min(width, height);
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder("letsparty::"+RoomCode, null, QRGContents.Type.TEXT, smallerDimension);

        try{
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        }catch (WriterException e) {
            Log.v("QR ERROR: ", e.toString());
        }
    }
}