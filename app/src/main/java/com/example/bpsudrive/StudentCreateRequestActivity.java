package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentCreateRequestBinding;

// Create Request screen with SQLite insertion, session user ID, and duplicate tap prevention using ViewBinding.
public class StudentCreateRequestActivity extends AppCompatActivity {

    private ActivityStudentCreateRequestBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private long serviceId;
    private String serviceName;
    private double serviceFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentCreateRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Get intent extras from service detail screen
        serviceId = getIntent().getLongExtra("service_id", -1);
        serviceName = getIntent().getStringExtra("service_name");
        serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);

        if (serviceName != null) {
            binding.tvServiceName.setText(serviceName);
        }
        binding.tvServiceFee.setText("Service fee: ₱" + String.format("%.2f", serviceFee));

        // Load current student user info into form
        loadStudentInfo();

        binding.btnCreateRequest.setOnClickListener(v -> handleRequestSubmission());
    }

    private void loadStudentInfo() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int emailIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);

                if (nameIdx != -1 && studentIdIdx != -1 && emailIdx != -1) {
                    binding.tvStudentName.setText(cursor.getString(nameIdx));
                    binding.tvStudentId.setText("Student ID: " + cursor.getString(studentIdIdx));
                    binding.tvStudentEmail.setText(cursor.getString(emailIdx));
                }
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }
    }

    private void handleRequestSubmission() {
        String purpose = binding.etDescription.getText().toString().trim();
        String additionalInfo = binding.etAdditional.getText().toString().trim();

        // Validate required fields
        if (purpose.isEmpty()) {
            Toast.makeText(this, "Please enter the purpose of your request", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (serviceId == -1) {
            Toast.makeText(this, "Invalid service selection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent duplicate submissions by disabling button temporarily
        binding.btnCreateRequest.setEnabled(false);
        binding.btnCreateRequest.setText("Submitting...");

        // Generate unique reference number
        String referenceNo = "BPSU-" + System.currentTimeMillis();

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REQUEST_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_REQUEST_SERVICE_ID, serviceId);
        values.put(DatabaseHelper.COLUMN_REQUEST_REF_NO, referenceNo);
        values.put(DatabaseHelper.COLUMN_REQUEST_PURPOSE, purpose);
        values.put(DatabaseHelper.COLUMN_REQUEST_ADDITIONAL, additionalInfo);
        values.put(DatabaseHelper.COLUMN_REQUEST_STATUS, "Pending");
        values.put(DatabaseHelper.COLUMN_REQUEST_CREATED_AT, String.valueOf(System.currentTimeMillis()));

        long newRequestId = -1;
        try {
            newRequestId = db.insert(DatabaseHelper.TABLE_REQUESTS, null, values);
            if (newRequestId != -1) {
                // Insert notification for successful request creation
                ContentValues notifValues = new ContentValues();
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_USER_ID, userId);
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_TITLE, "Request Submitted");
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_MESSAGE, "Your request for " + (serviceName != null ? serviceName : "Service") + " (" + referenceNo + ") has been submitted.");
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 0);
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_REQUEST_ID, newRequestId);
                db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, notifValues);
            }
        } catch (Exception e) {
            // Handle DB insertion error
        } finally {
            db.close();
        }

        if (newRequestId != -1) {
            Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentRequestSubmittedActivity.class);
            intent.putExtra("reference_no", referenceNo);
            intent.putExtra("service_name", serviceName != null ? serviceName : "Service Request");
            intent.putExtra("service_fee", serviceFee);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // Restore button state on failure
            binding.btnCreateRequest.setEnabled(true);
            binding.btnCreateRequest.setText("Submit Request");
            Toast.makeText(this, "Failed to submit request. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
