package com.example.finalproject_hudalulu_220253814.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.finalproject_hudalulu_220253814.models.User;

/**
 * Separate SQLiteOpenHelper for user accounts (Register/Login), independent from
 * TaskDbHelper. Supports login by either email or username, matching the
 * Register/Login screen requirements (name, email, username, password; login by
 * email/username + password).
 */
public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FocusFlowUsers.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PASSWORD = "password";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_FULL_NAME + " TEXT NOT NULL, " +
                    COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_PHONE + " TEXT, " +
                    COL_ADDRESS + " TEXT, " +
                    COL_PASSWORD + " TEXT NOT NULL" +
                    ");";

    public UserDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /** CREATE: Registers a new user. Returns the new id, or -1 if email/username is taken. */
    public long insertUser(User user) {
        if (emailExists(user.getEmail()) || usernameExists(user.getUsername())) return -1;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = userToContentValues(user);
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    /** READ: Login by either email OR username, plus password. Returns the User, or null. */
    public User getUserByCredentials(String identifier, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                "(" + COL_EMAIL + "=? OR " + COL_USERNAME + "=?) AND " + COL_PASSWORD + "=?",
                new String[]{identifier, identifier, password}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        db.close();
        return user;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        db.close();
        return user;
    }

    public boolean emailExists(String email) {
        return columnValueExists(COL_EMAIL, email);
    }

    public boolean usernameExists(String username) {
        return columnValueExists(COL_USERNAME, username);
    }

    private boolean columnValueExists(String column, String value) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, column + "=?",
                new String[]{value}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /** UPDATE: Persists changes to an existing user's profile (matched by id). */
    public int updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = userToContentValues(user);
        int rows = db.update(TABLE_USERS, values, COL_ID + "=?",
                new String[]{String.valueOf(user.getId())});
        db.close();
        return rows;
    }

    /** DELETE: Removes a user account permanently. */
    public int deleteUser(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    private ContentValues userToContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PHONE, user.getPhone());
        values.put(COL_ADDRESS, user.getAddress());
        values.put(COL_PASSWORD, user.getPassword());
        return values;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User(
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD))
        );
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        return user;
    }
}