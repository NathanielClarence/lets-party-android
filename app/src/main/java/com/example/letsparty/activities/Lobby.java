package com.example.letsparty.activities;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Lobby extends AppCompatActivity {

    private Room room;
    private List<String> gameIds;

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
                //start the game runner activity
                .thenAccept(gameIds -> {
                    Intent intent = new Intent(this, GameRunner.class);
                    intent.putStringArrayListExtra("gameIds",new ArrayList<>(gameIds));
                    intent.putExtra(MainActivity.ROOM, this.room);
                    startActivity(intent);
                });

    }


}