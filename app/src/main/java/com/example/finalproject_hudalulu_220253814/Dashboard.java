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

    private TextView tvDailyFocusProgress, tvGreeting, tvRemainingHours, tvAchievement;
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

        // هان بنربط العناصر مع الـ View
        tvDailyFocusProgress = findViewById(R.id.tvDailyFocusProgress);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvRemainingHours = findViewById(R.id.tvRemainingHours);
        tvAchievement = findViewById(R.id.tvAchievement);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        ivProfile = findViewById(R.id.ivProfile);

        findViewById(R.id.clProgress).setOnClickListener(v -> showSetGoalDialog());

        refreshDashboardStats();

        // الإعدادات والبروفايل
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, settings.class));
        });

        findViewById(R.id.ivProfile).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, Profile.class));
        });

        // زر المتابعة إذا في سشن شغالة
        Button btnResume = findViewById(R.id.btnResume);
        SharedPreferences timerPrefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        boolean hasActive = timerPrefs.getBoolean("hasActiveSession", false);
        btnResume.setVisibility(hasActive ? android.view.View.VISIBLE : android.view.View.GONE);

        btnResume.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Smart_timer.class);
            startActivity(intent);
        });

        // شريط التنقل السفلي
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navDashboard.setOnClickListener(v -> {
            // إحنا هان أصلاً
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

        // بنعبي القيم الموجودة حالياً
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
        
        // 1. رسالة الترحيب حسب الوقت
        String fullName = prefs.getString("fullName", "Alex");
        String firstName = fullName.split(" ")[0];
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int greetingResId;
        if (hour < 12) greetingResId = R.string.greeting_morning;
        else if (hour < 17) greetingResId = R.string.greeting_afternoon;
        else greetingResId = R.string.greeting_evening;
        
        tvGreeting.setText(getString(greetingResId, firstName));

        // 2. هدف التركيز اليومي
        String goalStr = prefs.getString("dailyFocusGoal", "3 Hours 00 Minutes");
        float goalHours = 3.0f;
        
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(goalStr);
            int h = 0, m = 0;
            if (matcher.find()) h = Integer.parseInt(matcher.group(1));
            if (matcher.find()) m = Integer.parseInt(matcher.group(1));
            goalHours = h + (m / 60.0f);
        } catch (Exception e) {
            goalHours = 3.0f;
        }

        // 3. التقدم الحالي
        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        float currentHours = totalMinutes / 60.0f;
        
        // بنعرضها بصيغة "5/8" جوا الدائرة
        tvDailyFocusProgress.setText(String.format(java.util.Locale.getDefault(), "%.1f / %.1f", currentHours, goalHours));
        
        int progressPercent = 0;
        if (goalHours > 0) {
            progressPercent = (int) ((currentHours / goalHours) * 100);
        }
        circularProgressBar.setProgress(Math.min(progressPercent, 100));

        // 4. الساعات المتبقية ونسبة الإنجاز
        float remaining = Math.max(0, goalHours - currentHours);
        if (remaining >= 1.0f) {
            tvRemainingHours.setText(getString(R.string.hours_format, remaining));
        } else {
            tvRemainingHours.setText(getString(R.string.minutes_format, (int)(remaining * 60)));
        }
        
        tvAchievement.setText(progressPercent + "%");

        // 5. صورة البروفايل
        String imagePath = prefs.getString("profileImageUri", null);
        if (imagePath != null && ivProfile != null) {
            com.bumptech.glide.Glide.with(this)
                .load(imagePath)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivProfile);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardStats();
        
        // تحديث حالة زر المتابعة
        Button btnResume = findViewById(R.id.btnResume);
        if (btnResume != null) {
            boolean hasActive = getSharedPreferences("TimerPrefs", MODE_PRIVATE).getBoolean("hasActiveSession", false);
            btnResume.setVisibility(hasActive ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
}
