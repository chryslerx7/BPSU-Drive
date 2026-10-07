package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Receipt Preview screen. The receipt is a static wireframe
// placeholder built from TextViews. No Bitmap, URI, file, camera,
// SQLite, upload, or submission logic.
// btnSubmitReceipt is visual-only; btnRetakeReceipt closes the screen.
// Exposed IDs for later wiring:
// tvReceiptPreviewTitle, tvReceiptReference, receiptPreviewContainer,
// tvReceiptStudent, tvReceiptService, tvReceiptAmount, tvReceiptDate,
// tvReceiptInstruction, btnSubmitReceipt, btnRetakeReceipt, btn_back
public class StudentReceiptPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_receipt_preview);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btnRetakeReceipt).setOnClickListener(v -> finish());
    }
}
