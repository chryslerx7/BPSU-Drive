package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class StaffLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
