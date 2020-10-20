package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.letsparty.R;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Game extends AppCompatActivity {
    public static final String TIME_ELAPSED = "timeElapsed";
    public static final String END_TIME = "endTime";
    public static final String GAME_ID = "gameId";

    //mapping of game id to class
    public static final Map<String, Class<? extends Game>> GAME_IDS =
            Stream.of(
                    new SimpleEntry<>("ClearDanger", ClearDanger.class),
                    new SimpleEntry<>("Landscape", Landscape.class),
                    new SimpleEntry<>("MeasureVoice", MeasureVoice.class),
                    new SimpleEntry<>("ShakePhone", ShakePhone.class)
            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    private long startTime;
    private long endTime;

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
        returnIntent.putExtra(GAME_ID, this.getGameId());
        setResult(RESULT_OK, returnIntent);

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