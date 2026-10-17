package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentServicesBinding;

// Services screen loading dynamic service catalog from SQLite DatabaseHelper using ViewBinding.
public class StudentServicesActivity extends AppCompatActivity {

    private ActivityStudentServicesBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.btnBack.setOnClickListener(v -> finish());

        loadServicesFromDatabase();
    }

    private void loadServicesFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SERVICES, null, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int feeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_FEE);
                int descIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_DESC);

                int index = 0;
                do {
                    if (idIdx == -1 || nameIdx == -1 || feeIdx == -1 || descIdx == -1) {
                        break;
                    }

                    long serviceId = cursor.getLong(idIdx);
                    String name = cursor.getString(nameIdx);
                    double fee = cursor.getDouble(feeIdx);
                    String desc = cursor.getString(descIdx);

                    // Bind data to the 4 existing service cards
                    if (index == 0) {
                        binding.tvCertTitle.setText(name);
                        binding.btnViewCertificate.setOnClickListener(v -> openServiceDetail(serviceId, name, fee, desc));
                    } else if (index == 1) {
                        binding.tvDocTitle.setText(name);
                        binding.btnViewDocument.setOnClickListener(v -> openServiceDetail(serviceId, name, fee, desc));
                    } else if (index == 2) {
                        binding.tvIdTitle.setText(name);
                        binding.btnViewId.setOnClickListener(v -> openServiceDetail(serviceId, name, fee, desc));
                    } else if (index == 3) {
                        binding.tvInquiryTitle.setText(name);
                        binding.btnViewInquiry.setOnClickListener(v -> openServiceDetail(serviceId, name, fee, desc));
                    }
                    index++;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Handle database error gracefully
        } finally {
            db.close();
        }
    }

    private void openServiceDetail(long serviceId, String name, double fee, String desc) {
        Intent intent = new Intent(this, StudentServiceRequestActivity.class);
        intent.putExtra("service_id", serviceId);
        intent.putExtra("service_name", name);
        intent.putExtra("service_fee", fee);
        intent.putExtra("service_desc", desc);
        startActivity(intent);
    }
}
