package com.example.bpsudrive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// ONE reusable Request Details screen.
// Default static state: Receipt Verification (Phase H).
// Receipt Verified (Phase N, Student 16) reuses this same layout:
//   - hide tvStageChip and tvFeeAmount
//   - noticeBox -> bg_info_box_green + verified_notice_title/body
//   - show tvProgressTitle + tvStepVerification (VISIBLE)
//   - tvStepSubmitted/tvStepReceipt stay green checks
//   - tvStepCurrent becomes the completed verification step
//   - tvStepAccepted becomes maroon-bold current (@string/verified_step_accepted)
//   - detail rows take verified_request/receipt/fee/purpose/remarks strings
// Request Accepted (Phase O, Student 17) reuses this same layout:
//   - tvHeaderTitle -> accepted_header; hide tvStageChip and tvFeeAmount
//   - noticeBox stays pink + accepted_notice_title/body
//   - show tvProgressTitle + tvStepVerification as green checks
//   - tvStepCurrent becomes the completed verification step
//   - tvStepAccepted takes verified_step_accepted as maroon-bold current
//   - detail rows take accepted_request, verified_receipt, verified_fee,
//     details_student, verified_purpose, accepted_remarks
// Request Processing (Phase P, Student 18) reuses this same layout:
//   - tvHeaderTitle -> processing_header; hide tvStageChip and tvFeeAmount
//   - noticeBox stays pink + processing_notice_title/body
//   - show tvProgressTitle + tvStepVerification as green checks
//   - tvStepSubmitted/tvStepReceipt stay green checks; hide tvStepCurrent
//   - tvStepAccepted takes processing_step_accepted as a green check
//   - tvStepProcessing takes processing_step_current as maroon-bold current
//   - tvStepCompleted stays inactive grey
//   - detail rows take processing_request, verified_receipt, verified_fee,
//     details_student, verified_purpose, processing_remarks
// Request Completed (Phase Q, Student 19) reuses this same layout:
//   - tvHeaderTitle -> completed_header; hide tvStageChip and tvFeeAmount
//   - noticeBox -> bg_info_box_green + completed_notice_title/body
//   - show tvProgressTitle + tvStepVerification as green checks
//   - tvStepSubmitted/tvStepReceipt stay green checks; hide tvStepCurrent
//   - tvStepAccepted takes processing_step_accepted as a green check
//   - tvStepProcessing takes completed_step_processing as a green check
//   - tvStepCompleted takes completed_step_completed as a green check
//     (all steps complete; no maroon current row)
//   - detail rows take completed_request, verified_receipt, verified_fee,
//     details_student, verified_purpose, completed_remarks
//   - btnPrimaryAction takes completed_action (Back to My Requests)
// Static sample data only. No SQLite, no status logic, no payment/receipt logic.
// Exposed IDs for later wiring:
// tvHeaderTitle, tvRequestTitle, tvRequestReference, tvStageChip,
// noticeBox, tvNoticeTitle, tvNoticeBody,
// progressContainer, tvProgressTitle, tvStepSubmitted, tvStepReceipt,
// tvStepVerification, tvStepCurrent,
// tvStepAccepted, tvStepProcessing, tvStepCompleted,
// tvStudentInfo, tvFeeAmount, tvReceiptStatus, tvRequestStatus,
// tvRequestDescription, tvReceiptInfo,
// btnPrimaryAction, btn_back
public class StudentRequestDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_details);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
