package com.example.letsparty.activities;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.example.letsparty.PlayerUtil;
import com.example.letsparty.R;
import com.example.letsparty.exceptions.RoomNotFoundException;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.databinding.ActivityMainBinding;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnSuccessListener;

import com.example.letsparty.entities.Player;


public class MainActivity<mFunctions> extends AppCompatActivity implements NameDialog.NameDialogListener{

    private FirebaseFunctions mFunctions;
    private String token;

    public static final String ROOM = "room";
    public static final String PLAYER = "playerId";

    private String playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFunctions = FirebaseFunctions.getInstance();
        this.playerId = PlayerUtil.getPlayerId();


        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        SharedPreferences prefs = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE);
        token = prefs.getString("token", "");

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
        Log.d("onHostClicked", "Checking");
        NameDialog dialog = new NameDialog();
        dialog.show(getSupportFragmentManager(), "name");
    }

    private void onJoinClicked(){
        Log.d("Test", "Join button clicked");
        //redirect to new activity
        Intent intent = new Intent(this, JoinGame.class);
        startActivityForResult(intent, 0);

        //show a dialog asking for the room number from the user
        //RoomNumberDialog dialog = new RoomNumberDialog();
        //dialog.show(getSupportFragmentManager(), "roomNumber");
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
                if (exception instanceof RoomNotFoundException) {
                    String errorText = getString(R.string.error_room_number) + " " + roomCode;
                    Toast errorToast = Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT);
                    errorToast.show();
                }
            });
    }

    @Override
    public void onNameDialogPositiveClick(DialogFragment dialog, Player player) {
        //contact server and get a new room id
        ServerConnector sc = ServerUtil.getServerConnector(this);
        try {
            sc.createRoom(player)
                    .addOnSuccessListener(room -> {
                        //  roomCreated = room;
                        //  Log.d("ROOM_CREATED", roomCreated.toString());
                        Intent intent = new Intent(this, Lobby.class);
                        intent.putExtra(ROOM, room);
                        intent.putExtra(PLAYER, player);
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