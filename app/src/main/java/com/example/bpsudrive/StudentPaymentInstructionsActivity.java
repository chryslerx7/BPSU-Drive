package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentPaymentInstructionsBinding;

// Payment Instructions screen loading real request and fee data using ViewBinding with existing receipt checks.
public class StudentPaymentInstructionsActivity extends AppCompatActivity {

    private ActivityStudentPaymentInstructionsBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentPaymentInstructionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        long requestId = getIntent().getLongExtra("request_id", -1);
        if (requestId != -1) {
            loadPaymentDetails(requestId);
        } else {
            Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPaymentDetails(long requestId) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            finish();
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Step 1: Check if a receipt has already been submitted for this request
        String existingReceiptStatus = null;
        try (Cursor receiptCursor = db.query(DatabaseHelper.TABLE_RECEIPTS,
                new String[]{DatabaseHelper.COLUMN_RECEIPT_STATUS},
                DatabaseHelper.COLUMN_RECEIPT_REQUEST_ID + " = ?",
                new String[]{String.valueOf(requestId)}, null, null, DatabaseHelper.COLUMN_ID + " DESC")) {
            if (receiptCursor != null && receiptCursor.moveToFirst()) {
                int statusIdx = receiptCursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_STATUS);
                if (statusIdx != -1) {
                    existingReceiptStatus = receiptCursor.getString(statusIdx);
                }
            }
        } catch (Exception e) {
            // Handle query error
        }

        String query = "SELECT r.*, s." + DatabaseHelper.COLUMN_SERVICE_NAME + ", s." + DatabaseHelper.COLUMN_SERVICE_FEE +
                " FROM " + DatabaseHelper.TABLE_REQUESTS + " r" +
                " INNER JOIN " + DatabaseHelper.TABLE_SERVICES + " s ON r." + DatabaseHelper.COLUMN_REQUEST_SERVICE_ID + " = s." + DatabaseHelper.COLUMN_ID +
                " WHERE r." + DatabaseHelper.COLUMN_ID + " = ? AND r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(requestId), String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                int refIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_REF_NO);
                int serviceNameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int feeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_FEE);

                if (refIdx != -1 && serviceNameIdx != -1 && feeIdx != -1) {
                    String refNo = cursor.getString(refIdx);
                    String serviceName = cursor.getString(serviceNameIdx);
                    double fee = cursor.getDouble(feeIdx);

                    // Step 2: If a receipt is already submitted (Pending Verification or Verified), route directly to Submitted screen
                    if ("Pending Verification".equalsIgnoreCase(existingReceiptStatus) || "Verified".equalsIgnoreCase(existingReceiptStatus)) {
                        Toast.makeText(this, "A receipt has already been submitted for this request.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, StudentReceiptSubmittedActivity.class);
                        intent.putExtra("request_id", requestId);
                        intent.putExtra("reference_no", refNo);
                        intent.putExtra("service_name", serviceName);
                        intent.putExtra("service_fee", fee);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    binding.tvPaymentTitle.setText(serviceName);
                    binding.tvPaymentAmount.setText("₱" + String.format("%.2f", fee));
                    binding.tvPaymentReference.setText("Reference No: " + refNo);

                    binding.btnContinue.setOnClickListener(v -> {
                        Intent intent = new Intent(this, StudentCameraReceiptActivity.class);
                        intent.putExtra("request_id", requestId);
                        intent.putExtra("service_name", serviceName);
                        intent.putExtra("service_fee", fee);
                        intent.putExtra("reference_no", refNo);
                        startActivity(intent);
                    });
                }
            } else {
                Toast.makeText(this, "Request access denied or not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading payment instructions", Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            db.close();
        }
    }
}
