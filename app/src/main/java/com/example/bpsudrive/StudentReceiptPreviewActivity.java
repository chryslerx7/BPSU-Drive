package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentReceiptPreviewBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Receipt preview activity using ViewBinding, displaying actual request, student, and receipt details from SQLite.
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
            binding.tvReceiptService.setText("Service: " + serviceName);
        }
        binding.tvReceiptAmount.setText("Amount: ₱" + String.format("%.2f", serviceFee));

        // Dynamically load correct student name and request creation date from SQLite
        int userId = sessionManager.getUserId();
        if (requestId != -1 && userId != -1) {
            loadRequestStudentAndDate(requestId, userId);
        }

        // Load and display actual captured or selected receipt image if available
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (bitmap != null) {
                    binding.ivReceiptPhoto.setImageBitmap(bitmap);
                    binding.ivReceiptPhoto.setVisibility(View.VISIBLE);
                }
            }
        }

        binding.btnSubmitReceipt.setOnClickListener(v -> {
            if (requestId == -1) {
                Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
                return;
            }

            int currentUserId = sessionManager.getUserId();
            if (currentUserId == -1) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 1: Verify request exists, belongs to student, and is not Cancelled
            SQLiteDatabase readDb = databaseHelper.getReadableDatabase();
            String requestQuery = "SELECT " + DatabaseHelper.COLUMN_REQUEST_STATUS +
                    " FROM " + DatabaseHelper.TABLE_REQUESTS +
                    " WHERE " + DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?";

            try (Cursor cursor = readDb.rawQuery(requestQuery, new String[]{String.valueOf(requestId), String.valueOf(currentUserId)})) {
                if (cursor != null && cursor.moveToFirst()) {
                    int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_STATUS);
                    if (statusIdx != -1) {
                        String requestStatus = cursor.getString(statusIdx);
                        if ("Cancelled".equalsIgnoreCase(requestStatus)) {
                            Toast.makeText(this, "Cannot submit receipts for a cancelled request.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "Request access denied or not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error validating request eligibility", Toast.LENGTH_SHORT).show();
                return;
            } finally {
                readDb.close();
            }

            // Step 2: Check if an active (Pending Verification or Verified) receipt already exists
            boolean hasActiveReceipt = false;
            SQLiteDatabase checkDb = databaseHelper.getReadableDatabase();
            try (Cursor cursor = checkDb.query(DatabaseHelper.TABLE_RECEIPTS,
                    new String[]{DatabaseHelper.COLUMN_RECEIPT_STATUS},
                    DatabaseHelper.COLUMN_RECEIPT_REQUEST_ID + " = ?",
                    new String[]{String.valueOf(requestId)}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_STATUS);
                        if (statusIdx != -1) {
                            String receiptStatus = cursor.getString(statusIdx);
                            if ("Pending Verification".equalsIgnoreCase(receiptStatus) || "Verified".equalsIgnoreCase(receiptStatus)) {
                                hasActiveReceipt = true;
                                break;
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                // Handle check error
            } finally {
                checkDb.close();
            }

            if (hasActiveReceipt) {
                Toast.makeText(this, "A receipt is already submitted and awaiting verification.", Toast.LENGTH_LONG).show();
                return;
            }

            // Step 3: Prevent duplicate taps by temporarily disabling submit button
            binding.btnSubmitReceipt.setEnabled(false);
            binding.btnSubmitReceipt.setText("Submitting...");

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
                    // Insert notification for successful receipt submission
                    ContentValues notifValues = new ContentValues();
                    notifValues.put(DatabaseHelper.COLUMN_NOTIF_USER_ID, currentUserId);
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
                // Restore button state on failure
                binding.btnSubmitReceipt.setEnabled(true);
                binding.btnSubmitReceipt.setText("Submit Receipt");
                Toast.makeText(this, "Failed to submit receipt. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRequestStudentAndDate(long requestId, int userId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT r." + DatabaseHelper.COLUMN_REQUEST_CREATED_AT + ", u." + DatabaseHelper.COLUMN_USER_NAME + ", u." + DatabaseHelper.COLUMN_USER_STUDENT_ID +
                " FROM " + DatabaseHelper.TABLE_REQUESTS + " r" +
                " INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = u." + DatabaseHelper.COLUMN_ID +
                " WHERE r." + DatabaseHelper.COLUMN_ID + " = ? AND r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(requestId), String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int studentIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STUDENT_ID);
                int createdAtIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_CREATED_AT);

                if (nameIdx != -1 && studentIdIdx != -1) {
                    String studentName = cursor.getString(nameIdx);
                    String studentId = cursor.getString(studentIdIdx);
                    binding.tvReceiptStudent.setText("Student: " + studentName + " (" + studentId + ")");
                }

                if (createdAtIdx != -1) {
                    String rawDate = cursor.getString(createdAtIdx);
                    binding.tvReceiptDate.setText("Request Date: " + formatDate(rawDate));
                }
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "N/A";
        }
        try {
            long time = Long.parseLong(rawDate);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return sdf.format(new Date(time));
        } catch (Exception e) {
            return rawDate;
        }
    }
}
