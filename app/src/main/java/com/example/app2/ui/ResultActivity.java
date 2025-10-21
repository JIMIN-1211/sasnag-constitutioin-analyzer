package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

public class ResultActivity extends AppCompatActivity {

    private TextView btnBack, tvUserTitle, tvConstitution, tvBmiValue, tvBmiBadge, tvDescription;
    private ProgressBar progressBmi;
    private Button btnSeeProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        bindViews();
        setupBack();

        // 전달값 받기 (없으면 기본값 사용)
        Intent i = getIntent();
        String name = i.getStringExtra("name");
        String constitution = i.getStringExtra("constitution"); // "태음인","태양인","소양인","소음인"
        float heightCm = i.getFloatExtra("heightCm", 170f);
        float weightKg = i.getFloatExtra("weightKg", 65f);

        // UI 채우기
        tvUserTitle.setText((name == null ? "홍길동" : name) + " 님의 분석 결과");
        tvConstitution.setText(constitution == null ? "태음인 (太陰人)" : constitution);

        double bmi = calcBmi(heightCm, weightKg);
        tvBmiValue.setText(String.format("%.1f", bmi));
        setBmiProgress(bmi);
        setBmiBadgeAndDesc(bmi, constitution);

        btnSeeProducts.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        tvUserTitle = findViewById(R.id.tvUserTitle);
        tvConstitution = findViewById(R.id.tvConstitution);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiBadge = findViewById(R.id.tvBmiBadge);
        tvDescription = findViewById(R.id.tvDescription);
        progressBmi = findViewById(R.id.progressBmi);
        btnSeeProducts = findViewById(R.id.btnSeeProducts);
    }

    private void setupBack() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private double calcBmi(float heightCm, float weightKg) {
        double h = heightCm / 100.0;
        return weightKg / (h * h);
    }

    private void setBmiProgress(double bmi) {
        // ProgressBar max=400 → bmi*10 으로 소수점 표현
        int p = (int) Math.max(0, Math.min(400, Math.round(bmi * 10)));
        progressBmi.setProgress(p);
    }

    private void setBmiBadgeAndDesc(double bmi, String constitution) {
        String badge;
        int badgeColor;   // 뱃지 배경색 바꾸고 싶으면 drawable 여러개 대신 tint 사용
        String desc;

        if (bmi < 18.5) {
            badge = "저체중";
            badgeColor = 0xFF64B5F6; // 파랑
            desc = "현재 저체중 범위입니다. 균형 잡힌 식단과 충분한 휴식을 통해 체중을 회복하는 것이 중요합니다.";
        } else if (bmi < 23) {
            badge = "정상";
            badgeColor = 0xFF81C784; // 초록
            desc = "정상 체중 범위입니다. 규칙적인 운동과 식습관을 유지해 주세요.";
        } else if (bmi < 25) {
            badge = "과체중";
            badgeColor = 0xFFFFB74D; // 주황
            desc = "과체중 범위입니다. 식단 조절과 가벼운 유산소 운동을 권장합니다.";
        } else {
            badge = "비만";
            badgeColor = 0xFFE57373; // 빨강
            desc = "비만 범위입니다. 식습관 개선과 규칙적인 운동이 중요합니다.";
        }

        // 체질에 따라 문구에 한 줄 가미 (간단 예시)
        String cons = constitution == null ? "태음인" : constitution;
        String tip;
        switch (cons) {
            case "태양인":
                tip = " (태양인은 상체 열 관리와 규칙적인 수면이 도움됩니다.)";
                break;
            case "소양인":
                tip = " (소양인은 과식/야식 줄이고 가벼운 유산소 위주가 좋아요.)";
                break;
            case "소음인":
                tip = " (소음인은 따뜻한 음식과 소화에 부담 적은 식단이 좋아요.)";
                break;
            default: // 태음인
                tip = " (태음인은 꾸준한 유산소와 기름진 음식 줄이기가 핵심입니다.)";
                break;
        }

        tvBmiBadge.setText(badge);
        tvBmiBadge.getBackground().setTint(badgeColor);
        tvDescription.setText(desc + tip);
    }
}
