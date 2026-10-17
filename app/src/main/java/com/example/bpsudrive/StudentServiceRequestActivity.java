package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentServiceRequestBinding;

// Service Request details screen displaying dynamic service catalog data using ViewBinding.
public class StudentServiceRequestActivity extends AppCompatActivity {

    private ActivityStudentServiceRequestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentServiceRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Retrieve service details passed via Intent
        long serviceId = getIntent().getLongExtra("service_id", -1);
        String serviceName = getIntent().getStringExtra("service_name");
        String serviceDesc = getIntent().getStringExtra("service_desc");
        double serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);

        if (serviceName != null) {
            binding.tvServiceName.setText(serviceName);
        }
        if (serviceDesc != null) {
            binding.tvServiceDesc.setText(serviceDesc);
        }
        binding.tvServiceFee.setText("Service Fee: ₱" + String.format("%.2f", serviceFee));

        // Proceed to Create Request form activity
        binding.btnSubmitRequest.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentCreateRequestActivity.class);
            intent.putExtra("service_id", serviceId);
            intent.putExtra("service_name", serviceName);
            intent.putExtra("service_fee", serviceFee);
            startActivity(intent);
        });
    }
}
