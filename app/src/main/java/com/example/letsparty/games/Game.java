package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.letsparty.R;

import java.time.Instant;

public abstract class Game extends AppCompatActivity {
    public static final String TIME_ELAPSED = "timeElapsed";
    public static final String END_TIME = "endTime";
    public static final String GAME_ID = "gameId";

    private long startTime;
    private long endTime;
    protected String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game);
        startTime = System.currentTimeMillis();
    }

    protected void gameFinished(){
        endTime = System.currentTimeMillis();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(TIME_ELAPSED, endTime - startTime);
        returnIntent.putExtra(END_TIME, endTime);
        returnIntent.putExtra(GAME_ID, this.gameId);
        setResult(RESULT_OK, returnIntent);

        this.finish();
    }

    public String getGameId(){
        return gameId;
    }
}