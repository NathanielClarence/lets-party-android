package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import com.example.letsparty.R;

import java.util.Timer;
import java.util.TimerTask;


public class Landscape extends Game
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape);
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView text = (TextView) findViewById(R.id.txt);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            text.setText("Switched!");
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {

                public void run()
                {
                    gameFinished(true);
                }

            }, 3000);
        }
    }



}