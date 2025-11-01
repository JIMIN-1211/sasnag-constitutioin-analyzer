package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

public class MyPageActivity extends AppCompatActivity {

    // 1. 뷰 변수 선언
    private ImageView btnBack;
    private Button btnLogout;
    private TextView tvUserName, tvUserEmail;

    // 메뉴 아이템들
    private TextView btnDiagnosisHistory, btnRediagnosis, btnChangePassword,
            btnSocialLink, btnNotice, btnInquiry, btnTerms, btnWithdraw;

    // 하단 탭
    private TextView btnHome, btnOpenRecord, btnOpenMyPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        bindViews();  // 2. 뷰 바인딩
        setupClicks(); // 3. 클릭 리스너 설정

        loadUserData(); // 4. 사용자 정보 로드 (예시)
    }

    /**
     * XML의 뷰들을 자바 변수와 연결합니다.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        // 메뉴
        btnDiagnosisHistory = findViewById(R.id.btnDiagnosisHistory);
        btnRediagnosis = findViewById(R.id.btnRediagnosis);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSocialLink = findViewById(R.id.btnSocialLink);
        btnNotice = findViewById(R.id.btnNotice);
        btnInquiry = findViewById(R.id.btnInquiry);
        btnTerms = findViewById(R.id.btnTerms);
        btnWithdraw = findViewById(R.id.btnWithdraw);

        // 하단 탭
        btnHome = findViewById(R.id.btnHome);
        btnOpenRecord = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage = findViewById(R.id.btnOpenMyPage);
    }

    /**
     * 클릭 가능한 뷰들에 리스너를 설정합니다.
     */
    private void setupClicks() {
        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 로그아웃
        btnLogout.setOnClickListener(v ->
                Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show());

        // 메뉴 아이템 (임시로 Toast)
        btnDiagnosisHistory.setOnClickListener(v ->
                Toast.makeText(this, "체질 진단 내역", Toast.LENGTH_SHORT).show());
        btnRediagnosis.setOnClickListener(v ->
                Toast.makeText(this, "체질 다시 진단하기", Toast.LENGTH_SHORT).show());
        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(this, "비밀번호 변경", Toast.LENGTH_SHORT).show());
        btnSocialLink.setOnClickListener(v ->
                Toast.makeText(this, "소셜 계정 연동", Toast.LENGTH_SHORT).show());
        btnNotice.setOnClickListener(v ->
                Toast.makeText(this, "공지사항", Toast.LENGTH_SHORT).show());
        btnInquiry.setOnClickListener(v ->
                Toast.makeText(this, "문의하기", Toast.LENGTH_SHORT).show());
        btnTerms.setOnClickListener(v ->
                Toast.makeText(this, "서비스 약관", Toast.LENGTH_SHORT).show());
        btnWithdraw.setOnClickListener(v ->
                Toast.makeText(this, "계정 탈퇴", Toast.LENGTH_SHORT).show());

        // 하단 탭 (다른 액티비티로 이동)
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnOpenRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, HealthRecordActivity.class);
            startActivity(intent);
        });

        btnOpenMyPage.setOnClickListener(v ->
                Toast.makeText(this, "현재 화면입니다.", Toast.LENGTH_SHORT).show());
    }

    /**
     * (예시) 로그인된 사용자 정보를 불러와 뷰에 설정합니다.
     */
    private void loadUserData() {
        // TODO: 실제로는 로그인 세션이나 DB에서 사용자 정보를 가져와야 합니다.
        tvUserName.setText("홍길동 님");
        tvUserEmail.setText("user@example.com");
    }
}