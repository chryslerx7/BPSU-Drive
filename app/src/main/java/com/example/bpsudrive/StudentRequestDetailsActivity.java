package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentRequestDetailsBinding;

// Request Details screen with request cancellation and payment/receipt instruction access using ViewBinding.
public class StudentRequestDetailsActivity extends AppCompatActivity {

    private ActivityStudentRequestDetailsBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentRequestDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        long requestId = getIntent().getLongExtra("request_id", -1);
        if (requestId != -1) {
            loadRequestDetails(requestId);

            // Wire primary action button to open Payment Instructions
            binding.btnPrimaryAction.setOnClickListener(v -> {
                Intent intent = new Intent(this, StudentPaymentInstructionsActivity.class);
                intent.putExtra("request_id", requestId);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRequestDetails(long requestId) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            finish();
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT r.*, s." + DatabaseHelper.COLUMN_SERVICE_NAME + ", s." + DatabaseHelper.COLUMN_SERVICE_FEE + ", " +
                "u." + DatabaseHelper.COLUMN_USER_NAME + ", u." + DatabaseHelper.COLUMN_USER_STUDENT_ID + ", u." + DatabaseHelper.COLUMN_USER_EMAIL +
                " FROM " + DatabaseHelper.TABLE_REQUESTS + " r" +
                " INNER JOIN " + DatabaseHelper.TABLE_SERVICES + " s ON r." + DatabaseHelper.COLUMN_REQUEST_SERVICE_ID + " = s." + DatabaseHelper.COLUMN_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = u." + DatabaseHelper.COLUMN_ID +
                " WHERE r." + DatabaseHelper.COLUMN_ID + " = ? AND r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(requestId), String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                int refIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_REF_NO);
                int serviceNameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int feeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_FEE);
                int purposeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_PURPOSE);
                int additionalIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_ADDITIONAL);
                int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_STATUS);
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);

                if (refIdx != -1 && serviceNameIdx != -1 && feeIdx != -1 && purposeIdx != -1 && statusIdx != -1) {
                    String refNo = cursor.getString(refIdx);
                    String serviceName = cursor.getString(serviceNameIdx);
                    double fee = cursor.getDouble(feeIdx);
                    String purpose = cursor.getString(purposeIdx);
                    String additional = cursor.getString(additionalIdx);
                    String status = cursor.getString(statusIdx);
                    String studentName = nameIdx != -1 ? cursor.getString(nameIdx) : "Student";
                    String studentId = studentIdIdx != -1 ? cursor.getString(studentIdIdx) : "";

                    binding.tvRequestTitle.setText(serviceName);
                    binding.tvRequestReference.setText("Reference: " + refNo);
                    binding.tvStageChip.setText(status);
                    binding.tvStudentInfo.setText("Student: " + studentName + " (" + studentId + ")");
                    binding.tvFeeAmount.setText("Fee: ₱" + String.format("%.2f", fee));
                    binding.tvRequestStatus.setText("Request Status: " + status);
                    binding.tvRequestDescription.setText("Purpose: " + purpose);
                    binding.tvReceiptInfo.setText("Additional Info: " + (additional != null && !additional.isEmpty() ? additional : "None"));
                    binding.tvReceiptStatus.setText("Receipt Status: Pending Submission");

                    // Show cancellation button ONLY if status is Pending
                    if ("Pending".equalsIgnoreCase(status)) {
                        binding.btnCancelRequest.setVisibility(View.VISIBLE);
                        binding.btnCancelRequest.setOnClickListener(v -> showCancelConfirmationDialog(requestId));
                    } else {
                        binding.btnCancelRequest.setVisibility(View.GONE);
                    }
                }
            } else {
                Toast.makeText(this, "Request access denied or not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading request details", Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            db.close();
        }
    }

    private void showCancelConfirmationDialog(long requestId) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel this request? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> executeCancellation(requestId))
                .setNegativeButton("Keep Request", null)
                .show();
    }

    private void executeCancellation(long requestId) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REQUEST_STATUS, "Cancelled");

        int rowsAffected = -1;
        try {
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_REQUESTS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                    new String[]{String.valueOf(requestId), String.valueOf(userId), "Pending"}
            );

            if (rowsAffected > 0) {
                // Insert notification for request cancellation
                ContentValues notifValues = new ContentValues();
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_USER_ID, userId);
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_TITLE, "Request Cancelled");
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_MESSAGE, "Your service request has been cancelled.");
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 0);
                notifValues.put(DatabaseHelper.COLUMN_NOTIF_REQUEST_ID, requestId);
                db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, notifValues);
            }
        } catch (Exception e) {
            // Handle update exception
        } finally {
            db.close();
        }

        if (rowsAffected > 0) {
            Toast.makeText(this, "Request cancelled successfully", Toast.LENGTH_SHORT).show();
            // Reload request details to reflect updated status and hide cancel button
            loadRequestDetails(requestId);
        } else {
            Toast.makeText(this, "Failed to cancel request. It may no longer be pending.", Toast.LENGTH_LONG).show();
        }
    }
}
