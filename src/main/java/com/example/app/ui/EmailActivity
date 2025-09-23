package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

public class EmailActivity extends AppCompatActivity {

    private TextView btnBack, tvTimer;
    private EditText etPhone, etCode;
    private Button btnRequestCode, btnSubmit;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        bindViews();
        setupBack();
        setupCodeRequest();
        setupFormValidation();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTimer = findViewById(R.id.tvTimer);

        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);

        btnRequestCode = findViewById(R.id.btnRequestCode);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupBack() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // 인증번호 요청(추후 추가예정) + 3분 타이머
    private void setupCodeRequest() {
        btnRequestCode.setOnClickListener(v -> {
            // 서버에 인증번호 요청 API 연동
            startTimer(3 * 60); // 3분
        });
    }

    private void startTimer(int seconds) {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override public void onTick(long ms) {
                int s = (int) (ms / 1000);
                tvTimer.setText(String.format("%02d:%02d", s / 60, s % 60));
            }
            @Override public void onFinish() {
                tvTimer.setText("00:00");
            }
        }.start();
    }

    // 폼 검증
    private void setupFormValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validate(); }
            @Override public void afterTextChanged(Editable s) { validate(); }
        };
        etPhone.addTextChangedListener(watcher);
        etCode.addTextChangedListener(watcher);

        btnSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(EmailActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        validate();
    }

    private void validate() {
        boolean ok = etPhone.getText().length() > 0
                && etCode.getText().length() > 0;

        btnSubmit.setEnabled(ok);
        btnSubmit.setAlpha(ok ? 1f : 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
