package com.example.letsparty.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityResultsBinding;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Results extends AppCompatActivity {

    private ActivityResultsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        Room room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        List<Player> players = room.getPlayers().stream()
                .sorted(Comparator.comparing(Player::getScore).reversed())
                .collect(Collectors.toList());
        displayPlayerScores(players);
        binding.btnReturnHome.setOnClickListener(view -> this.returnToHome());
    }

    private void displayPlayerScores(List<Player> players) {
        players.stream()
                .map(this::playerToRow)
                .forEach(row -> binding.scoreTable.addView(row));
    }

    private TableRow playerToRow(Player player){
        int color = getResources().getColor(R.color.main_text, getTheme());
        TableRow row = new TableRow(this);

        TextView playerText = new TextView(this);
        playerText.setText(player.getNickname());
        playerText.setTextColor(color);
        playerText.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                4f));
        row.addView(playerText);

        TextView scoreText = new TextView(this);
        scoreText.setText(String.valueOf(player.getScore()));
        scoreText.setTextColor(color);
        scoreText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        scoreText.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f));
        row.addView(scoreText);
        return row;
    }

    private void returnToHome(){
        this.finish();
    }
}