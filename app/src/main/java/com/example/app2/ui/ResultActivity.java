package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private TextView btnBack, tvUserTitle, tvConstitution, tvBmiValue, tvBmiBadge, tvDescription;
    private ProgressBar progressBmi;
    private LinearLayout layoutKeywords;
    private TextView tvNutrients; // 부족 영양소

    // 하단 탭
    private TextView btnHome, btnOpenRecord, btnOpenMyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        bindViews();

        // 뒤로가기
        btnBack.setOnClickListener(v -> onBackPressed());

        // 하단 탭 네비게이션
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        btnOpenRecord.setOnClickListener(v -> {
            Intent i = new Intent(this, HealthRecordActivity.class);
            startActivity(i);
        });
        btnOpenMyPage.setOnClickListener(v->{
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        });

        // ----- 인텐트에서 서버 응답 및 보조 값 수신 -----
        Intent i = getIntent();
        String json = i.getStringExtra("resultJson");
        if (json == null) json = i.getStringExtra("analyzeJson"); // 호환 키

        String userName   = i.getStringExtra("name");
        Float heightCmIn  = i.hasExtra("heightCm") ? i.getFloatExtra("heightCm", 0f) : null;
        Float weightKgIn  = i.hasExtra("weightKg") ? i.getFloatExtra("weightKg", 0f) : null;

        if (TextUtils.isEmpty(userName)) userName = "홍길동";
        tvUserTitle.setText(userName + " 님의 분석 결과");

        if (TextUtils.isEmpty(json)) {
            Toast.makeText(this, "결과 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ----- 파싱 변수 -----
        String constitution = "태음인";
        Integer score = null;
        String summary = null;              // description.summary
        String healthTrends = null;         // description.health_trends
        String lifeManagement = null;       // description.life_management
        Double bmiValue = null;             // 응답의 bmi.value
        String bmiCategory = null;          // 응답의 bmi.category
        ArrayList<String> keywords = new ArrayList<>();
        ArrayList<String> nutrients = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(json);

            // 체질/점수
            constitution = root.optString("constitution", constitution);
            if (root.has("score") && !root.isNull("score")) {
                score = root.optInt("score");
            }

            // 설명
            JSONObject d = root.optJSONObject("description");
            if (d != null) {
                summary        = d.optString("summary", null);
                healthTrends   = d.optString("health_trends", null);
                lifeManagement = d.optString("life_management", null);
            }

            // BMI
            JSONObject b = root.optJSONObject("bmi");
            if (b != null) {
                if (b.has("value") && !b.isNull("value")) {
                    bmiValue = b.optDouble("value");
                }
                bmiCategory = b.optString("category", null);
            }

            // 키워드
            JSONArray kArr = root.optJSONArray("keywords");
            if (kArr != null) {
                for (int idx = 0; idx < kArr.length(); idx++) {
                    String kw = kArr.optString(idx, null);
                    if (!TextUtils.isEmpty(kw)) keywords.add(kw);
                }
            }

            // 부족 영양소
            JSONArray nArr = root.optJSONArray("nutrients");
            if (nArr != null) {
                for (int idx = 0; idx < nArr.length(); idx++) {
                    String nu = nArr.optString(idx, null);
                    if (!TextUtils.isEmpty(nu)) nutrients.add(nu);
                }
            }

        } catch (JSONException e) {
            Toast.makeText(this, "결과 파싱 실패", Toast.LENGTH_SHORT).show();
        }

        // ----- 체질 바인딩 -----
        tvConstitution.setText(constitution);

        // ----- BMI 표시: 서버값 > 클라계산(height/weight) > XML초기값 -----
        Double bmiToShow = null;
        String badgeToShow = null;

        if (bmiValue != null && !bmiValue.isNaN() && bmiValue > 0) {
            bmiToShow = bmiValue;
            badgeToShow = !TextUtils.isEmpty(bmiCategory) ? bmiCategory : badgeFromBmi(bmiValue);
        } else if (heightCmIn != null && weightKgIn != null && heightCmIn > 0 && weightKgIn > 0) {
            bmiToShow = calcBmi(heightCmIn, weightKgIn);
            badgeToShow = badgeFromBmi(bmiToShow);
        }

        if (bmiToShow != null) {
            tvBmiValue.setText(String.format("%.1f", bmiToShow));
            tvBmiBadge.setText(badgeToShow);
            tintBadge(tvBmiBadge, badgeToShow);
            setBmiProgress(bmiToShow);
        }

        // ----- 설명: 서버 summary 우선, 없으면 BMI 기반 기본 설명 + 체질 팁 -----
        if (!TextUtils.isEmpty(summary)) {
            tvDescription.setText(summary + constitutionTip(constitution));
        } else if (bmiToShow != null) {
            setBmiBadgeAndDesc(bmiToShow, constitution); // 내부에서 tvDescription 설정
        }

        // ----- 키워드 칩 동적 구성 -----
        layoutKeywords.removeAllViews(); // 빈 컨테이너여야 함
        if (!keywords.isEmpty()) {
            for (String kw : keywords) {
                TextView chip = new TextView(this);
                chip.setText(kw);
                chip.setTextColor(0xFF7A7A7A);
                chip.setPadding(dp(10), dp(6), dp(10), dp(6));
                chip.setBackgroundResource(R.drawable.bg_chip_outline);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.rightMargin = dp(8);
                chip.setLayoutParams(lp);
                layoutKeywords.addView(chip);
            }
        }

        // ----- 부족 영양소 바인딩 (있을 때만 노출) -----
        if (!nutrients.isEmpty()) {
            String joined = TextUtils.join(", ", nutrients);
            tvNutrients.setText("부족 영양소: " + joined);
            tvNutrients.setVisibility(View.VISIBLE);
        } else {
            tvNutrients.setVisibility(View.GONE);
        }
    }

    private void bindViews() {
        btnBack         = findViewById(R.id.btnBack);
        tvUserTitle     = findViewById(R.id.tvUserTitle);
        tvConstitution  = findViewById(R.id.tvConstitution);
        tvBmiValue      = findViewById(R.id.tvBmiValue);
        tvBmiBadge      = findViewById(R.id.tvBmiBadge);
        tvDescription   = findViewById(R.id.tvDescription);
        progressBmi     = findViewById(R.id.progressBmi);
        layoutKeywords  = findViewById(R.id.layoutKeywords);
        tvNutrients     = findViewById(R.id.tvNutrients);

        // 바텀바 탭
        btnHome   = findViewById(R.id.btnHome);
        btnOpenRecord = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage     = findViewById(R.id.btnOpenMyPage);
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

    private String badgeFromBmi(double bmi) {
        if (bmi < 18.5) return "저체중";
        else if (bmi < 23) return "정상";
        else if (bmi < 25) return "과체중";
        else return "비만";
    }

    private void tintBadge(TextView badgeView, String badge) {
        int color;
        switch (badge) {
            case "저체중": color = 0xFF64B5F6; break; // 파랑
            case "정상":   color = 0xFF81C784; break; // 초록
            case "과체중": color = 0xFFFFB74D; break; // 주황
            default:       color = 0xFFE57373; break; // 비만=빨강
        }
        if (badgeView.getBackground() != null) {
            badgeView.getBackground().setTint(color);
        }
    }

    private void setBmiBadgeAndDesc(double bmi, String constitution) {
        String badge = badgeFromBmi(bmi);
        tintBadge(tvBmiBadge, badge);
        tvBmiBadge.setText(badge);

        String desc;
        if (bmi < 18.5) {
            desc = "현재 저체중 범위입니다. 균형 잡힌 식단과 충분한 휴식을 통해 체중을 회복하는 것이 중요합니다.";
        } else if (bmi < 23) {
            desc = "정상 체중 범위입니다. 규칙적인 운동과 식습관을 유지해 주세요.";
        } else if (bmi < 25) {
            desc = "과체중 범위입니다. 식단 조절과 가벼운 유산소 운동을 권장합니다.";
        } else {
            desc = "비만 범위입니다. 식습관 개선과 규칙적인 운동이 중요합니다.";
        }
        tvDescription.setText(desc + constitutionTip(constitution));
    }

    /** 체질별 간단 팁 한 줄 */
    private String constitutionTip(String constitution) {
        if (constitution == null) return "";
        switch (constitution) {
            case "태양인":
                return " (상체 열 관리와 규칙적인 수면이 도움됩니다.)";
            case "소양인":
                return " (과식/야식 줄이고 가벼운 유산소 위주가 좋아요.)";
            case "소음인":
                return " (따뜻한 음식과 소화에 부담 적은 식단이 좋아요.)";
            default: // 태음인
                return " (꾸준한 유산소와 기름진 음식 줄이기가 핵심입니다.)";
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
