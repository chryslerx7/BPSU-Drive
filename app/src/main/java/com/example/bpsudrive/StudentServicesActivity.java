package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentServicesBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Locale;

// Services screen dynamically loading all service catalog items from SQLite DatabaseHelper using ViewBinding.
public class StudentServicesActivity extends AppCompatActivity {

    private ActivityStudentServicesBinding binding;
    private DatabaseHelper databaseHelper;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentServicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Wire real-time service search
        binding.etSearchServices.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                loadServicesFromDatabase();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadServicesFromDatabase();
    }

    private void loadServicesFromDatabase() {
        binding.containerServices.removeAllViews();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Ensure missing default services exist in SQLite safely
        databaseHelper.checkAndSeedServices(db);

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(DatabaseHelper.TABLE_SERVICES);

        ArrayList<String> queryArgs = new ArrayList<>();
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query.append(" WHERE ").append(DatabaseHelper.COLUMN_SERVICE_NAME).append(" LIKE ? OR ")
                    .append(DatabaseHelper.COLUMN_SERVICE_DESC).append(" LIKE ?");
            queryArgs.add("%" + searchQuery + "%");
            queryArgs.add("%" + searchQuery + "%");
        }

        query.append(" ORDER BY ").append(DatabaseHelper.COLUMN_ID).append(" ASC");

        try (Cursor cursor = db.rawQuery(query.toString(), queryArgs.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int feeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_FEE);
                int descIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_DESC);

                do {
                    if (idIdx == -1 || nameIdx == -1 || feeIdx == -1 || descIdx == -1) {
                        continue;
                    }

                    long serviceId = cursor.getLong(idIdx);
                    String name = cursor.getString(nameIdx);
                    double fee = cursor.getDouble(feeIdx);
                    String desc = cursor.getString(descIdx);

                    addServiceCard(serviceId, name, fee, desc);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Handle error
        } finally {
            db.close();
        }
    }

    private void addServiceCard(long serviceId, String name, double fee, String desc) {
        float density = getResources().getDisplayMetrics().density;

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, (int) (12 * density));
        card.setLayoutParams(cardParams);
        card.setRadius(16 * density);
        card.setCardBackgroundColor(getColor(R.color.white));
        card.setCardElevation(2 * density);
        card.setStrokeColor(getColor(R.color.stroke_grey));
        card.setStrokeWidth((int) (1 * density));

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * density);
        innerLayout.setPadding(padding, padding, padding, padding);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(name);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextColor(getColor(R.color.text_secondary));
        tvDesc.setTextSize(13);
        tvDesc.setPadding(0, (int) (4 * density), 0, 0);

        TextView tvFee = new TextView(this);
        tvFee.setText(fee > 0 ? "Fee: ₱" + String.format(Locale.US, "%.2f", fee) : "Free");
        tvFee.setTextColor(getColor(R.color.text_primary));
        tvFee.setTextSize(13);
        tvFee.setTypeface(null, Typeface.BOLD);
        tvFee.setPadding(0, (int) (8 * density), 0, 0);

        TextView btnView = new TextView(this);
        btnView.setText("View Service →");
        btnView.setTextColor(getColor(R.color.bpsu_maroon));
        btnView.setTextSize(13);
        btnView.setTypeface(null, Typeface.BOLD);
        btnView.setPadding(0, (int) (4 * density), 0, 0);
        btnView.setOnClickListener(v -> openServiceDetail(serviceId, name, fee, desc));

        innerLayout.addView(tvTitle);
        innerLayout.addView(tvDesc);
        innerLayout.addView(tvFee);
        innerLayout.addView(btnView);
        card.addView(innerLayout);

        binding.containerServices.addView(card);
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
