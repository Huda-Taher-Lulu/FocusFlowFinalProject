package com.example.finalproject_hudalulu_220253814;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class focus_history extends AppCompatActivity {

    private LinearLayout llHistoryContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_focus_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        llHistoryContainer = findViewById(R.id.llHistoryContainer);
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        List<Task> allTasks = TaskRepository.getInstance(this).getTasks();
        Map<String, List<Task>> groupedTasks = new HashMap<>();

        for (Task task : allTasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                String cat = task.getCategory() != null ? task.getCategory() : "Others";
                if (!groupedTasks.containsKey(cat)) {
                    groupedTasks.put(cat, new ArrayList<>());
                }
                groupedTasks.get(cat).add(task);
            }
        }

        llHistoryContainer.removeAllViews();

        if (groupedTasks.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No focus sessions completed yet.");
            emptyTv.setGravity(android.view.Gravity.CENTER);
            emptyTv.setPadding(0, 100, 0, 0);
            llHistoryContainer.addView(emptyTv);
            return;
        }

        for (String category : groupedTasks.keySet()) {
            addCategorySection(category, groupedTasks.get(category));
        }
    }

    private void addCategorySection(String category, List<Task> tasks) {
        View headerView = LayoutInflater.from(this).inflate(R.layout.item_history_header, llHistoryContainer, false);
        TextView tvCategoryName = headerView.findViewById(R.id.tvCategoryName);
        TextView tvSessionCount = headerView.findViewById(R.id.tvSessionCount);
        View vCategoryIndicator = headerView.findViewById(R.id.vCategoryIndicator);

        tvCategoryName.setText(category);
        tvSessionCount.setText(tasks.size() + (tasks.size() == 1 ? " session" : " sessions"));

        // Set indicator color based on category
        int colorRes = R.color.primary_green;
        if ("Study".equalsIgnoreCase(category)) colorRes = android.R.color.holo_orange_light;
        else if ("Code".equalsIgnoreCase(category)) colorRes = R.color.primary_green;
        else if ("Design".equalsIgnoreCase(category)) colorRes = android.R.color.holo_purple;
        
        vCategoryIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(colorRes)));

        llHistoryContainer.addView(headerView);

        for (Task task : tasks) {
            View taskView = LayoutInflater.from(this).inflate(R.layout.item_history_card, llHistoryContainer, false);
            TextView tvTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvRelativeTime = taskView.findViewById(R.id.tvRelativeTime);
            TextView tvDuration = taskView.findViewById(R.id.tvDuration);
            TextView tvNotes = taskView.findViewById(R.id.tvNotes);

            tvTitle.setText(task.getTitle());
            tvDuration.setText(task.getDuration() + " mins");
            tvNotes.setText(task.getNotes() != null && !task.getNotes().isEmpty() ? task.getNotes() : "No notes recorded.");
            
            if (task.getCompletionTimestamp() > 0) {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        task.getCompletionTimestamp(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS);
                tvRelativeTime.setText(relativeTime);
            } else {
                tvRelativeTime.setVisibility(View.GONE);
            }

            llHistoryContainer.addView(taskView);
        }
    }
}