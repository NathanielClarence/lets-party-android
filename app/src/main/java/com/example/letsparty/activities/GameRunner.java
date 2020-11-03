package com.example.letsparty.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.letsparty.entities.Room;
import com.example.letsparty.games.Game;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.example.letsparty.serverconnector.StubServerConnector;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;

public class GameRunner extends AppCompatActivity {
    private Room room;
    private List<String> gameIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        this.gameIds = intent.getStringArrayListExtra("gameIds");

        //only start the game if the state isn't recreated
        if (savedInstanceState == null)
            runGame(0);
    }

    private void runGame(int i){
        //obtain the game to be played
        Class<? extends Game> gameClass = Game.GAME_IDS.get(this.gameIds.get(i));
        if (gameClass == null){
            throw new RuntimeException("Game with id " +  gameIds.get(i) + " does not exist");
        }

        //start the game
        Intent intent = new Intent(this, gameClass);
        intent.putExtra(MainActivity.ROOM, room);
        startActivityForResult(intent, i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //placeholder for calculating point. Can be changed later
            double points = (2000 - data.getLongExtra(Game.TIME_ELAPSED, 2000)) / 1000.0;

            //send game completion to server
            ServerConnector sc = ServerUtil.getServerConnector(this);
            sc.gameFinish(this.room.getRoomCode(), "1", data.getStringExtra(Game.GAME_ID), points);
        }

        int i = requestCode + 1;
        readyForNextGame(i);
    }

    private void readyForNextGame(int i) {
        waitForNextGame()
            .addOnCompleteListener(task -> {
                if (i < this.gameIds.size()) {
                    //if there are games remaining, go to next game
                    runGame(i);
                } else {
                    //if no games remaining, go to result screen
                    Intent intent = new Intent(this, Results.class);
                    startActivity(intent);
                    finish();
                }
            });
    }

    private Task<Boolean> waitForNextGame(){
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        //the following snippet is UNTESTED code for receiving a message from Firebase when all players are ready
        BroadcastReceiver br = new GameBroadcastReceiver(tcs);
        IntentFilter filter = new IntentFilter("game_ready");
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(br, filter);
        Log.d("broadcast", "next game registered");

        //tcs.setResult(true); //placeholder
        return tcs.getTask();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("gameIds", new ArrayList<>(this.gameIds));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.gameIds = savedInstanceState.getStringArrayList("gameIds");
    }

    private static class GameBroadcastReceiver extends BroadcastReceiver{
        private TaskCompletionSource<Boolean> tcs;

        GameBroadcastReceiver(TaskCompletionSource<Boolean> tcs){
            this.tcs = tcs;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            tcs.setResult(true);
            Log.d("broadcast", "next game received");

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.unregisterReceiver(this);
        }
    }
}
