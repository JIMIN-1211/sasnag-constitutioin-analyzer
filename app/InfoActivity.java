package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    private TextView btnBack, tvTimer;
    private EditText etName, etPhone, etCode, etBirthYear, etPassword;
    private Button btnRequestCode, btnMale, btnFemale, btnNone, btnSubmit;

    private String gender = "male"; // 기본 선택 남성
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        bindViews();
        setupBack();
        setupGenderButtons();
        setupCodeRequest();
        setupFormValidation();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTimer = findViewById(R.id.tvTimer);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);
        etBirthYear = findViewById(R.id.etBirthYear);
        etPassword = findViewById(R.id.etPassword);

        btnRequestCode = findViewById(R.id.btnRequestCode);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnNone = findViewById(R.id.btnNone);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupBack() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // 성별 토글 (간단히 색만 교체)
    private void setupGenderButtons() {
        View.OnClickListener sel = v -> {
            if (v.getId() == R.id.btnMale) gender = "male";
            else if (v.getId() == R.id.btnFemale) gender = "female";
            else gender = "none";
            applyGenderStyle();
        };
        btnMale.setOnClickListener(sel);
        btnFemale.setOnClickListener(sel);
        btnNone.setOnClickListener(sel);

        applyGenderStyle(); // 초기 적용
    }

    private void applyGenderStyle() {
        // 선택된 것만 초록색, 나머지는 회색
        styleChip(btnMale, "male".equals(gender));
        styleChip(btnFemale, "female".equals(gender));
        styleChip(btnNone, "none".equals(gender));
    }

    private void styleChip(Button b, boolean selected) {
        if (selected) {
            b.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            b.setTextColor(getColor(android.R.color.white));
        } else {
            b.setBackgroundTintList(null);
            b.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            b.setTextColor(0xFF333333);
        }
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
        etName.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etCode.addTextChangedListener(watcher);
        etBirthYear.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        btnSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // 뒤로가기로 가입화면 안 돌아오도록
        });

        validate();
    }

    private void validate() {  //일단 길이는 최소한으로 맞춰놓으니 나중에 고쳐놓을 것
        boolean ok = etName.getText().length() > 0
                && etPhone.getText().length() > 0
                && etCode.getText().length() > 0
                && etBirthYear.getText().length() > 0
                && etPassword.getText().length() > 0;

        btnSubmit.setEnabled(ok);
        btnSubmit.setAlpha(ok ? 1f : 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
