package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class BodyInfoActivity extends AppCompatActivity {

    private EditText etHeight, etWeight;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_info);

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnNext = findViewById(R.id.btnNext);

        // 입력 확인
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);

        btnNext.setOnClickListener(v -> {
            // 입력값 가져오기
            String height = etHeight.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();

            // 질문 페이지로 이동
            Intent intent = new Intent(BodyInfoActivity.this, QuestionActivity.class);
            intent.putExtra("height", height);
            intent.putExtra("weight", weight);
            startActivity(intent);
            finish();
        });

        // 뒤로가기 → 앱 종료
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
    }

    private void validateForm() {
        boolean ok = etHeight.getText().toString().trim().length() > 0
                && etWeight.getText().toString().trim().length() > 0;
        btnNext.setEnabled(ok);
        btnNext.setAlpha(ok ? 1f : 0.5f);
    }
}
