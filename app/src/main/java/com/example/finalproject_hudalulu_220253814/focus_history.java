package com.example.finalproject_hudalulu_220253814;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject_hudalulu_220253814.adapters.HistoryAdapter;
import com.example.finalproject_hudalulu_220253814.adapters.HistoryListItem;
import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class focus_history extends AppCompatActivity {

    private RecyclerView rvHistory;

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

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        // بنحمل كل المهام ونفلتر المخلصين منها
        List<Task> allTasks = TaskRepository.getInstance(this).getTasks();

        // تجميع المهام حسب التصنيف
        Map<String, List<Task>> groupedTasks = new LinkedHashMap<>();
        for (Task task : allTasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                String cat = task.getCategory() != null ? task.getCategory() : "Others";
                groupedTasks.computeIfAbsent(cat, k -> new ArrayList<>()).add(task);
            }
        }

        List<HistoryListItem> rows = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            rows.add(new HistoryListItem.Header(entry.getKey(), entry.getValue().size()));
            for (Task task : entry.getValue()) {
                rows.add(new HistoryListItem.TaskRow(task));
            }
        }

        rvHistory.setAdapter(new HistoryAdapter(rows));
    }
}
