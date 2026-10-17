package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentProfileBinding;

// Student profile screen using ViewBinding, loading user data from SQLite, and handling logout.
public class StudentProfileActivity extends AppCompatActivity {

    private ActivityStudentProfileBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Verify valid student session
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, StudentLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        markSelected(binding.bottomNav.navProfile);
        markUnselected(binding.bottomNav.navHome);

        loadUserProfile();

        // Wire bottom navigation clicks
        binding.bottomNav.navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            finish();
        });
        binding.bottomNav.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentMyRequestsActivity.class));
            finish();
        });
        binding.bottomNav.navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNotificationsActivity.class));
            finish();
        });

        // Wire profile actions
        binding.btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Handle logout action
        binding.btnLogout.setOnClickListener(v -> handleLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int emailIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);

                if (nameIdx != -1 && studentIdIdx != -1 && emailIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    String studentId = cursor.getString(studentIdIdx);
                    String email = cursor.getString(emailIdx);

                    binding.tvProfileName.setText(name);
                    binding.tvProfileStudent.setText(name);
                    binding.tvProfileStudentId.setText(studentId);
                    binding.tvProfileEmail.setText(email);
                }
            }
        } catch (Exception e) {
            // Handle error silently
        } finally {
            db.close();
        }
    }

    private void handleLogout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(this, StudentLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void markSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_nav_selected);
        tab.setTextColor(getColor(R.color.bpsu_maroon));
        tab.setTypeface(null, Typeface.BOLD);
    }

    private void markUnselected(TextView tab) {
        tab.setBackgroundResource(0);
        tab.setTextColor(getColor(R.color.text_secondary));
        tab.setTypeface(null, Typeface.NORMAL);
    }
}
