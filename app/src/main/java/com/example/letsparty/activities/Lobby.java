package com.example.letsparty.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.StubServerConnector;

public class Lobby extends AppCompatActivity {

    private Room room;

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
        sc.startMatch(room.getRoomCode());
    }
}