package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.letsparty.R;

public abstract class Game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }
}