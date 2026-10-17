package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentReceiptPreviewBinding;

// Receipt preview activity using ViewBinding, inserting receipt record and notification into SQLite upon submission.
public class StudentReceiptPreviewActivity extends AppCompatActivity {

    private ActivityStudentReceiptPreviewBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentReceiptPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRetakeReceipt.setOnClickListener(v -> finish());

        long requestId = getIntent().getLongExtra("request_id", -1);
        String serviceName = getIntent().getStringExtra("service_name");
        double serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);
        String refNo = getIntent().getStringExtra("reference_no");
        String imagePath = getIntent().getStringExtra("image_path");

        if (refNo != null) {
            binding.tvReceiptReference.setText("Reference: " + refNo);
        }
        if (serviceName != null) {
            binding.tvReceiptService.setText(serviceName);
        }
        binding.tvReceiptAmount.setText("Amount: ₱" + String.format("%.2f", serviceFee));

        binding.btnSubmitReceipt.setOnClickListener(v -> {
            if (requestId == -1) {
                Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = sessionManager.getUserId();
            if (userId == -1) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_RECEIPT_REQUEST_ID, requestId);
            values.put(DatabaseHelper.COLUMN_RECEIPT_IMAGE_PATH, imagePath != null ? imagePath : "default_receipt.jpg");
            values.put(DatabaseHelper.COLUMN_RECEIPT_STATUS, "Pending Verification");
            values.put(DatabaseHelper.COLUMN_RECEIPT_SUBMITTED_AT, String.valueOf(System.currentTimeMillis()));

            long receiptId = -1;
            try {
                receiptId = db.insert(DatabaseHelper.TABLE_RECEIPTS, null, values);
                if (receiptId != -1) {
                    // Insert notification for receipt submission
                    ContentValues notifValues = new ContentValues();
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_USER_ID, userId);
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_TITLE, "Receipt Submitted");
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_MESSAGE, "Your payment receipt for " + (refNo != null ? refNo : "Request") + " has been submitted for verification.");
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 0);
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_REQUEST_ID, requestId);
                    db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, notifValues);
                }
            } catch (Exception e) {
                // Handle insert error
            } finally {
                db.close();
            }

            if (receiptId != -1) {
                Toast.makeText(this, "Receipt submitted successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, StudentReceiptSubmittedActivity.class);
                intent.putExtra("request_id", requestId);
                intent.putExtra("reference_no", refNo);
                intent.putExtra("service_name", serviceName);
                intent.putExtra("service_fee", serviceFee);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to submit receipt. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
