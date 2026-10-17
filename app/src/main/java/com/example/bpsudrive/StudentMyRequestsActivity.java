package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentMyRequestsBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// My Requests screen loading real SQLite request records with search, status filtering, and creation date display.
public class StudentMyRequestsActivity extends AppCompatActivity {

    private ActivityStudentMyRequestsBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private Filter currentFilter = Filter.ALL;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentMyRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Wire tab click listeners
        binding.tabAll.setOnClickListener(v -> selectFilter(Filter.ALL));
        binding.tabPending.setOnClickListener(v -> selectFilter(Filter.PENDING));
        binding.tabProcessing.setOnClickListener(v -> selectFilter(Filter.PROCESSING));
        binding.tabCompleted.setOnClickListener(v -> selectFilter(Filter.COMPLETED));
        binding.tabCancelled.setOnClickListener(v -> selectFilter(Filter.CANCELLED));

        // Wire real-time search input
        binding.etSearchRequests.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                loadRequests();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

    private enum Filter { ALL, PENDING, PROCESSING, COMPLETED, CANCELLED }

    private void selectFilter(Filter filter) {
        currentFilter = filter;
        styleTab(binding.tabAll, filter == Filter.ALL);
        styleTab(binding.tabPending, filter == Filter.PENDING);
        styleTab(binding.tabProcessing, filter == Filter.PROCESSING);
        styleTab(binding.tabCompleted, filter == Filter.COMPLETED);
        styleTab(binding.tabCancelled, filter == Filter.CANCELLED);

        loadRequests();
    }

    private void loadRequests() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }

        binding.containerRequests.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        StringBuilder query = new StringBuilder();
        query.append("SELECT r.*, s.").append(DatabaseHelper.COLUMN_SERVICE_NAME).append(", s.").append(DatabaseHelper.COLUMN_SERVICE_FEE)
                .append(" FROM ").append(DatabaseHelper.TABLE_REQUESTS).append(" r")
                .append(" INNER JOIN ").append(DatabaseHelper.TABLE_SERVICES).append(" s ON r.").append(DatabaseHelper.COLUMN_REQUEST_SERVICE_ID).append(" = s.").append(DatabaseHelper.COLUMN_ID)
                .append(" WHERE r.").append(DatabaseHelper.COLUMN_REQUEST_USER_ID).append(" = ?");

        ArrayList<String> queryArgs = new ArrayList<>();
        queryArgs.add(String.valueOf(userId));

        if (currentFilter == Filter.PENDING) {
            query.append(" AND r.").append(DatabaseHelper.COLUMN_REQUEST_STATUS).append(" = 'Pending'");
        } else if (currentFilter == Filter.PROCESSING) {
            query.append(" AND r.").append(DatabaseHelper.COLUMN_REQUEST_STATUS).append(" = 'Processing'");
        } else if (currentFilter == Filter.COMPLETED) {
            query.append(" AND r.").append(DatabaseHelper.COLUMN_REQUEST_STATUS).append(" = 'Completed'");
        } else if (currentFilter == Filter.CANCELLED) {
            query.append(" AND r.").append(DatabaseHelper.COLUMN_REQUEST_STATUS).append(" = 'Cancelled'");
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            query.append(" AND (s.").append(DatabaseHelper.COLUMN_SERVICE_NAME).append(" LIKE ? OR r.").append(DatabaseHelper.COLUMN_REQUEST_REF_NO).append(" LIKE ?)");
            queryArgs.add("%" + searchQuery + "%");
            queryArgs.add("%" + searchQuery + "%");
        }

        query.append(" ORDER BY r.").append(DatabaseHelper.COLUMN_ID).append(" DESC");

        try (Cursor cursor = db.rawQuery(query.toString(), queryArgs.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int refIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_REF_NO);
                int serviceNameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_STATUS);
                int dateIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_CREATED_AT);

                do {
                    if (idIdx == -1 || refIdx == -1 || serviceNameIdx == -1 || statusIdx == -1) {
                        continue;
                    }

                    long requestId = cursor.getLong(idIdx);
                    String refNo = cursor.getString(refIdx);
                    String serviceName = cursor.getString(serviceNameIdx);
                    String status = cursor.getString(statusIdx);
                    String rawDate = dateIdx != -1 ? cursor.getString(dateIdx) : "";

                    addRequestCard(requestId, refNo, serviceName, status, formatDate(rawDate));
                } while (cursor.moveToNext());
            } else {
                // Show empty state
                TextView emptyView = new TextView(this);
                emptyView.setText(searchQuery.isEmpty() ? "No requests found." : "No requests match '" + searchQuery + "'.");
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

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "";
        }
        try {
            long time = Long.parseLong(rawDate);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return sdf.format(new Date(time));
        } catch (Exception e) {
            return rawDate;
        }
    }

    private void addRequestCard(long requestId, String refNo, String serviceName, String status, String formattedDate) {
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

        LinearLayout metaHeader = new LinearLayout(this);
        metaHeader.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvRef = new TextView(this);
        tvRef.setText("Request #" + refNo);
        tvRef.setTextColor(getColor(R.color.bpsu_maroon));
        tvRef.setTextSize(13);
        tvRef.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams refParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvRef.setLayoutParams(refParams);

        TextView tvDate = new TextView(this);
        tvDate.setText(formattedDate);
        tvDate.setTextColor(getColor(R.color.text_secondary));
        tvDate.setTextSize(12);

        metaHeader.addView(tvRef);
        metaHeader.addView(tvDate);

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

        innerLayout.addView(metaHeader);
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
