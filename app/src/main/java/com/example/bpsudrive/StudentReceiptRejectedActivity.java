package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Receipt Rejected state. Static sample rejection reason only.
// No rejection logic, no status updates, no resubmission workflow,
// no SQLite, no camera, no networking.
// btnSubmitNewReceipt is visual-only for now.
// Exposed IDs for later wiring:
// tvRejectedTitle, tvRejectedChip, tvRequestReference,
// rejectionReasonContainer, tvRejectionReason,
// tvRequestStatus, tvReceiptStatus, tvReviewedDate,
// previousReceiptContainer, tvPrevService, tvPrevAmount,
// btnSubmitNewReceipt, btn_back
public class StudentReceiptRejectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_receipt_rejected);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
