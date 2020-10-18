package com.example.letsparty.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;

import com.google.zxing.WriterException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Lobby extends AppCompatActivity {

    private Room room;
    private String playerId;
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

        this.playerId = intent.getStringExtra(MainActivity.PLAYER_ID);
        boolean isHost = room.getHost().getId().equals(this.playerId);
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
        if (!room.getHost().getId().equals(this.playerId)) {
            readyForMatch();
        }
    }

    private void startMatch() {
        ServerConnector sc = ServerUtil.getServerConnector();
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
                startActivity(intent);
            })
            .addOnFailureListener(ex -> {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT);
                binding.startButton.setEnabled(true);
                //binding.readyButton.setEnabled(true);
            });
    }



    private Task<List<String>> waitForMatchStart() {
        TaskCompletionSource tcs = new TaskCompletionSource();

        //the following snippet is UNTESTED code for receiving a message from Firebase when all players are ready
        /*BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //check message that all players are ready
                List<String> gameIds = intent.getStringArrayListExtra("gameIds");
                tcs.setResult(gameIds);
            }
        };
        IntentFilter filter = new IntentFilter("players_ready");
        registerReceiver(br, filter);*/

        //the following code is a stub for testing purposes
        List<String> gameIds = Stream.of("ClearDanger", "Landscape", "MeasureVoice").collect(Collectors.toList());
        new Handler().postDelayed(() -> tcs.setResult(gameIds), 5000);

        return tcs.getTask();
    }

    private void generateQRCode(String RoomCode){
        qrImage = (ImageView) findViewById(R.id.imageView2);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
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