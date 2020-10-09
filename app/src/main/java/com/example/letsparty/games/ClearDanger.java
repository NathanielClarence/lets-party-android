package com.example.letsparty.games;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

<<<<<<< HEAD:app/src/main/java/com/example/letsparty/ClearDanger.java
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
=======

>>>>>>> 7989dc773172898ad4bc2058e68fb54680aa066e:app/src/main/java/com/example/letsparty/games/ClearDanger.java
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.letsparty.GameUtil;
import com.example.letsparty.R;

public class ClearDanger extends Game
{
    private static final String TAG = "message" ;
    private int dangerNum = 3;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_danger);
        SharedPreferences prefs = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE);
        final String token = prefs.getString("token", "");

        Log.e("NEW_INACTIVITY_TOKEN", token);

        if (TextUtils.isEmpty(token)) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(ClearDanger.this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String newToken = instanceIdResult.getToken();
                    Log.e("newToken", newToken);
                    SharedPreferences.Editor editor = getSharedPreferences("TOKEN_PREF", MODE_PRIVATE).edit();
                    if (token != null) {
                        editor.putString("token", newToken);
                        editor.apply();
                    }

                }
            });
        }
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