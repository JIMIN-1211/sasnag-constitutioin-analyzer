package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button kakaoBtn, googleBtn, naverBtn, idBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 첫 번째 화면 XML

        kakaoBtn = findViewById(R.id.btnKakaoLogin);
        googleBtn = findViewById(R.id.btnGoogleLogin);
        naverBtn = findViewById(R.id.btnNaverLogin);
        idBtn = findViewById(R.id.btnIdLogin);

        // 버튼 클릭 시 약관동의 화면으로 이동
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, AgreementActivity.class);
                startActivity(intent);
            }
        };

        kakaoBtn.setOnClickListener(listener);
        googleBtn.setOnClickListener(listener);
        naverBtn.setOnClickListener(listener);
        idBtn.setOnClickListener(listener);
    }
}
