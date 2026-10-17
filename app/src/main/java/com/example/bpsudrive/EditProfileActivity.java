package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityEditProfileBinding;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

// Edit Profile screen using ViewBinding, supporting profile photo management, name updates, and session safety.
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
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

        // Register gallery picker for Change Photo
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            String localPath = copyUriToInternalStorage(imageUri);
                            if (localPath != null) {
                                int userId = sessionManager.getUserId();
                                sessionManager.saveProfilePhotoPath(userId, localPath);
                                loadProfilePhoto(localPath);
                                Toast.makeText(this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to save profile photo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        binding.btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            galleryLauncher.launch(Intent.createChooser(intent, "Select Profile Photo"));
        });

        binding.btnRemovePhoto.setOnClickListener(v -> showRemovePhotoConfirmation());

        loadUserData();

        binding.btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int emailIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);

                if (nameIdx != -1 && studentIdIdx != -1 && emailIdx != -1) {
                    binding.etFullName.setText(cursor.getString(nameIdx));
                    binding.tvReadOnlyStudentId.setText(cursor.getString(studentIdIdx));
                    binding.tvReadOnlyEmail.setText(cursor.getString(emailIdx));
                }
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }

        // Load profile photo if present
        String photoPath = sessionManager.getProfilePhotoPath(userId);
        loadProfilePhoto(photoPath);
    }

    private void loadProfilePhoto(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    binding.profileImage.setImageBitmap(bitmap);
                    return;
                }
            }
        }
        binding.profileImage.setImageResource(R.drawable.ic_profile_avatar);
    }

    private String copyUriToInternalStorage(Uri uri) {
        File file = new File(getFilesDir(), "profile_" + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg");
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new java.io.FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void showRemovePhotoConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Are you sure you want to remove your profile photo?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    int userId = sessionManager.getUserId();
                    String currentPath = sessionManager.getProfilePhotoPath(userId);
                    if (currentPath != null) {
                        File file = new File(currentPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    sessionManager.clearProfilePhotoPath(userId);
                    binding.profileImage.setImageResource(R.drawable.ic_profile_avatar);
                    Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveProfile() {
        String newName = binding.etFullName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, newName);

        int rows = -1;
        try {
            rows = db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }

        if (rows > 0) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
