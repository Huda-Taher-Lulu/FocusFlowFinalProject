package com.example.finalproject_hudalulu_220253814;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;

public class TaskDetailsActivity extends AppCompatActivity {

    private Task task;
    private TextView tvCategoryChip, tvTaskTitle, tvProgressPercent;
    private ProgressBar taskProgressBar;
    private TextView tvDurationValue, tvRemainingValue, tvAudioValue, tvStatusValue, tvNotesContent;
    private ImageView ivCategoryIconLarge, ivStatusDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        task = (Task) getIntent().getSerializableExtra("TASK");
        if (task == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvCategoryChip = findViewById(R.id.tvCategoryChip);
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        taskProgressBar = findViewById(R.id.taskProgressBar);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        tvRemainingValue = findViewById(R.id.tvRemainingValue);
        tvAudioValue = findViewById(R.id.tvAudioValue);
        tvStatusValue = findViewById(R.id.tvStatusValue);
        tvNotesContent = findViewById(R.id.tvNotesContent);
        ivCategoryIconLarge = findViewById(R.id.ivCategoryIconLarge);
        ivStatusDot = findViewById(R.id.ivStatusDot);
    }

    private void setupListeners() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.ivShare).setOnClickListener(v -> shareTask());

        findViewById(R.id.btnEditNote).setOnClickListener(v -> showEditNoteDialog());

        findViewById(R.id.btnEditTask).setOnClickListener(v -> {
            Intent intent = new Intent(this, Edit_session.class);
            intent.putExtra("TASK", task);
            startActivity(intent);
        });

        findViewById(R.id.btnDeleteTask).setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void updateUI() {
        // Refresh task from DB
        task = TaskRepository.getInstance(this).getTask(task.getId());
        if (task == null) {
            finish();
            return;
        }

        tvTaskTitle.setText(task.getTitle());
        tvCategoryChip.setText(task.getCategory());
        
        String minsLabel = getString(R.string.min_unit);
        tvDurationValue.setText(getString(R.string.minutes_suffix, task.getDuration()).replace("m", " " + minsLabel));

        long timeLeftMins = task.getTimeLeftInMillis() / (60 * 1000);
        tvRemainingValue.setText(timeLeftMins + " " + minsLabel);

        tvAudioValue.setText(task.getAudioPreference());
        
        // Status mapping to strings
        String statusText = task.getStatus();
        if ("COMPLETED".equals(statusText)) statusText = getString(R.string.completed);
        else if ("IN PROGRESS".equals(statusText)) statusText = getString(R.string.in_progress);
        else if ("PENDING".equals(statusText)) statusText = getString(R.string.pending);
        
        tvStatusValue.setText(statusText);
        tvNotesContent.setText(task.getNotes());

        // Progress Calculation (Requirement 7)
        int progress;
        if ("COMPLETED".equals(task.getStatus())) {
            progress = 100;
        } else if ("PENDING".equals(task.getStatus())) {
            progress = 0;
        } else {
            long totalMillis = (long) task.getDuration() * 60 * 1000;
            if (totalMillis > 0) {
                progress = (int) (((totalMillis - task.getTimeLeftInMillis()) * 100) / totalMillis);
            } else {
                progress = 0;
            }
        }
        taskProgressBar.setProgress(progress);
        tvProgressPercent.setText(progress + "%");

        // Status Dot Color
        if ("COMPLETED".equals(task.getStatus())) {
            ivStatusDot.setImageTintList(ColorStateList.valueOf(Color.parseColor("#1B4332")));
        } else if ("IN PROGRESS".equals(task.getStatus())) {
            ivStatusDot.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
        } else {
            ivStatusDot.setImageTintList(ColorStateList.valueOf(Color.parseColor("#6B7280")));
        }

        // Category Icon Mapping
        if ("Study".equalsIgnoreCase(task.getCategory())) {
            ivCategoryIconLarge.setImageResource(R.drawable.ic_book);
        } else if ("Code".equalsIgnoreCase(task.getCategory())) {
            ivCategoryIconLarge.setImageResource(R.drawable.ic_code);
        } else if ("Design".equalsIgnoreCase(task.getCategory())) {
            ivCategoryIconLarge.setImageResource(R.drawable.ic_palette);
        } else {
            ivCategoryIconLarge.setImageResource(R.drawable.ic_star_floating);
        }
    }

    private void shareTask() {
        String content = getString(R.string.share_task_content,
                task.getTitle(), task.getDuration(), task.getCategory(), task.getStatus());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(intent, getString(R.string.share_task_title)));
    }

    private void showEditNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_note, null);
        builder.setView(view);

        EditText etNotes = view.findViewById(R.id.etNotes);
        etNotes.setText(task.getNotes());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        view.findViewById(R.id.btnSaveNote).setOnClickListener(v -> {
            String newNote = etNotes.getText().toString();
            task.setNotes(newNote);
            TaskRepository.getInstance(this).updateTask(this, task);
            tvNotesContent.setText(newNote);
            dialog.dismiss();
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnCancelNote).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_desc)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    TaskRepository.getInstance(this).removeTask(this, task.getId());
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
