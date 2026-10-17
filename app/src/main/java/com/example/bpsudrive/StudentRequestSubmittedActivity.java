package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentRequestSubmittedBinding;

// Success screen shown after request submission using ViewBinding.
public class StudentRequestSubmittedActivity extends AppCompatActivity {

    private ActivityStudentRequestSubmittedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentRequestSubmittedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finishToHome());
        binding.btnViewRequest.setOnClickListener(v -> finishToHome());

        // Retrieve passed request confirmation details
        String referenceNo = getIntent().getStringExtra("reference_no");
        String serviceName = getIntent().getStringExtra("service_name");
        double serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);

        if (referenceNo != null) {
            binding.tvRequestReference.setText("Reference No: " + referenceNo);
        }
        if (serviceName != null) {
            binding.tvServiceName.setText(serviceName);
        }
        binding.tvRequestStatus.setText("Status: Pending");
    }

    private void finishToHome() {
        Intent intent = new Intent(this, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
