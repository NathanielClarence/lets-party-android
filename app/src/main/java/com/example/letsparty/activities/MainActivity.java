package com.example.letsparty.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.letsparty.PlayerUtil;
import com.example.letsparty.databinding.ActivityMainBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements NameDialog.NameDialogListener{

    private String token;

    public static final String ROOM = "room";
    public static final String PLAYER = "playerId";

    private String playerId;
    private ImageView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.playerId = PlayerUtil.getPlayerId();

        if (!this.checkPermission()){
            requestPermission();
            Log.e("PRM", "granted");
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        SharedPreferences prefs = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE);
        token = prefs.getString("token", "");

        Log.e("NEW_INACTIVITY_TOKEN", token);
        if (TextUtils.isEmpty(token)) {
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(MainActivity.this, instanceIdResult -> {
                    String newToken = instanceIdResult.getToken();
                    Log.e("newToken", newToken);
                    SharedPreferences.Editor editor = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE).edit();
                    if (newToken != null) {
                        editor.putString("token", newToken);
                        editor.apply();
                    }

                });
        }

        binding.menuHost.setOnClickListener(view -> this.onHostClicked());
        binding.menuJoin.setOnClickListener(view -> this.onJoinClicked());

        title = (ImageView) this.findViewById(R.id.gameTitle);
        titleAnimate(title);
    }

    private void onHostClicked(){
        Log.d("onHostClicked", "Checking");
        NameDialog dialog = new NameDialog();
        dialog.show(getSupportFragmentManager(), "name");
    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
        //redirect to activity to ask room number and name
        Intent intent = new Intent(this, JoinGame.class);
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            String nickname = data.getStringExtra("playerName");
            Player player = new Player(this.playerId, nickname, token);

            String roomCode = data.getStringExtra("roomCode");
            joinRoom(roomCode, player);
        }
    }


    public void joinRoom(String roomCode, Player player) {
        //contact server and get a new room id
        ServerConnector sc = ServerUtil.getServerConnector(this);

        sc.joinRoom(roomCode, player)
            .addOnSuccessListener(room -> {
                //go to the lobby
                Intent intent = new Intent(this, Lobby.class);
                intent.putExtra(ROOM, room);
                intent.putExtra(PLAYER, player);
                startActivity(intent);
            })
            .addOnFailureListener(exception -> {
                //if room not found, display an error message
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    public void requestPermission(){
        System.out.println("inside requestPermission()");
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, 1);
    }

    public boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onNameDialogPositiveClick(DialogFragment dialog, Player player) {
        //contact server and get a new room id
        ServerConnector sc = ServerUtil.getServerConnector(this);
        sc.createRoom(player)
            .addOnSuccessListener(room -> {
                //  roomCreated = room;
                //  Log.d("ROOM_CREATED", roomCreated.toString());
                Intent intent = new Intent(this, Lobby.class);
                intent.putExtra(ROOM, room);
                intent.putExtra(PLAYER, player);
                startActivity(intent);
            })
            .addOnFailureListener(exception -> Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onNameDialogNegativeClick(DialogFragment dialog) {

    }

    public void titleAnimate(ImageView title) {
        Animation animUpDown;
        animUpDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.title_float);
        title.startAnimation(animUpDown);
    }
}