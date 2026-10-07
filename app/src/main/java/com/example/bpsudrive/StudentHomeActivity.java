package com.example.bpsudrive;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

// UI-only Falcon greeting animation. No auth, no DB, no business logic.
// Greeting TextView (R.id.tv_greeting) is exposed for later SessionManager wiring:
//   TextView greeting = findViewById(R.id.tv_greeting);
//   greeting.setText("Magandang araw, " + studentName + "!");
public class StudentHomeActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View speechDialog;
    private View typing;
    private View message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        View falcon = findViewById(R.id.falcon_image);
        speechDialog = findViewById(R.id.speech_dialog);
        typing = findViewById(R.id.tv_typing);
        message = findViewById(R.id.tv_falcon_message);

        // Initial: hide dialog pieces, show greeting only at the end of intro.
        speechDialog.setVisibility(View.INVISIBLE);
        typing.setVisibility(View.GONE);
        message.setVisibility(View.GONE);

        // Falcon appears / settles.
        Animation falconAnim = AnimationUtils.loadAnimation(this, R.anim.falcon_settle);
        falcon.startAnimation(falconAnim);

        // Speech dialog fades/slides in.
        handler.postDelayed(() -> {
            speechDialog.setVisibility(View.VISIBLE);
            speechDialog.startAnimation(AnimationUtils.loadAnimation(this, R.anim.speech_in));
        }, 400);

        // State 1 Greeting remains ~1.8s, then State 2 Typing ~1.0s.
        handler.postDelayed(() -> {
            typing.setVisibility(View.VISIBLE);
            Animation blink = AnimationUtils.loadAnimation(this, R.anim.typing_blink);
            typing.startAnimation(blink);
        }, 2200);

        // State 3 Message: typing -> final message, remain visible.
        handler.postDelayed(() -> {
            typing.clearAnimation();
            typing.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            message.startAnimation(AnimationUtils.loadAnimation(this, R.anim.speech_in));
        }, 3200);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
