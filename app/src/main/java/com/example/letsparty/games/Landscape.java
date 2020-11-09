package com.example.letsparty.games;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import com.example.letsparty.R;

import java.util.Timer;
import java.util.TimerTask;


public class Landscape extends Game
{
    private int curOrient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape);
        this.curOrient = this.getResources().getConfiguration().orientation;
        String txt = this.curOrient == Configuration.ORIENTATION_PORTRAIT ?
                "Switch to landscape mode!":
                "Switch to portrait mode!";
        TextView txtView = findViewById(R.id.txt);
        txtView.setText(txt);
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView text = (TextView) findViewById(R.id.txt);

        // Checks the orientation of the screen
        int newOrient = newConfig.orientation;
        if ((curOrient == Configuration.ORIENTATION_PORTRAIT && newOrient == Configuration.ORIENTATION_LANDSCAPE)
         || (curOrient == Configuration.ORIENTATION_LANDSCAPE && newOrient == Configuration.ORIENTATION_PORTRAIT))
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