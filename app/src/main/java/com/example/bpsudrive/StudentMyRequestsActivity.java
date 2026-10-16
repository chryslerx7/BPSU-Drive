package com.example.bpsudrive;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// ONE reusable My Requests screen with four UI-only filter states:
// ALL / PENDING / PROCESSING / COMPLETED.
// Static sample cards only. No SQLite, no retrieval, no business logic.
public class StudentMyRequestsActivity extends AppCompatActivity {

    private TextView tabAll;
    private TextView tabPending;
    private TextView tabProcessing;
    private TextView tabCompleted;
    private View cardPending;
    private View cardProcessing;
    private View cardCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_my_requests);

        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabProcessing = findViewById(R.id.tabProcessing);
        tabCompleted = findViewById(R.id.tabCompleted);
        cardPending = findViewById(R.id.requestCard1);
        cardProcessing = findViewById(R.id.requestCard2);
        cardCompleted = findViewById(R.id.requestCard3);

        tabAll.setOnClickListener(v -> selectFilter(Filter.ALL));
        tabPending.setOnClickListener(v -> selectFilter(Filter.PENDING));
        tabProcessing.setOnClickListener(v -> selectFilter(Filter.PROCESSING));
        tabCompleted.setOnClickListener(v -> selectFilter(Filter.COMPLETED));

        selectFilter(Filter.ALL);

        // Wire bottom navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_notifications).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentNotificationsActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, StudentProfileActivity.class));
            finish();
        });
    }

    private enum Filter { ALL, PENDING, PROCESSING, COMPLETED }

    private void selectFilter(Filter filter) {
        styleTab(tabAll, filter == Filter.ALL);
        styleTab(tabPending, filter == Filter.PENDING);
        styleTab(tabProcessing, filter == Filter.PROCESSING);
        styleTab(tabCompleted, filter == Filter.COMPLETED);

        cardPending.setVisibility(
                (filter == Filter.ALL || filter == Filter.PENDING) ? View.VISIBLE : View.GONE);
        cardProcessing.setVisibility(
                (filter == Filter.ALL || filter == Filter.PROCESSING) ? View.VISIBLE : View.GONE);
        cardCompleted.setVisibility(
                (filter == Filter.ALL || filter == Filter.COMPLETED) ? View.VISIBLE : View.GONE);
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected
                ? R.drawable.bg_filter_selected
                : R.drawable.bg_filter_unselected);
        tab.setTextColor(getColor(selected ? R.color.bpsu_maroon : R.color.text_secondary));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }
}
