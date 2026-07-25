package com.example.finalproject_hudalulu_220253814;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;

import java.util.List;
import java.util.Locale;

public class Focus_state extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView tvTotalFocusHours, tvStudyHours, tvEfficiency, tvSessionsCount, tvLongestSession;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_focus_state);
        
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadStats();
        setupNavigation();
    }

    private void initViews() {
        tvTotalFocusHours = findViewById(R.id.tvTotalFocusHours);
        tvStudyHours = findViewById(R.id.tvStudyHours);
        tvEfficiency = findViewById(R.id.tvEfficiency);
        tvSessionsCount = findViewById(R.id.tvSessionsCount);
        tvLongestSession = findViewById(R.id.tvLongestSession);

        // Settings / Profile
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Focus_state.this, settings.class));
        });

        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                startActivity(new Intent(Focus_state.this, Profile.class));
            });

            String imagePath = prefs.getString("profileImageUri", null);
            if (imagePath != null) {
                Glide.with(this)
                        .load(imagePath)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivProfile);
            }
        }
    }

    private void loadStats() {
        long totalFocusMinutes = prefs.getLong("totalFocusMinutes", 0);
        double totalHours = totalFocusMinutes / 60.0;
        tvTotalFocusHours.setText(String.format(Locale.getDefault(), "%.1fh", totalHours));

        int sessionsCount = prefs.getInt("completedTasksCount", 0);
        tvSessionsCount.setText(String.valueOf(sessionsCount));

        // Calculate breakdown from repository
        TaskRepository repository = TaskRepository.getInstance(this);
        List<Task> allTasks = repository.getTasks();
        
        long studyMinutes = 0;
        long maxSessionMinutes = 0;
        int completedCount = 0;

        for (Task task : allTasks) {
            if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
                completedCount++;
                long duration = task.getDuration(); // assuming duration is total planned
                // But we want actual spent? Let's use duration for now.
                if ("Study".equalsIgnoreCase(task.getCategory())) {
                    studyMinutes += duration;
                }
                if (duration > maxSessionMinutes) {
                    maxSessionMinutes = duration;
                }
            }
        }

        tvStudyHours.setText(String.format(Locale.getDefault(), "%.1fh", studyMinutes / 60.0));
        tvLongestSession.setText(String.valueOf(maxSessionMinutes));
        
        // Efficiency could be (completed / total) * 100
        if (allTasks.size() > 0) {
            int efficiency = (completedCount * 100) / allTasks.size();
            tvEfficiency.setText(efficiency + "%");
        } else {
            tvEfficiency.setText("0%");
        }
    }

    private void setupNavigation() {
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navTasks.setOnClickListener(v -> startActivity(new Intent(Focus_state.this, Task_manager.class)));
        navTimer.setOnClickListener(v -> startActivity(new Intent(Focus_state.this, Smart_timer.class)));
        navDashboard.setOnClickListener(v -> startActivity(new Intent(Focus_state.this, Dashboard.class)));
        navStats.setOnClickListener(v -> {}); // Already here
    }
}
