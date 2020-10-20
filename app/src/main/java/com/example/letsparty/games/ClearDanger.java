package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.messaging.FirebaseMessaging;
import com.example.letsparty.GameUtil;
import com.example.letsparty.R;

public class ClearDanger extends Game
{
    private int dangerNum = 3;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_danger);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    public void onButtonClicked(View v)
    {
        Button btn = (Button) v;
        System.out.println("Button clicked " + btn.getTag() );
        String tag = (String) btn.getTag();

        if("danger".equals(tag))
        {
            btn.setEnabled(false);
            btn.setBackgroundColor(Color.TRANSPARENT);
            dangerNum--;
        }
        else
        {
            System.out.println("FAILED!");
            /*builder = new AlertDialog.Builder(this);
            builder.setMessage("You Lost!").setTitle("Success");
            AlertDialog dialog = builder.create();
            dialog.show();*/
        }

        if(dangerNum <= 0)
        {
            System.out.println("SUCCESS!");
            gameFinished();
            //AlertDialog dialog = GameUtil.createDialog(this, "Success!", "Click next when you're ready");
            //dialog.show();

        }

    }
}