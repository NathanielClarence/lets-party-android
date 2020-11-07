package com.example.letsparty.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;

import android.os.Handler;
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
import com.example.letsparty.exceptions.RoomCancelledException;
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

    private Room room;
    private Player player;
    private List<String> gameIds;
    private Bitmap bitmap;
    private ImageView qrImage;
    private TextView txtPlayerList;
    private ActivityLobbyBinding binding;
    private TaskCompletionSource<List<String>> startMatchTcs;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());

        Intent intent = getIntent();
        String userType = "else";
        userType = intent.getStringExtra("TYPE");

        if (userType.equals("guest")){
            //temporarily set this player as host
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

        txtPlayerList = binding.textView2;

        setContentView(binding.getRoot());

        //check player list every 1 second
        MyFirebaseMessageService.playerList.clear();
        MyFirebaseMessageService.addPlayerToList(this.player.getNickname());
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                runOnUiThread(() -> {
                    updatePlayers();
                    txtPlayerList.invalidate();
                });
            }

        },1000, 1000);
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
        //if current player is not host, change host in room
        if (!room.getHost().equals(firstP)){
            Log.e("HOSTC", "HOST CHANGED");
            room.setHost(firstP);
        }

        ArrayList<String> roomPlayers = new ArrayList<>();
        for (Player player1 : room.getPlayers()){
            roomPlayers.add(player1.getNickname());
        }
        Log.e("FSF", String.valueOf(roomPlayers.contains("1202")));
        for (String p1 : p){
            if(!roomPlayers.contains(p1)){
                room.addPlayer(new Player(null, p1, null));
            }
        }

        String playerList = "PLAYER LIST"+ System.getProperty("line.separator");
        for (Player player1 : room.getPlayers()){
            playerList = playerList + player1.getNickname()+System.getProperty("line.separator");
        }
        //set player list
        txtPlayerList.setText(playerList);
        //View v = findViewById(R.id.)

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
                this.finish();
            })
            .addOnFailureListener(ex -> {
                if (ex instanceof  RoomCancelledException){
                    Toast.makeText(this, "Room has been cancelled by the host", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.startButton.setEnabled(true);
                    //binding.readyButton.setEnabled(true);
                }
            });
    }
    private Task<List<String>> waitForMatchStart() {
        TaskCompletionSource<List<String>>  tcs= new TaskCompletionSource<>();
        startMatchTcs = new TaskCompletionSource<>();

        //use broadcast receiver to receive messages to start the match
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("cancelled", false)){
                    //if message is about how the room is cancelled, then set the task as failed
                    startMatchTcs.trySetException(new RoomCancelledException());
                } else {
                    //if message is how the match should start, then set task as success, with the list of games to be played
                    List<String> gameIds = intent.getStringArrayListExtra("gameIds");
                    Log.d("broadcast", "start match received");
                    startMatchTcs.trySetResult(gameIds);
                }

                lbm.unregisterReceiver(this);
            }
        };
        IntentFilter filter = new IntentFilter("start_match");
        lbm.registerReceiver(br, filter);
        Log.d("broadcast", "start match registered");

        //the following code is a stub for testing purposes
        //List<String> gameIds = Stream.of("ClearDanger", "Landscape", "MeasureVoice").collect(Collectors.toList());
        //new Handler().postDelayed(() -> tcs.setResult(gameIds), 5000);
        //return tcs.getTask();
        //specify timeout
        new Handler().postDelayed(
                () -> startMatchTcs.trySetException(new RuntimeException("Timeout")),
                60000
        );
        return startMatchTcs.getTask();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String quitMessage = this.player.equals(this.room.getHost()) ?
                "Cancel the match?" :
                "Quit the room?";
        builder.setMessage(quitMessage)
                .setPositiveButton("Quit", (dialog, i) -> this.quitRoom())
                .setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss())
                .show();
    }

    private void quitRoom(){
        ServerConnector sc = ServerUtil.getServerConnector(this);
        sc.quitRoom(this.room.getRoomCode(), this.player)
            .addOnSuccessListener(res -> this.finish())
            .addOnFailureListener(ex -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show());
    }
}