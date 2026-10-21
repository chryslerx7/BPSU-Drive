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

import com.example.bpsudrive.databinding.ActivityStudentRegisterBinding;

// Student registration activity with SQLite persistence, duplicate checks, PBKDF2 hashing, real-time password validation, and automatic login.
public class StudentRegisterActivity extends AppCompatActivity {

    private ActivityStudentRegisterBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        // Return to login screen
        binding.btnBackToLogin.setOnClickListener(v -> finish());

        // Wire real-time password requirement validation
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordChecklist();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etPassword.addTextChangedListener(passwordWatcher);
        binding.etConfirmPassword.addTextChangedListener(passwordWatcher);

        // Handle account registration
        binding.btnCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    private void updatePasswordChecklist() {
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        boolean hasMinLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        boolean allValid = hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial;

        if (password.isEmpty()) {
            updateChecklistItem(binding.tvReqLength, "At least 8 characters", false, true);
            updateChecklistItem(binding.tvReqUppercase, "At least 1 uppercase letter (A-Z)", false, true);
            updateChecklistItem(binding.tvReqLowercase, "At least 1 lowercase letter (a-z)", false, true);
            updateChecklistItem(binding.tvReqDigit, "At least 1 number (0-9)", false, true);
            updateChecklistItem(binding.tvReqSpecial, "At least 1 special character (!@#$%^&*)", false, true);
            binding.tilPassword.setBoxStrokeColor(getColor(R.color.stroke_grey));
        } else {
            updateChecklistItem(binding.tvReqLength, "At least 8 characters", hasMinLength, false);
            updateChecklistItem(binding.tvReqUppercase, "At least 1 uppercase letter (A-Z)", hasUpper, false);
            updateChecklistItem(binding.tvReqLowercase, "At least 1 lowercase letter (a-z)", hasLower, false);
            updateChecklistItem(binding.tvReqDigit, "At least 1 number (0-9)", hasDigit, false);
            updateChecklistItem(binding.tvReqSpecial, "At least 1 special character (!@#$%^&*)", hasSpecial, false);

            if (allValid) {
                binding.tilPassword.setBoxStrokeColor(getColor(R.color.falcon_accent_green));
            } else {
                binding.tilPassword.setBoxStrokeColor(getColor(R.color.error_red));
            }
        }

        // Password match check
        if (confirmPassword.isEmpty()) {
            updateChecklistItem(binding.tvReqMatch, "Passwords match", false, true);
            binding.tilConfirmPassword.setBoxStrokeColor(getColor(R.color.stroke_grey));
        } else if (password.equals(confirmPassword)) {
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

    private void handleRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String studentId = binding.etStudentId.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate required fields
        if (fullName.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate 5 password requirements
        if (!DatabaseHelper.isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Check for duplicate student ID or email
        String checkSelection = DatabaseHelper.COLUMN_USER_STUDENT_ID + " = ? OR LOWER(" + DatabaseHelper.COLUMN_USER_EMAIL + ") = ?";
        String[] checkArgs = {studentId, email.toLowerCase()};

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null, checkSelection, checkArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Toast.makeText(this, "Student ID or Email is already registered", Toast.LENGTH_LONG).show();
                return;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Hash password securely using PBKDF2
        String passwordHash = DatabaseHelper.hashPassword(password);
        if (passwordHash == null) {
            Toast.makeText(this, "Error hashing password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert new student user record
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, studentId);
        values.put(DatabaseHelper.COLUMN_USER_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, passwordHash);
        values.put(DatabaseHelper.COLUMN_USER_COURSE, "BSIT");

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            // Handle database insertion error
        } finally {
            db.close();
        }

        if (result != -1) {
            // Automatically log in the newly registered student and navigate to Student Home
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.createLoginSession((int) result, studentId);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
