package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only success state shown after request submission.
// Static sample data only. No SQLite, no submission logic,
// no status generation, no notifications.
// Exposed IDs for later wiring:
// ivSuccess, tvSuccessTitle, tvSuccessMessage,
// tvRequestReference, tvServiceName, tvRequestStatus,
// btnViewRequest, btn_back
public class StudentRequestSubmittedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_submitted);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
