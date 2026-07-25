package com.example.finalproject_hudalulu_220253814.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject_hudalulu_220253814.R;
import com.example.finalproject_hudalulu_220253814.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // واجهة للتعامل مع الأحداث في الـ Activity
    public interface OnTaskActionListener {
        void onTaskClick(Task task);
        void onViewDetailsClick(Task task);
        void onEditClick(Task task);
        void onDeleteClick(Task task);
    }

    private List<Task> tasks;
    private final OnTaskActionListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateData(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_dynamic, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // تعبئة بيانات المهمة في الصف
        holder.tvTitle.setText(task.getTitle());
        holder.tvDuration.setText(holder.itemView.getContext().getString(R.string.mins_with_value, task.getDuration(), holder.itemView.getContext().getString(R.string.min_unit)));
        holder.tvCategory.setText(task.getCategory());

        // تغيير الشكل حسب الحالة
        if ("COMPLETED".equals(task.getStatus())) {
            holder.tvStatus.setText(R.string.completed);
            holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_green_status);
            holder.tvStatus.setTextColor(Color.parseColor("#1B4332"));
        } else if ("IN PROGRESS".equals(task.getStatus())) {
            holder.tvStatus.setText(R.string.in_progress);
            holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_yellow);
            holder.tvStatus.setTextColor(Color.parseColor("#B8860B"));
        } else {
            holder.tvStatus.setText(R.string.pending);
            holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_gray);
            holder.tvStatus.setTextColor(holder.itemView.getResources().getColor(R.color.text_gray));
        }

        // تحديد الأيقونة المناسبة للتصنيف
        if ("Study".equalsIgnoreCase(task.getCategory())) {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_book);
        } else if ("Code".equalsIgnoreCase(task.getCategory())) {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_code);
        } else if ("Design".equalsIgnoreCase(task.getCategory())) {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_palette);
        } else {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_tasks);
        }

        // تشغيل المستمعات
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetailsClick(task));
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(task));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDuration, tvCategory, tvStatus, btnViewDetails;
        ImageView ivCategoryIcon, ivDelete, ivEdit;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDuration = itemView.findViewById(R.id.tvTaskDuration);
            tvCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivEdit = itemView.findViewById(R.id.ivEdit);
        }
    }
}
