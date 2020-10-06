package com.example.letsparty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class GameUtil
{

    private static AppCompatActivity callerActivity;

    private static DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            System.out.println("POSITIVE CLICKED!");
            Intent intent = new Intent(callerActivity, Landscape.class);
            callerActivity.startActivity(intent);

        }
    };


    public static AlertDialog createDialog(AppCompatActivity activity, String title, String message)
    {
        setCallerActivity(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setTitle(title).setCancelable(false).setPositiveButton("Next", listener);
        AlertDialog dialog = builder.create();
        return dialog;

    }

    public static void setCallerActivity(AppCompatActivity callerActivity)
    {
        GameUtil.callerActivity = callerActivity;
    }
}
