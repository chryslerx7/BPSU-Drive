package com.example.bpsudrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class StudentLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);
        findViewById(R.id.btn_create_account).setOnClickListener(v ->
                startActivity(new Intent(this, StudentRegisterActivity.class)));
    }
}
