package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Edit Profile screen (separate from Student Profile).
// Static sample data only. Full Name is editable in appearance;
// Student ID and school email are read-only.
// btnSaveChanges is visual-only; photo buttons are visual-only.
// No SQLite, no SessionManager, no upload, no validation.
// Exposed IDs for later wiring:
// profileImage, btnChangePhoto, btnRemovePhoto,
// tilFullName, etFullName, tvReadOnlyStudentId, tvReadOnlyEmail,
// btnSaveChanges, btnCancel
public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }
}
