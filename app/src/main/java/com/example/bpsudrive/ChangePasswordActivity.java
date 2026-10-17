package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityChangePasswordBinding;

// Change Password screen using ViewBinding, verifying current password and updating hash in SQLite.
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
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All password fields are required", Toast.LENGTH_SHORT).show();
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
