package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.letsparty.R;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Game extends AppCompatActivity {
    public static final String TIME_ELAPSED = "timeElapsed";
    public static final String END_TIME = "endTime";
    public static final String GAME_ID = "gameId";
    public static final String TAG = "Game";

    //mapping of game id to class
    public static final Map<String, Class<? extends Game>> GAME_IDS =
            Stream.of(
                    new SimpleEntry<>("ClearDanger", ClearDanger.class),
                    new SimpleEntry<>("Landscape", Landscape.class),
                    //new SimpleEntry<>("MeasureVoice", MeasureVoice.class),
                    new SimpleEntry<>("ShakePhone", ShakePhone.class),
                    new SimpleEntry<>("FaceDirection", FaceDirection.class),
                    new SimpleEntry<>("WordColorV1", WordColorV1.class)
            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    private long startTime;
    private long endTime;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game);
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                Log.e(TAG, "Failed due to running out of time");
                gameFinished(false);
            }

        }, 10000);
    }

    protected void gameFinished(boolean success)
    {
        endTime = System.currentTimeMillis();
        timer.cancel();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(TIME_ELAPSED, endTime - startTime);
        returnIntent.putExtra(END_TIME, endTime);
        returnIntent.putExtra(GAME_ID, this.getGameId());
        setResult(RESULT_OK, returnIntent);

        Log.e(TAG, "success? " + success);
        Log.e(TAG, "Time elapsed " + ((endTime - startTime)/1000));
        Log.e(TAG, "Game ID: " + this.getGameId());
        this.finish();
    }


    public String getGameId(){
        for (String id: GAME_IDS.keySet()){
            if (GAME_IDS.get(id) == this.getClass())
                return id;
        }
        return null;
    }
}