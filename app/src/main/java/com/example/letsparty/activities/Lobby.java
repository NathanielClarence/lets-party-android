package com.example.letsparty.activities;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.letsparty.MyFirebaseMessageService;
import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.exceptions.RoomCancelledException;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Lobby extends AppCompatActivity {

    private Room room;
    private Player player;
    private Bitmap bitmap;
    private ImageView qrImage;
    private TextView txtPlayerList;
    private ActivityLobbyBinding binding;
    private TaskCompletionSource<List<String>> startMatchTcs;
    private CancellationTokenSource startMatchCts = new CancellationTokenSource();
    private Timer timer;
    private BroadcastReceiver br;
    private boolean isWaiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());

        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        this.player = (Player) intent.getSerializableExtra(MainActivity.PLAYER);
        String roomCode = room.getRoomCode();

        binding.textView.setText(roomCode);
        generateQRCode(roomCode);

        boolean isHost = room.getHost().equals(this.player);
        binding.textView3.setVisibility(isHost ? View.INVISIBLE : View.VISIBLE);
        binding.startButton.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        binding.startButton.setOnClickListener(view -> startMatch());

        txtPlayerList = binding.textView2;

        setContentView(binding.getRoot());

    }

    @Override
    protected void onStart() {
        super.onStart();
        //if not the host, the go straight into waiting
        if (!room.getHost().equals(this.player)) {
            readyForMatch();
        }

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
        List<String> updatedPlayers = MyFirebaseMessageService.getPlayerList();
        List<String> roomPlayers = room.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());

        //add new players
        for (String p1 : updatedPlayers){
            if(!roomPlayers.contains(p1)){
                room.addPlayer(new Player(null, p1, null));
            }
        }
        //remove players who left
        roomPlayers.stream()
                .filter(p -> !updatedPlayers.contains(p))
                .forEach(p -> room.removePlayerByNickname(p));

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
        binding.progressBar.setVisibility(View.VISIBLE);
        isWaiting = true;
        waitForMatchStart()
            .addOnCompleteListener(task -> {
                isWaiting = false;
                binding.progressBar.setVisibility(View.INVISIBLE);
            })
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
                    if (ex.getMessage()!= null && !ex.getMessage().isEmpty()) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    binding.startButton.setEnabled(true);
                }
            });
    }
    private Task<List<String>> waitForMatchStart() {
        startMatchTcs = new TaskCompletionSource<>(startMatchCts.getToken());

        //use broadcast receiver to receive messages to start the match
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        br = new BroadcastReceiver() {
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

        //specify timeout after 1 minute
        //only for host because they're the one who needs to start the game
        //other players can wait indefinitely
        if (player.equals(room.getHost())) {
            new Handler().postDelayed(
                    () -> startMatchTcs.trySetException(new RuntimeException("Timeout")),
                    60000
            );
        }
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
            .addOnSuccessListener(res ->{
                this.cleanup();
                this.finish();
            })
            .addOnFailureListener(ex -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("waiting", isWaiting);
        if (isWaiting) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
            startMatchCts.cancel();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.isWaiting = savedInstanceState.getBoolean("waiting");
        if (isWaiting)
            this.readyForMatch();
    }

    private void cleanup(){
        timer.cancel();
        if (br != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        startMatchCts.cancel();
    }

    @Override
    protected void onStop() {
        cleanup();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cleanup();
        super.onDestroy();
    }
}