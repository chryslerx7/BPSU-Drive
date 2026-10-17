package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentCameraReceiptBinding;

// Camera receipt capture activity using ViewBinding.
public class StudentCameraReceiptActivity extends AppCompatActivity {

    private ActivityStudentCameraReceiptBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentCameraReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBottomBack.setOnClickListener(v -> finish());

        long requestId = getIntent().getLongExtra("request_id", -1);
        String serviceName = getIntent().getStringExtra("service_name");
        double serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);
        String refNo = getIntent().getStringExtra("reference_no");

        // Simulate capturing receipt and proceeding to preview
        binding.btnCaptureReceipt.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentReceiptPreviewActivity.class);
            intent.putExtra("request_id", requestId);
            intent.putExtra("service_name", serviceName);
            intent.putExtra("service_fee", serviceFee);
            intent.putExtra("reference_no", refNo);
            intent.putExtra("image_path", "receipt_capture_" + System.currentTimeMillis() + ".jpg");
            startActivity(intent);
        });
    }
}
