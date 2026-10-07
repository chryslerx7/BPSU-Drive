package com.example.bpsudrive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Notifications screen with static sample items.
// Reuses include_student_bottom_nav.xml; Notifications is marked selected
// with UI-only styling (no navigation logic).
// No SQLite, no FCM, no persistence, no networking.
// Exposed IDs for later wiring:
// tvNotificationsTitle, notificationList,
// notificationItem1-4, tvNotificationTitle1-4,
// tvNotificationMessage1-4, tvNotificationTime1-4,
// nav_home, nav_requests, nav_notifications, nav_profile
public class StudentNotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notifications);
        markSelected(findViewById(R.id.nav_notifications));
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
