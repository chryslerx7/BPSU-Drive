package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityChangePasswordBinding;

// Change Password screen using ViewBinding, verifying current password, real-time validation for new password, and updating hash in SQLite.
public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Verify valid student session
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, StudentLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding.btnCancel.setOnClickListener(v -> finish());

        // Wire real-time password requirement validation for new password fields only
        TextWatcher newPasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordChecklist();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etNewPassword.addTextChangedListener(newPasswordWatcher);
        binding.etConfirmPassword.addTextChangedListener(newPasswordWatcher);

        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void updatePasswordChecklist() {
        String newPassword = binding.etNewPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        boolean hasMinLength = newPassword.length() >= 8;
        boolean hasUpper = newPassword.matches(".*[A-Z].*");
        boolean hasLower = newPassword.matches(".*[a-z].*");
        boolean hasDigit = newPassword.matches(".*[0-9].*");
        boolean hasSpecial = newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean allValid = hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial;

        if (newPassword.isEmpty()) {
            updateChecklistItem(binding.tvReqLength, "At least 8 characters", false, true);
            updateChecklistItem(binding.tvReqUppercase, "At least 1 uppercase letter (A-Z)", false, true);
            updateChecklistItem(binding.tvReqLowercase, "At least 1 lowercase letter (a-z)", false, true);
            updateChecklistItem(binding.tvReqDigit, "At least 1 number (0-9)", false, true);
            updateChecklistItem(binding.tvReqSpecial, "At least 1 special character (!@#$%^&*)", false, true);
            binding.tilNewPassword.setBoxStrokeColor(getColor(R.color.stroke_grey));
        } else {
            updateChecklistItem(binding.tvReqLength, "At least 8 characters", hasMinLength, false);
            updateChecklistItem(binding.tvReqUppercase, "At least 1 uppercase letter (A-Z)", hasUpper, false);
            updateChecklistItem(binding.tvReqLowercase, "At least 1 lowercase letter (a-z)", hasLower, false);
            updateChecklistItem(binding.tvReqDigit, "At least 1 number (0-9)", hasDigit, false);
            updateChecklistItem(binding.tvReqSpecial, "At least 1 special character (!@#$%^&*)", hasSpecial, false);

            if (allValid) {
                binding.tilNewPassword.setBoxStrokeColor(getColor(R.color.falcon_accent_green));
            } else {
                binding.tilNewPassword.setBoxStrokeColor(getColor(R.color.error_red));
            }
        }

        // Password match check
        if (confirmPassword.isEmpty()) {
            updateChecklistItem(binding.tvReqMatch, "Passwords match", false, true);
            binding.tilConfirmPassword.setBoxStrokeColor(getColor(R.color.stroke_grey));
        } else if (newPassword.equals(confirmPassword)) {
            updateChecklistItem(binding.tvReqMatch, "Passwords match", true, false);
            binding.tilConfirmPassword.setBoxStrokeColor(getColor(R.color.falcon_accent_green));
        } else {
            updateChecklistItem(binding.tvReqMatch, "Passwords match", false, false);
            binding.tilConfirmPassword.setBoxStrokeColor(getColor(R.color.error_red));
        }
    }

    private void updateChecklistItem(TextView tv, String label, boolean isSatisfied, boolean isEmpty) {
        if (isEmpty) {
            tv.setText("• " + label);
            tv.setTextColor(getColor(R.color.text_secondary));
        } else if (isSatisfied) {
            tv.setText("✓ " + label);
            tv.setTextColor(getColor(R.color.falcon_accent_green));
        } else {
            tv.setText("✗ " + label);
            tv.setTextColor(getColor(R.color.error_red));
        }
    }

    private void handleChangePassword() {
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate 5 password requirements for new password
        if (!DatabaseHelper.isPasswordValid(newPassword)) {
            Toast.makeText(this, "New password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String storedHash = null;

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_PASSWORD_HASH},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int hashIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD_HASH);
                if (hashIdx != -1) {
                    storedHash = cursor.getString(hashIdx);
                }
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }

        if (storedHash == null || !DatabaseHelper.verifyPassword(currentPassword, storedHash)) {
            Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash new password securely using PBKDF2
        String newHash = DatabaseHelper.hashPassword(newPassword);
        if (newHash == null) {
            Toast.makeText(this, "Error processing new password", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase writeDb = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, newHash);

        int rows = -1;
        try {
            rows = writeDb.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            // Handle error
        } finally {
            writeDb.close();
        }

        if (rows > 0) {
            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
        }
    }
}
