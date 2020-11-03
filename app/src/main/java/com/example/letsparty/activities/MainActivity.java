package com.example.letsparty.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

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

public class MainActivity<mFunctions> extends AppCompatActivity implements NameDialog.NameDialogListener {
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

    private void onHostClicked( String token){
        //contact server and get a new room id
        Log.d("onHostClicked", "Checking");

        //get server connector

        NameDialog dialog = new NameDialog();
        dialog.show(getSupportFragmentManager(), "name");



    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
        //redirect to new activity
        Intent intent = new Intent(this, JoinGame.class);
        startActivity(intent);
    }

   /* @Override
    public void onRoomNumberEntered(String roomNumber) {
        //contact server and get a new room id
        ServerConnector sc = new StubServerConnector();
        Room room;
        try {
            room = sc.joinRoom(roomNumber, this.playerId);
        }catch (RoomNotFoundException e){
            //if room not found, display an error message
            String errorText = getString(R.string.error_room_number) + roomNumber;
            Toast errorToast = Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT);
            errorToast.show();
            return;
        }

        //go to the lobby
        Intent intent = new Intent(this, Lobby.class);
        intent.putExtra(ROOM, room);
        intent.putExtra(PLAYER_ID, playerId);
        startActivity(intent);
    } */

    @Override
    public void onNameDialogPositiveClick(DialogFragment dialog, Player player) {
        //contact server and get a new room id
        ServerConnector sc = ServerUtil.getServerConnector();

        try {
            sc.createRoom(player)
                    .addOnSuccessListener(room -> {
                        //  roomCreated = room;
                        //  Log.d("ROOM_CREATED", roomCreated.toString());
                        Intent intent = new Intent(this, Lobby.class);
                        intent.putExtra(ROOM, room);
                        //  intent.putExtra(PLAYER_ID, playerId);
                        startActivity(intent);
                    });
        }catch (Exception e){
            //if room not found, display an error message
           /* String errorText = getString(R.string.error_room_number) + name;
            Toast errorToast = Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT);
            errorToast.show(); */
            return;
        }
    }

    @Override
    public void onNameDialogNegativeClick(DialogFragment dialog) {

    }
}