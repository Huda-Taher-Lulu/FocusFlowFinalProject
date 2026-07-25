package com.example.finalproject_hudalulu_220253814.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject_hudalulu_220253814.R;
import com.example.finalproject_hudalulu_220253814.models.Task;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<HistoryListItem> items;

    public HistoryAdapter(List<HistoryListItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // بنحدد إذا كان هيدر أو بطاقة المهمة
        if (viewType == HistoryListItem.TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_history_header, parent, false));
        } else {
            return new TaskViewHolder(inflater.inflate(R.layout.item_history_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            HistoryListItem.Header header = (HistoryListItem.Header) item;
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.tvCategoryName.setText(header.category);
            h.tvSessionCount.setText(header.sessionCount + (header.sessionCount == 1 ? " session" : " sessions"));

            // تغيير لون المؤشر حسب التصنيف
            int colorRes = R.color.primary_green;
            if ("Study".equalsIgnoreCase(header.category)) colorRes = android.R.color.holo_orange_light;
            else if ("Code".equalsIgnoreCase(header.category)) colorRes = R.color.primary_green;
            else if ("Design".equalsIgnoreCase(header.category)) colorRes = android.R.color.holo_purple;

            h.vCategoryIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(h.itemView.getResources().getColor(colorRes)));
        } else if (holder instanceof TaskViewHolder) {
            Task task = ((HistoryListItem.TaskRow) item).task;
            TaskViewHolder h = (TaskViewHolder) holder;
            h.tvTitle.setText(task.getTitle());
            h.tvDuration.setText(task.getDuration() + " mins");
            h.tvNotes.setText(task.getNotes() != null && !task.getNotes().isEmpty() ? task.getNotes() : "No notes recorded.");

            // حساب الوقت النسبي (مثلاً: منذ ساعة)
            if (task.getCompletionTimestamp() > 0) {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        task.getCompletionTimestamp(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS);
                h.tvRelativeTime.setText(relativeTime);
                h.tvRelativeTime.setVisibility(View.VISIBLE);
            } else {
                h.tvRelativeTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSessionCount;
        View vCategoryIndicator;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSessionCount = itemView.findViewById(R.id.tvSessionCount);
            vCategoryIndicator = itemView.findViewById(R.id.vCategoryIndicator);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRelativeTime, tvDuration, tvNotes;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvNotes = itemView.findViewById(R.id.tvNotes);
        }
    }
}
