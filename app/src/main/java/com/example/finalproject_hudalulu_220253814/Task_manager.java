package com.example.finalproject_hudalulu_220253814;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class Task_manager extends AppCompatActivity {

    private LinearLayout llTasksContainer;
    private View llEmptyState;
    private TextView tvBannerSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_manager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        llTasksContainer = findViewById(R.id.llTasksContainer);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvBannerSubtitle = findViewById(R.id.tvBannerSubtitle);

        // Settings / Profile
        ImageView ivSettings = findViewById(R.id.ivSettings);
        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, settings.class));
            });
        }

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, Profile.class));
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
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        if (navTimer != null) {
            navTimer.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, Smart_timer.class));
            });
        }

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, Dashboard.class));
            });
        }

        if (navStats != null) {
            navStats.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, Focus_state.class));
            });
        }

        // Add New Task FAB
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(Task_manager.this, New_task.class));
        });

        // Focus History Link
        TextView tvFocusHistory = findViewById(R.id.tvFocusHistory);
        if (tvFocusHistory != null) {
            tvFocusHistory.setOnClickListener(v -> {
                startActivity(new Intent(Task_manager.this, focus_history.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDynamicTasks();
    }

    private void loadDynamicTasks() {
        if (llTasksContainer == null) return;
        llTasksContainer.removeAllViews();
        List<Task> tasks = TaskRepository.getInstance(this).getTasks();

        if (llEmptyState != null) {
            if (tasks.isEmpty()) {
                llEmptyState.setVisibility(View.VISIBLE);
                if (tvBannerSubtitle != null) tvBannerSubtitle.setText("You have no tasks scheduled for today.");
            } else {
                llEmptyState.setVisibility(View.GONE);
                if (tvBannerSubtitle != null) {
                    tvBannerSubtitle.setText("You have " + tasks.size() + " tasks scheduled for today.");
                }
            }
        }

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            View taskView = LayoutInflater.from(this).inflate(R.layout.item_task_dynamic, llTasksContainer, false);

            TextView tvTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvDuration = taskView.findViewById(R.id.tvTaskDuration);
            TextView tvCategory = taskView.findViewById(R.id.tvTaskCategory);
            TextView tvAudioPref = taskView.findViewById(R.id.tvAudioPref);
            TextView tvStatus = taskView.findViewById(R.id.tvTaskStatus);
            ImageView ivCategoryIcon = taskView.findViewById(R.id.ivCategoryIcon);
            ImageView ivAudioIcon = taskView.findViewById(R.id.ivAudioIcon);
            ImageView ivDelete = taskView.findViewById(R.id.ivDelete);
            ImageView ivEdit = taskView.findViewById(R.id.ivEdit);

            tvTitle.setText(task.getTitle());
            tvDuration.setText(task.getDuration() + " mins");
            tvCategory.setText(task.getCategory());
            tvAudioPref.setText(task.getAudioPreference());
            
            // Update Status UI
            if ("COMPLETED".equals(task.getStatus())) {
                tvStatus.setText("COMPLETED");
                tvStatus.setBackgroundResource(R.drawable.badge_bg_green_status);
                tvStatus.setTextColor(Color.parseColor("#1B4332"));
            } else if ("IN PROGRESS".equals(task.getStatus())) {
                tvStatus.setText("IN PROGRESS");
                tvStatus.setBackgroundResource(R.drawable.badge_bg_yellow);
                tvStatus.setTextColor(Color.parseColor("#B8860B"));
            } else {
                tvStatus.setText("PENDING");
                tvStatus.setBackgroundResource(R.drawable.badge_bg_gray);
                tvStatus.setTextColor(getResources().getColor(R.color.text_gray));
            }

            // Set icon based on category
            if ("Study".equalsIgnoreCase(task.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.ic_book);
            } else if ("Code".equalsIgnoreCase(task.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.ic_code);
            } else if ("Design".equalsIgnoreCase(task.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.ic_palette);
            } else {
                ivCategoryIcon.setImageResource(R.drawable.ic_tasks);
            }

            // Set icon based on audio preference
            if ("Lofi Beats".equalsIgnoreCase(task.getAudioPreference())) {
                ivAudioIcon.setImageResource(R.drawable.ic_music);
            } else {
                ivAudioIcon.setImageResource(R.drawable.ic_volume_off);
            }

            final int index = i;
            if (ivDelete != null) {
                ivDelete.setOnClickListener(v -> {
                    showDeleteDialogDynamic(taskView, index);
                });
            }

            if (ivEdit != null) {
                ivEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(Task_manager.this, Edit_session.class);
                    intent.putExtra("TASK_INDEX", index);
                    intent.putExtra("TASK_TITLE", task.getTitle());
                    intent.putExtra("TASK_DURATION", task.getDuration());
                    intent.putExtra("TASK_CATEGORY", task.getCategory());
                    intent.putExtra("TASK_AUDIO", task.getAudioPreference());
                    startActivity(intent);
                });
            }
            
            // Click on task body to start timer
            taskView.setOnClickListener(v -> {
                Intent intent = new Intent(Task_manager.this, Smart_timer.class);
                intent.putExtra("TASK_INDEX", index);
                intent.putExtra("DURATION", task.getDuration());
                intent.putExtra("TASK_TITLE", task.getTitle());
                intent.putExtra("TASK_AUDIO", task.getAudioPreference());
                startActivity(intent);
            });

            llTasksContainer.addView(taskView);
        }
    }

    private void showDeleteDialogDynamic(View taskView, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);

        btnConfirmDelete.setOnClickListener(v -> {
            TaskRepository.getInstance(this).removeTask(this, index);
            loadDynamicTasks();
            dialog.dismiss();
        });

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}