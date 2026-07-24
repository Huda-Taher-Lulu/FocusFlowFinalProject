package com.example.finalproject_hudalulu_220253814;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Focus_state extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_focus_state);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Settings / Profile
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, settings.class));
        });

        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                startActivity(new Intent(Focus_state.this, Profile.class));
            });

            // Load Profile Image
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imagePath = prefs.getString("profileImageUri", null);
            if (imagePath != null) {
                Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfile);
            }
        }

        // Navigation Bar
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navTasks.setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, Task_manager.class));
        });

        navTimer.setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, Smart_timer.class));
        });

        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, Dashboard.class));
        });

        navStats.setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, Focus_state.class));
        });
    }
}