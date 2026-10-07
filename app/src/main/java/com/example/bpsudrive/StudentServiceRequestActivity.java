package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Service Request details screen. No DB, no submission logic.
// Exposed IDs for later wiring:
// tvServiceName, tvServiceDesc, tvServiceFee, tvServiceProcessing,
// btnSubmitRequest, btn_back
public class StudentServiceRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_service_request);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
