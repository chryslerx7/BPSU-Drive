package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentRegisterBinding;

// Student registration activity with SQLite persistence, duplicate checks, PBKDF2 hashing, and automatic login.
public class StudentRegisterActivity extends AppCompatActivity {

    private ActivityStudentRegisterBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        // Return to login screen
        binding.btnBackToLogin.setOnClickListener(v -> finish());

        // Handle account registration
        binding.btnCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String studentId = binding.etStudentId.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate required fields
        if (fullName.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Check for duplicate student ID or email
        String checkSelection = DatabaseHelper.COLUMN_USER_STUDENT_ID + " = ? OR LOWER(" + DatabaseHelper.COLUMN_USER_EMAIL + ") = ?";
        String[] checkArgs = {studentId, email.toLowerCase()};

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null, checkSelection, checkArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Toast.makeText(this, "Student ID or Email is already registered", Toast.LENGTH_LONG).show();
                return;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Hash password securely
        String passwordHash = DatabaseHelper.hashPassword(password);
        if (passwordHash == null) {
            Toast.makeText(this, "Error hashing password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert new student user record
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, studentId);
        values.put(DatabaseHelper.COLUMN_USER_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, passwordHash);
        values.put(DatabaseHelper.COLUMN_USER_COURSE, "BSIT");

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            // Handle database insertion error
        } finally {
            db.close();
        }

        if (result != -1) {
            // Automatically log in the newly registered student and navigate to Student Home
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.createLoginSession((int) result, studentId);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
