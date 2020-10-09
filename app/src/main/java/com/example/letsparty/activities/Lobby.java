package com.example.letsparty.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Room;
import com.example.letsparty.games.ClearDanger;
import com.example.letsparty.games.Game;
import com.example.letsparty.games.Landscape;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.StubServerConnector;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Lobby extends AppCompatActivity {

    private Room room;
    private List<Class<? extends Game>> games;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLobbyBinding binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        binding.textView.setText(room.getRoomCode());
        //binding.editTextTextPersonName.on
        binding.button.setOnClickListener(view -> startMatch());
    }

    private void startMatch() {
        ServerConnector sc = new StubServerConnector();
        //sc.startMatch(room.getRoomCode());
        CompletableFuture
                //tell server the match has started and obtain list of games from server
                .supplyAsync(() -> sc.startMatch(room.getRoomCode()))
                //set the games and start the first one
                .thenAccept(games -> {
                    this.games = games;
                    runGame(0);
                });
        
    }

    private void runGame(int i){
        //obtain the game to be played
        Class<? extends Game> gameClass = this.games.get(i);

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
            ServerConnector sc = new StubServerConnector();
            sc.gameFinish(this.room.getRoomCode(), "1", data.getStringExtra(Game.GAME_ID), points);
        }

        int i = requestCode + 1;
        if (i < this.games.size()) {
            //if there are games remaining, go to next game
            runGame(requestCode + 1);
        } else {
            //go to result screen
            Intent intent = new Intent(this, Results.class);
            startActivity(intent);
            //finish();
        }

    }

}