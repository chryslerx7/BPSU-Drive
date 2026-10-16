package com.example.bpsudrive;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Notifications screen with bottom navigation wired up.
public class StudentNotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notifications);
        markSelected(findViewById(R.id.nav_notifications));
        markUnselected(findViewById(R.id.nav_home));

        // Wire bottom navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_requests).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentMyRequestsActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });
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
