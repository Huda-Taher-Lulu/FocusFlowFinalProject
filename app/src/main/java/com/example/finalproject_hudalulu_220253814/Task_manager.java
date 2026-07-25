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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject_hudalulu_220253814.adapters.TaskAdapter;
import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class Task_manager extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
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

        rvTasks = findViewById(R.id.rvTasks);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvBannerSubtitle = findViewById(R.id.tvBannerSubtitle);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(TaskRepository.getInstance(this).getTasks(), this);
        rvTasks.setAdapter(taskAdapter);

        // Settings / Profile
        ImageView ivSettings = findViewById(R.id.ivSettings);
        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, settings.class)));
        }

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, Profile.class)));

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imagePath = prefs.getString("profileImageUri", null);
            if (imagePath != null) {
                Glide.with(this).load(imagePath).placeholder(R.drawable.ic_person).circleCrop().into(ivProfile);
            }
        }

        // Navigation Bar
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        if (navTimer != null) navTimer.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, Smart_timer.class)));
        if (navDashboard != null) navDashboard.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, Dashboard.class)));
        if (navStats != null) navStats.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, Focus_state.class)));

        // Add New Task FAB
        findViewById(R.id.fabAdd).setOnClickListener(v -> startActivity(new Intent(Task_manager.this, New_task.class)));

        // Focus History Link
        TextView tvFocusHistory = findViewById(R.id.tvFocusHistory);
        if (tvFocusHistory != null) {
            tvFocusHistory.setOnClickListener(v -> startActivity(new Intent(Task_manager.this, focus_history.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void refreshTasks() {
        List<Task> tasks = TaskRepository.getInstance(this).getTasks();
        taskAdapter.updateData(tasks);

        if (llEmptyState != null) {
            if (tasks.isEmpty()) {
                llEmptyState.setVisibility(View.VISIBLE);
                if (tvBannerSubtitle != null) tvBannerSubtitle.setText("You have no tasks scheduled for today.");
            } else {
                llEmptyState.setVisibility(View.GONE);
                if (tvBannerSubtitle != null) tvBannerSubtitle.setText("You have " + tasks.size() + " tasks scheduled for today.");
            }
        }
    }


    @Override
    public void onTaskClick(Task task) {
        // Explicit Intent + full Task object passed as Serializable (Requirement 3.2)
        Intent intent = new Intent(Task_manager.this, Smart_timer.class);
        intent.putExtra("TASK", task);
        intent.putExtra("DURATION", task.getDuration());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Task task) {
        Intent intent = new Intent(Task_manager.this, Edit_session.class);
        intent.putExtra("TASK", task);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);

        btnConfirmDelete.setOnClickListener(v -> {
            TaskRepository.getInstance(this).removeTask(this, task.getId());
            refreshTasks();
            dialog.dismiss();
        });

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}