package com.example.letsparty.activities;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsparty.PlayerUtil;
import com.example.letsparty.databinding.ActivityMainBinding;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnSuccessListener;

import com.example.letsparty.entities.Player;

public class MainActivity<mFunctions> extends AppCompatActivity {
    private FirebaseFunctions mFunctions;

    public static final String ROOM = "room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFunctions = FirebaseFunctions.getInstance();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        SharedPreferences prefs = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE);
        final String token = prefs.getString("token", "");

        Log.e("NEW_INACTIVITY_TOKEN", token);
        if (TextUtils.isEmpty(token)) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String newToken = instanceIdResult.getToken();
                    Log.e("newToken", newToken);
                    SharedPreferences.Editor editor = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE).edit();
                    if (token != null) {
                        editor.putString("token", newToken);
                        editor.apply();
                    }

                }
            });
        }

        binding.menuHost.setOnClickListener(view -> this.onHostClicked(token));
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

    private void onHostClicked(String token){
        //contact server and get a new room id
        Log.d("onHostClicked", "Checking");
        Player host = new Player("3456", "Dimitri", token);
        //get server connector
        ServerConnector sc = ServerUtil.getServerConnector();

        //get player uuid
        sc.createRoom(host)
            .addOnSuccessListener(room -> {
                //go to the lobby
                Intent intent = new Intent(this, Lobby.class);
                intent.putExtra(ROOM, room);
                startActivity(intent);
            });
    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
    }

}