package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply saved theme before EdgeToEdge and layout inflation
        SharedPreferences themePrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int themeMode = themePrefs.getInt("themeMode", 2); // default to system
        if (themeMode == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (themeMode == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1.5 seconds delay for a snappier feel
        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, 1500);
    }

    private void checkNavigation() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        
        // Check local guest/sign-up status (Firebase removed to speed up sync)
        boolean isLocalLoggedIn = prefs.contains("fullName");

        if (isFirstRun) {
            // First time -> Onboarding
            startActivity(new Intent(Splash.this, MainActivity.class));
        } else if (isLocalLoggedIn) {
            // Logged in -> Dashboard
            startActivity(new Intent(Splash.this, Dashboard.class));
        } else {
            // Not logged in -> Sign In
            startActivity(new Intent(Splash.this, Sign_in.class));
        }
        finish();
    }
}