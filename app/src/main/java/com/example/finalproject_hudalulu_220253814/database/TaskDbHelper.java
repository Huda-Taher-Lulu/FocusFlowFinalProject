package com.example.finalproject_hudalulu_220253814.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.finalproject_hudalulu_220253814.models.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper implementation for the FocusFlow domain (Requirement 3.3).
 * Handles the local database that stores Tasks (focus sessions) and custom
 * Categories created by the user. Replaces the previous SharedPreferences/Gson
 * based storage in TaskRepository.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FocusFlow.db";
    private static final int DATABASE_VERSION = 1;

    // ---------- Tasks table ----------
    public static final String TABLE_TASKS = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DURATION = "duration";
    public static final String COL_CATEGORY = "category";
    public static final String COL_AUDIO_PREF = "audio_preference";
    public static final String COL_STATUS = "status";
    public static final String COL_TIME_LEFT_MILLIS = "time_left_millis";
    public static final String COL_NOTES = "notes";
    public static final String COL_COMPLETION_TIMESTAMP = "completion_timestamp";

    // ---------- Categories table ----------
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";

    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT NOT NULL, " +
                    COL_DURATION + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_CATEGORY + " TEXT, " +
                    COL_AUDIO_PREF + " TEXT, " +
                    COL_STATUS + " TEXT DEFAULT 'PENDING', " +
                    COL_TIME_LEFT_MILLIS + " INTEGER DEFAULT 0, " +
                    COL_NOTES + " TEXT, " +
                    COL_COMPLETION_TIMESTAMP + " INTEGER DEFAULT 0" +
                    ");";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CAT_NAME + " TEXT UNIQUE NOT NULL" +
                    ");";

    public TaskDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    // ======================= CRUD: Tasks =======================

    /** CREATE: Inserts a new task and returns its generated id, or -1 on failure. */
    public long insertTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = taskToContentValues(task);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    /** READ: Returns all tasks, most recently added first. */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, null, null,
                null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    /** READ: Returns a single task by its database id, or null if not found. */
    public Task getTaskById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Task task = null;
        if (cursor.moveToFirst()) {
            task = cursorToTask(cursor);
        }
        cursor.close();
        db.close();
        return task;
    }

    /** READ (optional filtering): Returns tasks that belong to a given category. */
    public List<Task> getTasksByCategory(String category) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COL_CATEGORY + "=?",
                new String[]{category}, null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    /** UPDATE: Updates an existing task (matched by id). Returns rows affected. */
    public int updateTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = taskToContentValues(task);
        int rows = db.update(TABLE_TASKS, values, COL_ID + "=?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    /** DELETE: Removes a task by id. Returns rows affected. */
    public int deleteTask(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // ======================= CRUD: Categories =======================

    public void insertCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME, name);
        // insertWithOnConflict + IGNORE avoids crashing on duplicate category names
        db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null,
                null, null, COL_CAT_ID + " ASC");

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    // ======================= Helpers =======================

    private ContentValues taskToContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DURATION, task.getDuration());
        values.put(COL_CATEGORY, task.getCategory());
        values.put(COL_AUDIO_PREF, task.getAudioPreference());
        values.put(COL_STATUS, task.getStatus());
        values.put(COL_TIME_LEFT_MILLIS, task.getTimeLeftInMillis());
        values.put(COL_NOTES, task.getNotes());
        values.put(COL_COMPLETION_TIMESTAMP, task.getCompletionTimestamp());
        return values;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task(
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_AUDIO_PREF))
        );
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        task.setTimeLeftInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME_LEFT_MILLIS)));
        task.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        task.setCompletionTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPLETION_TIMESTAMP)));
        return task;
    }
}
