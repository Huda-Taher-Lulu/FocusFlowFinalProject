package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {

    private TextView tvDailyFocusProgress, tvDailyStreak;
    private ProgressBar circularProgressBar;
    private ShapeableImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDailyFocusProgress = findViewById(R.id.tvDailyFocusProgress);
        tvDailyStreak = findViewById(R.id.tvDailyStreak);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        ivProfile = findViewById(R.id.ivProfile);

        findViewById(R.id.clProgress).setOnClickListener(v -> showSetGoalDialog());

        refreshDashboardStats();

        // Settings / Profile
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, settings.class));
        });

        findViewById(R.id.ivProfile).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Profile.class));
        });

        // Initialize Resume Button
        Button btnResume = findViewById(R.id.btnResume);
        btnResume.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Smart_timer.class);
            startActivity(intent);
        });

        // Navigation Bar
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navDashboard.setOnClickListener(v -> {
            // Already here
        });

        navTasks.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Task_manager.class));
        });

        navTimer.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Smart_timer.class));
        });

        navStats.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Focus_state.class));
        });
    }

    private void showSetGoalDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_goal, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etHours = dialogView.findViewById(R.id.etHours);
        EditText etMinutes = dialogView.findViewById(R.id.etMinutes);
        android.view.View btnSave = dialogView.findViewById(R.id.btnSaveGoal);
        android.view.View btnCancel = dialogView.findViewById(R.id.btnCancelGoal);

        // Pre-fill with current goal
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String goalStr = prefs.getString("dailyFocusGoal", "3 Hours 00 Minutes");
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*Hours\\s*(\\d+)\\s*Minutes");
            java.util.regex.Matcher matcher = pattern.matcher(goalStr);
            if (matcher.find()) {
                etHours.setText(matcher.group(1));
                etMinutes.setText(matcher.group(2));
            }
        } catch (Exception ignored) {}

        btnSave.setOnClickListener(v -> {
            String h = etHours.getText().toString().trim();
            String m = etMinutes.getText().toString().trim();
            if (h.isEmpty()) h = "0";
            if (m.isEmpty()) m = "0";

            int hours = Integer.parseInt(h);
            int minutes = Integer.parseInt(m);

            if (minutes >= 60) {
                hours += minutes / 60;
                minutes = minutes % 60;
            }

            String newGoal = String.format(java.util.Locale.getDefault(), "%d Hours %02d Minutes", hours, minutes);
            prefs.edit().putString("dailyFocusGoal", newGoal).apply();
            
            refreshDashboardStats();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void refreshDashboardStats() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        // 1. Get Focus Goal from Profile (Format: "H Hours MM Minutes")
        String goalStr = prefs.getString("dailyFocusGoal", "3 Hours 00 Minutes");
        float goalHours = 3.0f; // Default
        
        try {
            // Smart Parsing: Extract Hours and Minutes separately
            int h = 0;
            int m = 0;
            
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*Hours\\s*(\\d+)\\s*Minutes");
            java.util.regex.Matcher matcher = pattern.matcher(goalStr);
            
            if (matcher.find()) {
                h = Integer.parseInt(matcher.group(1));
                m = Integer.parseInt(matcher.group(2));
                goalHours = h + (m / 60.0f);
            } else {
                // Fallback for simple numeric input if any
                String numericPart = goalStr.replaceAll("[^0-9.]", "");
                if (!numericPart.isEmpty()) goalHours = Float.parseFloat(numericPart);
            }
        } catch (Exception e) {
            goalHours = 3.0f;
        }

        // 2. Focus Hours Progress
        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        float currentHours = totalMinutes / 60.0f;
        
        tvDailyFocusProgress.setText(String.format(java.util.Locale.getDefault(), "%.1f / %.1f", currentHours, goalHours));
        
        int progress = 0;
        if (goalHours > 0) {
            progress = (int) ((currentHours / goalHours) * 100);
        }
        circularProgressBar.setProgress(Math.min(progress, 100));

        // 3. Streak
        int streak = prefs.getInt("currentStreak", 0);
        tvDailyStreak.setText(streak + " Days");

        // 4. Profile Image
        String imagePath = prefs.getString("profileImageUri", null);
        if (imagePath != null && ivProfile != null) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivProfile);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardStats();
    }
}