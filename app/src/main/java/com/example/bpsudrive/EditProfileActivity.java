package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityEditProfileBinding;

// Edit Profile screen using ViewBinding, loading and saving student name in SQLite with session checks.
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Verify valid student session
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, StudentLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding.btnCancel.setOnClickListener(v -> finish());

        loadUserData();

        binding.btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int emailIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);

                if (nameIdx != -1 && studentIdIdx != -1 && emailIdx != -1) {
                    binding.etFullName.setText(cursor.getString(nameIdx));
                    binding.tvReadOnlyStudentId.setText(cursor.getString(studentIdIdx));
                    binding.tvReadOnlyEmail.setText(cursor.getString(emailIdx));
                }
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }
    }

    private void saveProfile() {
        String newName = binding.etFullName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, newName);

        int rows = -1;
        try {
            rows = db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }

        if (rows > 0) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
