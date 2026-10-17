package com.example.bpsudrive;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bpsudrive.databinding.ActivityStudentNotificationsBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Notifications screen loading real SQLite notifications with localized timestamp formatting using ViewBinding.
public class StudentNotificationsActivity extends AppCompatActivity {

    private static final String TAG = "BpsuDriveNotif";
    private ActivityStudentNotificationsBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        markSelected(binding.bottomNav.navNotifications);
        markUnselected(binding.bottomNav.navHome);

        // Wire bottom navigation
        binding.bottomNav.navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            finish();
        });
        binding.bottomNav.navRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentMyRequestsActivity.class));
            finish();
        });
        binding.bottomNav.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        int userId = sessionManager.getUserId();
        Log.d(TAG, "loadNotifications called. Current userId: " + userId);
        if (userId == -1) {
            Log.e(TAG, "loadNotifications: Invalid userId (-1). User not logged in.");
            return;
        }

        binding.containerNotificationCards.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTIFICATIONS +
                " WHERE " + DatabaseHelper.COLUMN_NOTIF_USER_ID + " = ?" +
                " ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            int count = cursor != null ? cursor.getCount() : 0;
            Log.d(TAG, "Notification query returned row count: " + count);

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int titleIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_TITLE);
                int msgIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_MESSAGE);
                int timeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP);
                int readIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_IS_READ);
                int reqIdIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTIF_REQUEST_ID);

                do {
                    if (idIdx == -1 || titleIdx == -1 || msgIdx == -1 || timeIdx == -1 || readIdx == -1) {
                        Log.e(TAG, "Notification cursor column index missing!");
                        continue;
                    }

                    long notifId = cursor.getLong(idIdx);
                    String title = cursor.getString(titleIdx);
                    String message = cursor.getString(msgIdx);
                    String timestamp = cursor.getString(timeIdx);
                    int isRead = cursor.getInt(readIdx);
                    long relatedRequestId = reqIdIdx != -1 ? cursor.getLong(reqIdIdx) : -1;

                    Log.d(TAG, "Loaded notification ID: " + notifId + ", title: " + title + ", timestamp: " + timestamp + ", isRead: " + isRead + ", requestId: " + relatedRequestId);
                    addNotificationCard(notifId, title, message, timestamp, isRead == 1, relatedRequestId);
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No notifications found for userId: " + userId + ". Showing empty state.");
                // Show empty state
                TextView emptyView = new TextView(this);
                emptyView.setText("No notifications yet.");
                emptyView.setTextColor(getColor(R.color.text_secondary));
                emptyView.setTextSize(14);
                emptyView.setGravity(Gravity.CENTER);
                emptyView.setPadding(0, 32, 0, 32);
                binding.containerNotificationCards.addView(emptyView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying notifications", e);
        } finally {
            db.close();
        }
    }

    private void addNotificationCard(long notifId, String title, String message, String timestamp, boolean isRead, long relatedRequestId) {
        float density = getResources().getDisplayMetrics().density;

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, (int) (12 * density));
        card.setLayoutParams(cardParams);
        card.setRadius(16 * density);
        
        if (isRead) {
            card.setCardBackgroundColor(getColor(R.color.white));
        } else {
            card.setCardBackgroundColor(0xFFF9F0F1); // subtle tint for unread
        }

        card.setCardElevation(1 * density);
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

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(null, isRead ? Typeface.NORMAL : Typeface.BOLD);

        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        tvMessage.setTextColor(getColor(R.color.text_secondary));
        tvMessage.setTextSize(13);
        tvMessage.setPadding(0, (int) (4 * density), 0, 0);

        TextView tvTime = new TextView(this);
        tvTime.setText(formatTimestamp(timestamp));
        tvTime.setTextColor(getColor(R.color.text_secondary));
        tvTime.setTextSize(12);
        tvTime.setPadding(0, (int) (4 * density), 0, 0);

        innerLayout.addView(tvTitle);
        innerLayout.addView(tvMessage);
        innerLayout.addView(tvTime);
        card.addView(innerLayout);

        // Click listener: mark as read and open request details if request ID exists
        card.setOnClickListener(v -> {
            Log.d(TAG, "Notification clicked: ID " + notifId + ", relatedRequestId: " + relatedRequestId);
            markNotificationAsRead(notifId);
            if (relatedRequestId != -1) {
                Intent intent = new Intent(this, StudentRequestDetailsActivity.class);
                intent.putExtra("request_id", relatedRequestId);
                startActivity(intent);
            } else {
                loadNotifications();
            }
        });

        binding.containerNotificationCards.addView(card);
    }

    private String formatTimestamp(String timestampStr) {
        try {
            long timeMillis = Long.parseLong(timestampStr);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        } catch (Exception e) {
            return "Today";
        }
    }

    private void markNotificationAsRead(long notifId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 1);
        try {
            int rows = db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values,
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(notifId)});
            Log.d(TAG, "markNotificationAsRead rows affected: " + rows + " for notification ID: " + notifId);
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification read state", e);
        } finally {
            db.close();
        }
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
