package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Services screen. No DB, no retrieval logic.
// Exposed IDs for later wiring:
// et_search_services, card_certificate, card_document, card_id_concern,
// card_inquiry, btn_view_certificate, btn_view_document, btn_view_id,
// btn_view_inquiry, btn_back
public class StudentServicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_services);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
