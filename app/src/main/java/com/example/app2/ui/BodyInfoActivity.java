package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

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
            String hStr = etHeight.getText().toString().trim();
            String wStr = etWeight.getText().toString().trim();

            float heightCm = 0f, weightKg = 0f;
            try { heightCm = Float.parseFloat(hStr); } catch (Exception ignore) {}
            try { weightKg = Float.parseFloat(wStr); } catch (Exception ignore) {}

            if (heightCm <= 0 || weightKg <= 0) {
                // 간단 가드 (선택)
                return;
            }

            Intent intent = new Intent(BodyInfoActivity.this, QuestionsSingleActivity.class);
            intent.putExtra("heightCm", heightCm);
            intent.putExtra("weightKg", weightKg);
            startActivity(intent);
        });

        // 뒤로가기 → 앱 종료
    }

    private void validateForm() {
        boolean ok = etHeight.getText().toString().trim().length() > 0
                && etWeight.getText().toString().trim().length() > 0;
        btnNext.setEnabled(ok);
        btnNext.setAlpha(ok ? 1f : 0.5f);
    }
}