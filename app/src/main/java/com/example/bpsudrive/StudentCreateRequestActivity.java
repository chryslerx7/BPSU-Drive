package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Create Request screen. No DB, no validation, no submission logic.
// Exposed IDs for later wiring:
// tvServiceName, tvServiceFee, tvStudentName, tvStudentId, tvStudentEmail,
// tilPurpose, etDescription, tilAdditional, etAdditional,
// btnCreateRequest, btn_back
public class StudentCreateRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_create_request);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
