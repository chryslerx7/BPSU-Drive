package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Change Password screen.
// Password fields are visual-only with static "Show" affordances.
// btnChangePassword is visual-only; no verification, hashing,
// validation, or persistence.
// Exposed IDs for later wiring:
// tilCurrentPassword, etCurrentPassword,
// tilNewPassword, etNewPassword,
// tilConfirmPassword, etConfirmPassword,
// btnChangePassword, btnCancel
public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }
}
