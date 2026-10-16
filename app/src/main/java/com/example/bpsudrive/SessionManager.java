package com.example.bpsudrive;

import android.content.Context;
import android.content.SharedPreferences;

// Lightweight session manager using SharedPreferences for managing local student login state.
// Kept separate from DatabaseHelper and not yet wired into login activities.
public class SessionManager {

    private static final String PREF_NAME = "BpsuDrivePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save user session upon successful login
    public void createLoginSession(int userId, String studentId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_STUDENT_ID, studentId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get logged-in user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Get logged-in student ID
    public String getStudentId() {
        return prefs.getString(KEY_STUDENT_ID, null);
    }

    // Clear session on logout
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
