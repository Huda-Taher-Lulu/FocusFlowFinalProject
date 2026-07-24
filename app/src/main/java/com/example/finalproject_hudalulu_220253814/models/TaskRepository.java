package com.example.finalproject_hudalulu_220253814.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static TaskRepository instance;
    private List<Task> taskList;
    private List<String> customCategories;
    private static final String PREF_NAME = "TaskPrefs";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_CATEGORIES = "categories";

    private TaskRepository(Context context) {
        taskList = loadTasks(context);
        customCategories = loadCategories(context);
    }

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Compatibility for older calls without context
    public static TaskRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TaskRepository must be initialized with context first");
        }
        return instance;
    }

    public void addTask(Context context, Task task) {
        taskList.add(task);
        saveTasks(context);
    }

    public List<Task> getTasks() {
        return taskList;
    }

    public void removeTask(Context context, int index) {
        if (index >= 0 && index < taskList.size()) {
            taskList.remove(index);
            saveTasks(context);
        }
    }

    public Task getTask(int index) {
        if (index >= 0 && index < taskList.size()) {
            return taskList.get(index);
        }
        return null;
    }

    public void updateTask(Context context, int index, Task task) {
        if (index >= 0 && index < taskList.size()) {
            taskList.set(index, task);
            saveTasks(context);
        }
    }

    // Category Methods
    public List<String> getCustomCategories() {
        return customCategories;
    }

    public void addCategory(Context context, String category) {
        if (!customCategories.contains(category)) {
            customCategories.add(category);
            saveCategories(context);
        }
    }

    // Persistence Methods
    private void saveTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString(KEY_TASKS, json);
        editor.apply();
    }

    private List<Task> loadTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_TASKS, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(json, type);
        return tasks != null ? tasks : new ArrayList<>();
    }

    private void saveCategories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(customCategories);
        editor.putString(KEY_CATEGORIES, json);
        editor.apply();
    }

    private List<String> loadCategories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_CATEGORIES, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> categories = gson.fromJson(json, type);
        return categories != null ? categories : new ArrayList<>();
    }
}