package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentLoginBinding;

// Student login activity connected to SQLite database and SessionManager using ViewBinding.
public class StudentLoginActivity extends AppCompatActivity {

    private ActivityStudentLoginBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Navigate to registration screen
        binding.btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, StudentRegisterActivity.class)));

        // Handle login authentication
        binding.btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String emailOrId = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validate required fields
        if (emailOrId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/student ID and password", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ? OR " + DatabaseHelper.COLUMN_USER_STUDENT_ID + " = ?";
        String[] selectionArgs = {emailOrId, emailOrId};

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int studentIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int passwordHashIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD_HASH);

                if (idIndex != -1 && studentIdIndex != -1 && passwordHashIndex != -1) {
                    int userId = cursor.getInt(idIndex);
                    String studentId = cursor.getString(studentIdIndex);
                    String storedHash = cursor.getString(passwordHashIndex);

                    // Verify password hash securely using PBKDF2
                    if (DatabaseHelper.verifyPassword(password, storedHash)) {
                        sessionManager.createLoginSession(userId, studentId);
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, StudentHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }

            // Generic failure message for invalid credentials
            Toast.makeText(this, "Invalid student ID/email or password", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred during login", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
}
