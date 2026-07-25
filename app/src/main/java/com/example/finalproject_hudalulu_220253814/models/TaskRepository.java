package com.example.finalproject_hudalulu_220253814.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finalproject_hudalulu_220253814.database.TaskDbHelper;

import java.util.List;

public class TaskRepository {
    private static TaskRepository instance;
    private final Context appContext;
    private final TaskDbHelper dbHelper;

    private TaskRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = new TaskDbHelper(appContext);
    }

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context);
        }
        return instance;
    }

    public static TaskRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TaskRepository must be initialized with context first");
        }
        return instance;
    }

    private long getCurrentUserId() {
        SharedPreferences prefs = appContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("currentUserId", -1);
    }

    public void addTask(Context context, Task task) {
        task.setUserId(getCurrentUserId());
        long newId = dbHelper.insertTask(task);
        task.setId(newId);
    }

    public List<Task> getTasks() {
        return dbHelper.getTasksForUser(getCurrentUserId());
    }

    public List<Task> getTasksByCategory(String category) {
        return dbHelper.getTasksByCategoryForUser(getCurrentUserId(), category);
    }

    public Task getTask(long id) {
        return dbHelper.getTaskById(id);
    }

    public void updateTask(Context context, Task task) {
        dbHelper.updateTask(task);
    }

    public void removeTask(Context context, long id) {
        dbHelper.deleteTask(id);
    }

    // Categories

    public List<String> getCustomCategories() {
        return dbHelper.getAllCategories();
    }

    public void addCategory(Context context, String category) {
        dbHelper.insertCategory(category);
    }
}