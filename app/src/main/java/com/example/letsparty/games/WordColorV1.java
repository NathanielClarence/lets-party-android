package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

import com.example.letsparty.R;

import java.util.ArrayList;
import java.util.Random;

public class WordColorV1 extends Game
{
    ArrayList<Integer> colors = new ArrayList<Integer>();
    ArrayList<String> colorNames = new ArrayList<String>();
    Button green, blue, red, black;
    int blackColor = Color.parseColor("#080707");
    int redColor = Color.parseColor("#d91709");
    int greenColor = Color.parseColor("#16a626");
    int blueColor = Color.parseColor("#0a54f5");
    int randomColor;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_color_v1);
        initialize();
        TextView word = findViewById(R.id.word);
        Random r = new Random();
        randomColor = colors.get(r.nextInt(colors.size()));
        String randomColorName = colorNames.get(r.nextInt(colorNames.size()));
        word.setTextColor(randomColor);
        word.setText(randomColorName);

    }

    public void initialize()
    {
        //initialize buttons and set their colors
        Button green = findViewById(R.id.green);
        Button blue = findViewById(R.id.blue);
        Button red = findViewById(R.id.red);
        Button black = findViewById(R.id.black);
        green.setBackgroundColor(greenColor); green.setTag(greenColor);
        blue.setBackgroundColor(blueColor); blue.setTag(blueColor);
        red.setBackgroundColor(redColor); red.setTag(redColor);
        black.setBackgroundColor(blackColor); black.setTag(blackColor);

        //initialize both arraylists with all the color strings and names
        colors.add(greenColor); colors.add(blueColor);
        colors.add(blackColor); colors.add(redColor);

        colorNames.add("Green"); colorNames.add("Blue");
        colorNames.add("Black"); colorNames.add("Red");
    }

    public void onButtonClicked(View v)
    {

        Integer btnColor = (Integer) v.getTag();
        if(btnColor == randomColor)
            System.out.println("************SUCCESS!");
        else
            System.out.println("************FAILED!");

    }
}