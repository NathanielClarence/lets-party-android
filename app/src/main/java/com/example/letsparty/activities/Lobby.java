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
import android.widget.TextView;
import android.widget.Toast;

import com.example.letsparty.MyFirebaseMessageService;
import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Lobby extends AppCompatActivity {

    private static Room room;
    private Player player;
    private List<String> gameIds;
    private Bitmap bitmap;
    private ImageView qrImage;
    private TextView txtPlayerList;
    private static ActivityLobbyBinding binding;
    private TaskCompletionSource<List<String>> startMatchTcs;
    private ArrayList<String> listOfPlayers = new ArrayList<>();
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());

        Intent intent = getIntent();
        String userType = "else";
        try{
            userType = intent.getStringExtra("TYPE");
        }catch (Exception e){
        }

        if (userType.equals("guest")){
            this.player = new Player("null", intent.getStringExtra("playerName"),
                    intent.getStringExtra(MainActivity.TOKEN));
            this.room = new Room(intent.getStringExtra("roomCode"), this.player);
        }else{
            this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
            this.player = (Player) intent.getSerializableExtra(MainActivity.PLAYER);
        }
        String roomCode = room.getRoomCode();

        binding.textView.setText(roomCode);
        generateQRCode(roomCode);

        boolean isHost = room.getHost().equals(this.player);
        binding.startButton.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        //binding.readyButton.setVisibility(isHost ? View.INVISIBLE : View.VISIBLE);

        //binding.editTextTextPersonName.on
        binding.startButton.setOnClickListener(view -> startMatch());
        //binding.readyButton.setOnClickListener(view -> readyForMatch());

        txtPlayerList = findViewById(R.id.textView2);

        setContentView(binding.getRoot());

        //check player list every 1 second
        MyFirebaseMessageService.addPlayerToList(this.player.getNickname());
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                updatePlayers();
            }

        }, 1000);
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
        readyForMatch();
        sc.startMatch(room.getRoomCode(), player)
            .addOnFailureListener(exception -> {
                if (startMatchTcs != null)
                    startMatchTcs.trySetException(exception);
            });
        timer.cancel();
    }

    //receive data from server
    public void updatePlayers(){
        ArrayList<String> p = MyFirebaseMessageService.getPlayerList();

        //check if player is host
        Player firstP = new Player("none", p.get(0), "none");
        if (!room.getHost().equals(firstP)){
            room.setHost(firstP);
        }

        List<String> roomPlayers = new ArrayList<>();
        for (Player player1 : room.getPlayers()){
            roomPlayers.add(player1.getNickname());
        }

        for (String p1 : p){
            if(!Arrays.asList(roomPlayers).contains(p1)){
                room.addPlayer(new Player(null, p1, null));
            }
        }

        String playerList = "PLAYER LIST\n";
        for (Player player1 : room.getPlayers()){
            playerList = playerList + player1.getNickname()+"\n";
        }
        txtPlayerList.setText(playerList);

        Log.e("LIST", playerList);
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
                startMatchTcs.trySetResult(gameIds);

                lbm.unregisterReceiver(this);
            }
        };
        IntentFilter filter = new IntentFilter("start_match");
        lbm.registerReceiver(br, filter);
        Log.d("broadcast", "start match registered");

        //the following code is a stub for testing purposes
        //List<String> gameIds = Stream.of("ClearDanger", "Landscape", "MeasureVoice").collect(Collectors.toList());
        //new Handler().postDelayed(() -> tcs.setResult(gameIds), 5000);
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