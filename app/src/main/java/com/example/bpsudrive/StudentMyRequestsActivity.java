package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentMyRequestsBinding;
import com.google.android.material.card.MaterialCardView;

// My Requests screen loading real SQLite request records for the logged-in student using ViewBinding.
public class StudentMyRequestsActivity extends AppCompatActivity {

    private ActivityStudentMyRequestsBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private Filter currentFilter = Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentMyRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        binding.tabAll.setOnClickListener(v -> selectFilter(Filter.ALL));
        binding.tabPending.setOnClickListener(v -> selectFilter(Filter.PENDING));
        binding.tabProcessing.setOnClickListener(v -> selectFilter(Filter.PROCESSING));
        binding.tabCompleted.setOnClickListener(v -> selectFilter(Filter.COMPLETED));

        // Wire bottom navigation
        binding.navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            finish();
        });
        binding.navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNotificationsActivity.class));
            finish();
        });
        binding.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private enum Filter { ALL, PENDING, PROCESSING, COMPLETED }

    private void selectFilter(Filter filter) {
        currentFilter = filter;
        styleTab(binding.tabAll, filter == Filter.ALL);
        styleTab(binding.tabPending, filter == Filter.PENDING);
        styleTab(binding.tabProcessing, filter == Filter.PROCESSING);
        styleTab(binding.tabCompleted, filter == Filter.COMPLETED);

        loadRequests();
    }

    private void loadRequests() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }

        binding.containerRequests.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT r.*, s." + DatabaseHelper.COLUMN_SERVICE_NAME + ", s." + DatabaseHelper.COLUMN_SERVICE_FEE +
                " FROM " + DatabaseHelper.TABLE_REQUESTS + " r" +
                " INNER JOIN " + DatabaseHelper.TABLE_SERVICES + " s ON r." + DatabaseHelper.COLUMN_REQUEST_SERVICE_ID + " = s." + DatabaseHelper.COLUMN_ID +
                " WHERE r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?";

        if (currentFilter == Filter.PENDING) {
            query += " AND r." + DatabaseHelper.COLUMN_REQUEST_STATUS + " = 'Pending'";
        } else if (currentFilter == Filter.PROCESSING) {
            query += " AND r." + DatabaseHelper.COLUMN_REQUEST_STATUS + " = 'Processing'";
        } else if (currentFilter == Filter.COMPLETED) {
            query += " AND r." + DatabaseHelper.COLUMN_REQUEST_STATUS + " = 'Completed'";
        }
        query += " ORDER BY r." + DatabaseHelper.COLUMN_ID + " DESC";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int refIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_REF_NO);
                int serviceNameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_STATUS);

                do {
                    if (idIdx == -1 || refIdx == -1 || serviceNameIdx == -1 || statusIdx == -1) {
                        continue;
                    }

                    long requestId = cursor.getLong(idIdx);
                    String refNo = cursor.getString(refIdx);
                    String serviceName = cursor.getString(serviceNameIdx);
                    String status = cursor.getString(statusIdx);

                    addRequestCard(requestId, refNo, serviceName, status);
                } while (cursor.moveToNext());
            } else {
                // Show empty state
                TextView emptyView = new TextView(this);
                emptyView.setText("No requests found.");
                emptyView.setTextColor(getColor(R.color.text_secondary));
                emptyView.setTextSize(14);
                emptyView.setGravity(Gravity.CENTER);
                emptyView.setPadding(0, 32, 0, 32);
                binding.containerRequests.addView(emptyView);
            }
        } catch (Exception e) {
            // Handle query error
        } finally {
            db.close();
        }
    }

    private void addRequestCard(long requestId, String refNo, String serviceName, String status) {
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
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        innerLayout.setLayoutParams(innerParams);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * density);
        innerLayout.setPadding(padding, padding, padding, padding);

        TextView tvRef = new TextView(this);
        tvRef.setText("Request #" + refNo);
        tvRef.setTextColor(getColor(R.color.bpsu_maroon));
        tvRef.setTextSize(13);
        tvRef.setTypeface(null, Typeface.BOLD);

        TextView tvService = new TextView(this);
        tvService.setText(serviceName);
        tvService.setTextColor(getColor(R.color.text_primary));
        tvService.setTextSize(15);
        tvService.setTypeface(null, Typeface.BOLD);
        tvService.setPadding(0, (int) (4 * density), 0, 0);

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status: " + status);
        tvStatus.setTextColor(getColor(R.color.text_secondary));
        tvStatus.setTextSize(13);
        tvStatus.setPadding(0, (int) (8 * density), 0, 0);

        TextView btnDetails = new TextView(this);
        btnDetails.setText("View Details →");
        btnDetails.setTextColor(getColor(R.color.bpsu_maroon));
        btnDetails.setTextSize(13);
        btnDetails.setTypeface(null, Typeface.BOLD);
        btnDetails.setPadding(0, (int) (12 * density), 0, 0);
        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentRequestDetailsActivity.class);
            intent.putExtra("request_id", requestId);
            startActivity(intent);
        });

        innerLayout.addView(tvRef);
        innerLayout.addView(tvService);
        innerLayout.addView(tvStatus);
        innerLayout.addView(btnDetails);
        card.addView(innerLayout);

        binding.containerRequests.addView(card);
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected
                ? R.drawable.bg_filter_selected
                : R.drawable.bg_filter_unselected);
        tab.setTextColor(getColor(selected ? R.color.bpsu_maroon : R.color.text_secondary));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }
}
