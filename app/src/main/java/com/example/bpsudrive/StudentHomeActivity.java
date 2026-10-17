package com.example.bpsudrive;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentHomeBinding;
import com.google.android.material.card.MaterialCardView;

// Student Home screen with dashboard summary, recent requests, ViewBinding, and session safety.
public class StudentHomeActivity extends AppCompatActivity {

    private ActivityStudentHomeBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Verify valid student session
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, StudentLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        markSelected(binding.bottomNav.navHome);
        markUnselected(binding.bottomNav.navRequests);
        markUnselected(binding.bottomNav.navNotifications);
        markUnselected(binding.bottomNav.navProfile);

        // Wire bottom navigation clicks
        binding.bottomNav.navHome.setOnClickListener(v -> {});
        binding.bottomNav.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentMyRequestsActivity.class));
            finish();
        });
        binding.bottomNav.navNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNotificationsActivity.class));
            finish();
        });
        binding.bottomNav.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });

        // Wire top notification bell
        binding.includeHomeHeader.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, StudentNotificationsActivity.class)));

        // Wire new request button and service rows
        binding.btnNewRequest.setOnClickListener(v ->
                startActivity(new Intent(this, StudentCreateRequestActivity.class)));

        binding.rowService1.setOnClickListener(v -> startActivity(new Intent(this, StudentServicesActivity.class)));
        binding.rowService2.setOnClickListener(v -> startActivity(new Intent(this, StudentServicesActivity.class)));
        binding.rowService3.setOnClickListener(v -> startActivity(new Intent(this, StudentServicesActivity.class)));
        binding.rowService4.setOnClickListener(v -> startActivity(new Intent(this, StudentServicesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // 1. Load user greeting
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                if (nameIdx != -1) {
                    String fullName = cursor.getString(nameIdx);
                    String firstName = fullName.split(" ")[0];
                    binding.tvGreeting.setText("Good morning, " + firstName);
                } else {
                    binding.tvGreeting.setText("Good morning, Student");
                }
            }
        } catch (Exception e) {
            binding.tvGreeting.setText("Good morning, Student");
        }

        // 2. Load summary counts
        int totalRequests = 0;
        int pendingRequests = 0;
        int awaitingVerification = 0;

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REQUESTS + " WHERE " + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?",
                new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                totalRequests = cursor.getInt(0);
            }
        } catch (Exception e) {
            // handle
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REQUESTS + " WHERE " + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_REQUEST_STATUS + " = ?",
                new String[]{String.valueOf(userId), "Pending"})) {
            if (cursor != null && cursor.moveToFirst()) {
                pendingRequests = cursor.getInt(0);
            }
        } catch (Exception e) {
            // handle
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RECEIPTS + " rc INNER JOIN " + DatabaseHelper.TABLE_REQUESTS + " r ON rc." + DatabaseHelper.COLUMN_RECEIPT_REQUEST_ID + " = r." + DatabaseHelper.COLUMN_ID +
                        " WHERE r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ? AND rc." + DatabaseHelper.COLUMN_RECEIPT_STATUS + " = ?",
                new String[]{String.valueOf(userId), "Pending Verification"})) {
            if (cursor != null && cursor.moveToFirst()) {
                awaitingVerification = cursor.getInt(0);
            }
        } catch (Exception e) {
            // handle
        }

        binding.tvTotalRequests.setText(String.valueOf(totalRequests));
        binding.tvPendingRequests.setText(String.valueOf(pendingRequests));
        binding.tvAwaitingVerification.setText(String.valueOf(awaitingVerification));

        // 3. Load up to 3 recent requests
        binding.containerRecentRequests.removeAllViews();
        String recentQuery = "SELECT r.*, s." + DatabaseHelper.COLUMN_SERVICE_NAME + " FROM " + DatabaseHelper.TABLE_REQUESTS + " r" +
                " INNER JOIN " + DatabaseHelper.TABLE_SERVICES + " s ON r." + DatabaseHelper.COLUMN_REQUEST_SERVICE_ID + " = s." + DatabaseHelper.COLUMN_ID +
                " WHERE r." + DatabaseHelper.COLUMN_REQUEST_USER_ID + " = ?" +
                " ORDER BY r." + DatabaseHelper.COLUMN_ID + " DESC LIMIT 3";

        try (Cursor cursor = db.rawQuery(recentQuery, new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                binding.tvEmptyRequests.setVisibility(View.GONE);
                do {
                    int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                    int refIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_REF_NO);
                    int serviceIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_NAME);
                    int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_STATUS);

                    if (idIdx != -1 && refIdx != -1 && serviceIdx != -1 && statusIdx != -1) {
                        long reqId = cursor.getLong(idIdx);
                        String refNo = cursor.getString(refIdx);
                        String serviceName = cursor.getString(serviceIdx);
                        String status = cursor.getString(statusIdx);

                        addRecentRequestCard(reqId, refNo, serviceName, status);
                    }
                } while (cursor.moveToNext());
            } else {
                binding.tvEmptyRequests.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            binding.tvEmptyRequests.setVisibility(View.VISIBLE);
        } finally {
            db.close();
        }
    }

    private void addRecentRequestCard(long requestId, String refNo, String serviceName, String status) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 6, 16, 6);
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(getColor(R.color.white));
        cardView.setRadius(16 * getResources().getDisplayMetrics().density);
        cardView.setCardElevation(2 * getResources().getDisplayMetrics().density);
        cardView.setStrokeColor(getColor(R.color.stroke_grey));
        cardView.setStrokeWidth(1);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        innerLayout.setPadding(padding, padding, padding, padding);

        TextView tvMeta = new TextView(this);
        tvMeta.setText("Reference: " + refNo);
        tvMeta.setTextColor(getColor(R.color.text_secondary));
        tvMeta.setTextSize(12);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(serviceName);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, 4, 0, 0);

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status: " + status);
        tvStatus.setTextColor(getColor(R.color.bpsu_maroon));
        tvStatus.setTextSize(13);
        tvStatus.setPadding(0, 8, 0, 0);
        tvStatus.setTypeface(null, Typeface.BOLD);

        innerLayout.addView(tvMeta);
        innerLayout.addView(tvTitle);
        innerLayout.addView(tvStatus);
        cardView.addView(innerLayout);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentRequestDetailsActivity.class);
            intent.putExtra("request_id", requestId);
            startActivity(intent);
        });

        binding.containerRecentRequests.addView(cardView);
    }

    private void markSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_nav_selected);
        tab.setTextColor(getColor(R.color.bpsu_maroon));
        tab.setTypeface(null, Typeface.BOLD);
    }

    private void markUnselected(TextView tab) {
        tab.setBackgroundResource(0);
        tab.setTextColor(getColor(R.color.text_secondary));
        tab.setTypeface(null, Typeface.NORMAL);
    }
}
