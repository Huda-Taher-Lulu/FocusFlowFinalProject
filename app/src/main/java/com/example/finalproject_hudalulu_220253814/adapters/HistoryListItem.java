package com.example.finalproject_hudalulu_220253814.adapters;

import com.example.finalproject_hudalulu_220253814.models.Task;

public abstract class HistoryListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;

    public abstract int getType();

    public static class Header extends HistoryListItem {
        public final String category;
        public final int sessionCount;

        public Header(String category, int sessionCount) {
            this.category = category;
            this.sessionCount = sessionCount;
        }

        @Override
        public int getType() { return TYPE_HEADER; }
    }

    public static class TaskRow extends HistoryListItem {
        public final Task task;

        public TaskRow(Task task) {
            this.task = task;
        }

        @Override
        public int getType() { return TYPE_TASK; }
    }
}