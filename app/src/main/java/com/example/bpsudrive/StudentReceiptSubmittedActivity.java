package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Receipt Submitted state. Static sample data only.
// No receipt insertion, no status updates, no upload,
// no SQLite, no networking, no verification logic.
// btnViewRequest is visual-only for now.
// Exposed IDs for later wiring:
// submittedIcon, tvSubmittedTitle, tvSubmittedMessage,
// submittedStatusContainer, tvRequestReference,
// tvReceiptStatus, tvPaymentAmount, btnViewRequest, btn_back
public class StudentReceiptSubmittedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_receipt_submitted);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
