package com.example.letsparty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Login extends AppCompatActivity {

    private ImageView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        title = (ImageView) this.findViewById(R.id.gameTitle);
        titleAnimate(title);
    }

    public void titleAnimate(ImageView title) {
        Animation animUpDown;
        animUpDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.title_float);
        title.startAnimation(animUpDown);
    }
}