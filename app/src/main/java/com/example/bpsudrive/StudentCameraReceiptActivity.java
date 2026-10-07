package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only static camera wireframe. No CameraX, no CAMERA permission,
// no ACTION_IMAGE_CAPTURE, no Bitmap/URI/file handling, no SQLite.
// btnCaptureReceipt and btnFlash are visual-only for now.
// Exposed IDs for later wiring:
// cameraPreviewContainer, receiptFrame,
// tvCameraInstruction, tvCameraHint,
// btnCaptureReceipt, btnFlash, btn_back, btn_bottom_back
public class StudentCameraReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_camera_receipt);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_bottom_back).setOnClickListener(v -> finish());
    }
}
