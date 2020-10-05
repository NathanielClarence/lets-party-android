package com.example.letsparty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.letsparty.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.menuHost.setOnClickListener(view -> Log.d("Test", "Host Button clicked"));
        binding.menuJoin.setOnClickListener(view -> Log.d("Test", "Join Button clicked"));
        
        String channelId = "fcm_default_channel";
        String channelName = "Topic";
        /* notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
        NotificationChannel(
        channelId,
        channelName, NotificationManager.IMPORTANCE_LOW
        ))
         */
    }

}