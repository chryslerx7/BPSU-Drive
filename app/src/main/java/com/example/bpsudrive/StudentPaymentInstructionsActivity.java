package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Payment Instructions screen (step 1 of the receipt workflow).
// Static sample data only. No payment processing, no SQLite,
// no receipt/camera logic. btnContinue is visual-only for now;
// Phase J will handle receipt capture.
// Exposed IDs for later wiring:
// tvPaymentTitle, tvPaymentAmount, tvPaymentReference,
// tvPaymentMethod, tvInstructionStep1-4, tvPaymentReminder,
// btnContinue, btn_back
public class StudentPaymentInstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_payment_instructions);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
