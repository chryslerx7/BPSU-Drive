package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentReceiptSubmittedBinding;

// Receipt submitted confirmation screen using ViewBinding.
public class StudentReceiptSubmittedActivity extends AppCompatActivity {

    private ActivityStudentReceiptSubmittedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentReceiptSubmittedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finishToHome());
        binding.btnViewRequest.setOnClickListener(v -> finishToHome());

        String refNo = getIntent().getStringExtra("reference_no");
        String serviceName = getIntent().getStringExtra("service_name");
        double serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);

        if (refNo != null) {
            binding.tvRequestReference.setText("Reference: " + refNo + (serviceName != null ? " (" + serviceName + ")" : ""));
        }
        binding.tvReceiptStatus.setText("Receipt Status: Pending Verification");
        binding.tvPaymentAmount.setText("Amount Paid: ₱" + String.format("%.2f", serviceFee));
    }

    private void finishToHome() {
        Intent intent = new Intent(this, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
