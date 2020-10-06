package com.example.letsparty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;


public class Landscape extends AppCompatActivity
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
        System.out.println(("*******************CONFIG HAS CHANGED*****************"));

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            System.out.println(("*******************LANDSCAPE*****************"));
            text.setText("Success!");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            System.out.println(("*******************PORTRAIT*****************"));
        }
    }



}