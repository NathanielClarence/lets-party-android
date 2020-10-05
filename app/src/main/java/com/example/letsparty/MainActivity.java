package com.example.letsparty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.letsparty.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String ROOM = "room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.menuHost.setOnClickListener(view -> this.onHostClicked());
        binding.menuJoin.setOnClickListener(view -> this.onJoinClicked());
        
        String channelId = "fcm_default_channel";
        String channelName = "Topic";
        /* notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
        NotificationChannel(
        channelId,
        channelName, NotificationManager.IMPORTANCE_LOW
        ))
         */

    }

    private void onHostClicked(){
        //contact server and get a new room id
        ServerConnector sc = new StubServerConnector();
        Room room = sc.createRoom("1");

        //go to the lobby
        Intent intent = new Intent(this, Lobby.class);
        intent.putExtra(ROOM, room);
        startActivity(intent);
    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
    }

}