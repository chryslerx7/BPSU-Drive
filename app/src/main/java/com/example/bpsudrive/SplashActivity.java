package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivitySplashBinding;

// Splash screen with session check: routes logged-in students to home and guests to login.
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Delay 2 seconds before checking session state and navigating
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, StudentHomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, StudentLoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
