package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

import java.util.Calendar;

public class InfoActivity extends AppCompatActivity {

    private TextView btnBack, tvPasswordMatch;
    private EditText etId, etPassword, etPasswordConfirm;
    private EditText etName, etBirthYear;
    private RadioGroup rgGender;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        bindViews();
        wireEvents();
        setupFormValidation();
        validate();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        etId = findViewById(R.id.etId);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        tvPasswordMatch = findViewById(R.id.tvPasswordMatch);
        btnSubmit = findViewById(R.id.btnSubmit);

        etName = findViewById(R.id.etName);
        etBirthYear = findViewById(R.id.etBirthYear);
        rgGender = findViewById(R.id.rgGender);
    }

    private void wireEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String username = etId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name     = etName.getText().toString().trim();
            int birthYear   = safeInt(etBirthYear.getText().toString().trim());
            String gender   = getSelectedGender();

            Intent i = new Intent(InfoActivity.this, EmailActivity.class);
            i.putExtra("username", username);
            i.putExtra("password", password);
            i.putExtra("name", name);
            i.putExtra("birth_year", birthYear);
            i.putExtra("gender", gender);
            startActivity(i);
        });
    }

    private String getSelectedGender() {
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMale) return "male";
        if (checkedId == R.id.rbFemale) return "female";
        return null;
    }

    private int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void highlightPasswordMatch(boolean match) {
        if (etPassword.length() > 0 || etPasswordConfirm.length() > 0) {
            if (match) {
                tvPasswordMatch.setText("비밀번호가 일치합니다");
                tvPasswordMatch.setTextColor(getColor(android.R.color.holo_green_dark));
            } else {
                tvPasswordMatch.setText("비밀번호가 일치하지 않습니다");
                tvPasswordMatch.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        } else {
            tvPasswordMatch.setText("");
        }
    }

    // 폼 검증
    private void setupFormValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validate(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etId.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etPasswordConfirm.addTextChangedListener(watcher);
        etName.addTextChangedListener(watcher);
        etBirthYear.addTextChangedListener(watcher);

        rgGender.setOnCheckedChangeListener((group, checkedId) -> validate());
    }

    private void validate() {
        String id   = etId.getText().toString().trim();
        String p1   = etPassword.getText().toString();
        String p2   = etPasswordConfirm.getText().toString();
        String name = etName.getText().toString().trim();

        String birthTxt = etBirthYear.getText().toString().trim();
        int by = safeInt(birthTxt);

        String gender = getSelectedGender();

        boolean hasId   = id.length() >= 0;     //일단 0으로
        boolean hasPw   = p1.length() >= 0;     //일단 0으로
        boolean match   = hasPw && p1.equals(p2);
        highlightPasswordMatch(match);

        boolean hasName = name.length() > 0;

        int yearNow = currentYear();
        boolean birthLenOk = birthTxt.length() == 0;    //일단 0으로
        boolean birthNumOk = birthLenOk && android.text.TextUtils.isDigitsOnly(birthTxt);

        boolean birthOk = birthTxt.length() > 0;        //일단 0으로

        boolean genderOk = (gender != null);

        boolean ok = hasId && match && hasName && birthOk && genderOk;

        btnSubmit.setEnabled(ok);
        btnSubmit.setAlpha(ok ? 1f : 0.5f);
    }

}
