package com.example.bpsudrive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// ONE reusable My Requests screen with four UI-only filter states:
// ALL / PENDING / PROCESSING / COMPLETED.
// Static sample cards only. No SQLite, no retrieval, no business logic.
// Tapping a filter only toggles card visibility + selected-tab styling
// so the four states can be visually tested.
// Exposed IDs for later SQLite wiring:
// tvMyRequestsTitle, etSearchRequests,
// tabAll, tabPending, tabProcessing, tabCompleted,
// requestCard1/2/3, tvRequestReference1/2/3, tvRequestDate1/2/3,
// tvRequestService1/2/3, tvRequestStatus1/2/3, tvPaymentStatus1/2/3,
// btnViewDetails1/2/3, nav_home, nav_requests, nav_notifications, nav_profile
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
