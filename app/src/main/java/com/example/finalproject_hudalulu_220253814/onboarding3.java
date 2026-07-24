package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class onboarding3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton btnNext = findViewById(R.id.btnGetStarted);
        btnNext.setOnClickListener(v -> {
            // Mark onboarding as finished
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();

            Intent intent = new Intent(onboarding3.this, SignUp.class);
            startActivity(intent);
        });

    }
}