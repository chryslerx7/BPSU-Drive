package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentHomeBinding;

// Student Home screen matching New Home UI reference, with ViewBinding and bottom navigation.
public class StudentHomeActivity extends AppCompatActivity {

    private ActivityStudentHomeBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Mark Home tab as selected in bottom navigation
        markSelected(binding.bottomNav.navHome);
        markUnselected(binding.bottomNav.navRequests);
        markUnselected(binding.bottomNav.navNotifications);
        markUnselected(binding.bottomNav.navProfile);

        // Load student greeting name
        loadUserGreeting();

        // Wire bottom navigation clicks
        binding.bottomNav.navHome.setOnClickListener(v -> {
            // Already on home
        });
        binding.bottomNav.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentMyRequestsActivity.class));
            finish();
        });
        binding.bottomNav.navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNotificationsActivity.class));
            finish();
        });
        binding.bottomNav.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });

        // Wire top notification bell
        binding.includeHomeHeader.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, StudentNotificationsActivity.class)));

        // Wire request details and new request buttons
        binding.btnViewDetails.setOnClickListener(v ->
                startActivity(new Intent(this, StudentRequestDetailsActivity.class)));
        binding.btnNewRequest.setOnClickListener(v ->
                startActivity(new Intent(this, StudentCreateRequestActivity.class)));
    }

    private void loadUserGreeting() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            binding.tvGreeting.setText("Good morning, Student");
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                if (nameIdx != -1) {
                    String fullName = cursor.getString(nameIdx);
                    // Extract first name for greeting
                    String firstName = fullName.split(" ")[0];
                    binding.tvGreeting.setText("Good morning, " + firstName);
                } else {
                    binding.tvGreeting.setText("Good morning, Student");
                }
            } else {
                binding.tvGreeting.setText("Good morning, Student");
            }
        } catch (Exception e) {
            binding.tvGreeting.setText("Good morning, Student");
        } finally {
            db.close();
        }
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
