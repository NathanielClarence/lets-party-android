package com.example.letsparty.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.StubServerConnector;
import com.example.letsparty.databinding.ActivityMainBinding;
import com.example.letsparty.serverconnector.FirebaseServerConnector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.functions.FirebaseFunctions;

public class MainActivity<mFunctions> extends AppCompatActivity {
    private FirebaseFunctions mFunctions;

    public static final String ROOM = "room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFunctions = FirebaseFunctions.getInstance();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.menuHost.setOnClickListener(view -> this.onHostClicked(mFunctions));
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

    private void onHostClicked(FirebaseFunctions mFunctions){
        //contact server and get a new room id
        Log.d("onHostClicked", "Checking");
        ServerConnector sc = new FirebaseServerConnector(mFunctions);

        //how do i get player id...
       Room room = sc.createRoom(mFunctions,"1");

        //go to the lobby
        Intent intent = new Intent(this, Lobby.class);
        intent.putExtra(ROOM, room);
        startActivity(intent);
    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
    }

}