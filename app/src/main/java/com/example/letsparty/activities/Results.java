package com.example.letsparty.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityResultsBinding;

public class Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        ActivityResultsBinding binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnReturnHome.setOnClickListener(view -> this.returnToHome());
    }

    private void returnToHome(){
        this.finish();
    }
}