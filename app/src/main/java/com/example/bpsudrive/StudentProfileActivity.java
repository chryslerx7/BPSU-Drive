package com.example.bpsudrive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Student Profile screen with static sample data.
// Reuses include_student_bottom_nav.xml; Profile is marked selected
// with UI-only styling (no navigation logic).
// Edit Profile / Change Password / Notification Settings / Logout
// are visual-only for now.
// No SQLite, no SessionManager, no auth logic.
// Exposed IDs for later wiring:
// profileImage, tvProfileName, tvProfileStudent,
// tvProfileStudentId, tvProfileEmail, profileContent,
// btnEditProfile, btnChangePassword, btnNotifSettings, btnLogout,
// nav_home, nav_requests, nav_notifications, nav_profile
public class StudentProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        markSelected(findViewById(R.id.nav_profile));
        markUnselected(findViewById(R.id.nav_home));
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
