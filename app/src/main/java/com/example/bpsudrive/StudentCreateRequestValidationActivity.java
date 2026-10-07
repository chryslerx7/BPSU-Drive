package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only validation/error visual state of the Create Request screen.
// No validation logic is implemented here.
// Exposed IDs for later Java wiring:
// tilPurpose, etDescription, tvDescriptionError,
// tilAdditional, etAdditional, tvServiceName, tvServiceFee,
// tvStudentName, tvStudentId, tvStudentEmail,
// btnCreateRequest, btn_back
public class StudentCreateRequestValidationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_create_request_validation);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
