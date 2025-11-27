package com.example.app2.ui;

import android.content.Intent;
import android.graphics.Color; // [추가] 탭 색상 변경용
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
// 💡 [삭제] import android.widget.Button; (더 이상 Button을 사용하지 않음)
import android.widget.ImageView;
import android.widget.LinearLayout; // (필요할 수 있으므로 유지)
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // [추가] 추천 상품 카드용

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.example.app2.models.AdviceResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    // [섹션 1] 상단 바 + 헤더 + BMI 카드
    private ImageView btnBack;
    private TextView tvUserTitle, tvConstitution, tvBmiValue, tvBmiBadge, tvDescription;
    private ProgressBar progressBmi;

    // [섹션 2] 맞춤 솔루션 (탭)
    private TextView tabDiet, tabExercise, tabLife;
    private View layoutDietContent, layoutExerciseContent, layoutLifeContent;
    private TextView tvNutrientDesc;
    private ImageView ivFood1, ivFood2, ivFood3;
    private TextView tvFood1, tvFood2, tvFood3;

    // [섹션 3] 추천 식품 (신규)
    private CardView recommendCard1, recommendCard2;
    private ImageView ivRecommend1, ivRecommend2;
    private TextView tvRecommendName1, tvRecommendName2;
    private TextView btnRecommendLink1, btnRecommendLink2;

    // [섹션 4] 하단 탭
    private TextView btnHome, btnOpenRecord, btnOpenMyPage;

    private String constitutionTypeEng = "taeumin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result); // 👈 제공해주신 XML을 사용

        bindViews();
        setupSolutionTabs(); // 탭 클릭 로직 설정

        // --- ▼▼▼ 클릭 리스너 설정 ▼▼▼ ---

        btnBack.setOnClickListener(v -> onBackPressed());

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        btnOpenRecord.setOnClickListener(v -> startActivity(new Intent(this, HealthRecordActivity.class)));
        btnOpenMyPage.setOnClickListener(v -> startActivity(new Intent(this, MyPageActivity.class)));

        // "추천 식품" 버튼 (쇼핑몰 링크) 클릭 리스너
        View.OnClickListener shopClickListener = v -> {
            String url = "https://allbio.kr/"; // (임시 URL, 나중에 상품별 URL로 변경)

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        btnRecommendLink1.setOnClickListener(shopClickListener);
        btnRecommendLink2.setOnClickListener(shopClickListener);

        // --- ▲▲▲ 클릭 리스너 설정 끝 ▲▲▲ ---


        // --- ▼▼▼ [기존] 데이터 파싱 및 UI 업데이트 (수정 없음) ▼▼▼ ---
        Intent i = getIntent();
        String json = i.getStringExtra("resultJson");
        if (json == null) json = i.getStringExtra("analyzeJson");

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

        String constitution = "태음인";
        Double bmiValue = null;
        String bmiCategory = null;
        String summary = null; // 💡 summary 변수 선언 (fetchAdvice에서 사용)

        try {
            JSONObject root = new JSONObject(json);

            constitutionTypeEng = root.optString("constitution", "taeumin");
            constitution = koFromEng(constitutionTypeEng);

            JSONObject d = root.optJSONObject("description");
            if (d != null) {
                summary = d.optString("summary", null);
            }
            JSONObject b = root.optJSONObject("bmi");
            if (b != null) {
                if (b.has("value") && !b.isNull("value")) {
                    bmiValue = b.optDouble("value");
                }
                bmiCategory = b.optString("category", null);
            }

        } catch (JSONException e) {
            Toast.makeText(this, "결과 파싱 실패", Toast.LENGTH_SHORT).show();
        }

        tvConstitution.setText(constitution);

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

        // [수정] summary가 JSON에 있으면 먼저 설정
        if (!TextUtils.isEmpty(summary)) {
            tvDescription.setText(summary + constitutionTip(constitutionTypeEng));
        } else if (bmiToShow != null) {
            // summary가 없을 때만 BMI 기반 기본 문구 설정
            setBmiBadgeAndDesc(bmiToShow, constitutionTypeEng);
        }

        // [수정] summary 변수를 fetchAdvice로 전달
        fetchAdvice(constitutionTypeEng, summary);
        // --- ▲▲▲ [기존] 데이터 파싱 및 UI 업데이트 끝 ▲▲▲ ---
    }

    /**
     * [신규] 맞춤 솔루션 탭의 '눈속임' 클릭 로직 (배경 교체)
     */
    private void setupSolutionTabs() {
        tabDiet.setOnClickListener(v -> {
            selectTab(tabDiet);
            deselectTab(tabExercise);
            deselectTab(tabLife);

            layoutDietContent.setVisibility(View.VISIBLE);
            layoutExerciseContent.setVisibility(View.GONE);
            layoutLifeContent.setVisibility(View.GONE);
        });

        tabExercise.setOnClickListener(v -> {
            deselectTab(tabDiet);
            selectTab(tabExercise);
            deselectTab(tabLife);

            layoutDietContent.setVisibility(View.GONE);
            layoutExerciseContent.setVisibility(View.VISIBLE);
            layoutLifeContent.setVisibility(View.GONE);
        });

        tabLife.setOnClickListener(v -> {
            deselectTab(tabDiet);
            deselectTab(tabExercise);
            selectTab(tabLife);

            layoutDietContent.setVisibility(View.GONE);
            layoutExerciseContent.setVisibility(View.GONE);
            layoutLifeContent.setVisibility(View.VISIBLE);
        });

        // 초기 상태: 식단 탭 선택
        selectTab(tabDiet);
        deselectTab(tabExercise);
        deselectTab(tabLife);
    }

    // [신규] 탭 선택 시 UI (Drawable 배경 적용)
    private void selectTab(TextView tab) {
        tab.setTextColor(Color.BLACK);
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
        tab.setBackgroundResource(R.drawable.bg_solution_tab_selected_white);
    }

    // [신규] 탭 선택 해제 시 UI (투명 배경 적용)
    private void deselectTab(TextView tab) {
        tab.setTextColor(Color.parseColor("#8A8A8A")); // 회색
        tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        tab.setBackgroundResource(android.R.color.transparent);
    }

    /**
     * [수정] fetchAdvice: JSON의 summary가 있으면 API의 summary를 덮어쓰지 않도록 수정
     */
    private void fetchAdvice(String constitutionType, String jsonSummary) {
        if (TextUtils.isEmpty(constitutionType)) return;

        ApiService api = ApiClient.getApiService(this);
        api.getAdviceByType(constitutionType).enqueue(new Callback<AdviceResponse>() {
            @Override public void onResponse(Call<AdviceResponse> call, Response<AdviceResponse> res) {
                if (!res.isSuccessful() || res.body() == null) return;

                AdviceResponse body = res.body();
                StringBuilder sb = new StringBuilder();

                // [수정] JSON에서 파싱한 summary가 *없을* 경우에만 API의 summary를 사용
                if (TextUtils.isEmpty(jsonSummary) && !TextUtils.isEmpty(body.summary)) {
                    sb.append(body.summary);
                }

                if (!TextUtils.isEmpty(body.health_trends)) {
                    if (sb.length() > 0) sb.append("\n\n");
                    sb.append("• 건강 경향: ").append(body.health_trends);
                }
                if (!TextUtils.isEmpty(body.life_management)) {
                    if (sb.length() > 0) sb.append("\n\n");
                    sb.append("• 생활 관리: ").append(body.life_management);
                }

                if (sb.length() > 0) {
                    // [수정] JSON summary가 없는 경우에만 API 텍스트로 덮어씀
                    if (TextUtils.isEmpty(jsonSummary)) {
                        sb.append(constitutionTip(constitutionType));
                        tvDescription.setText(sb.toString());
                    } else {
                        // (JSON summary가 이미 있으므로 API 내용은 덧붙이지 않음)
                        // (또는, 덧붙이고 싶다면: tvDescription.append("\n\n" + sb.toString()); )
                    }
                }
            }
            @Override public void onFailure(Call<AdviceResponse> call, Throwable t) {}
        });
    }

    /**
     * [최종] XML 레이아웃의 모든 ID를 연결합니다.
     */
    private void bindViews() {
        // 섹션 1
        btnBack         = findViewById(R.id.btnBack);
        tvUserTitle     = findViewById(R.id.tvUserTitle);
        tvConstitution  = findViewById(R.id.tvConstitution);
        tvBmiValue      = findViewById(R.id.tvBmiValue);
        tvBmiBadge      = findViewById(R.id.tvBmiBadge);
        tvDescription   = findViewById(R.id.tvDescription);
        progressBmi     = findViewById(R.id.progressBmi);

        // 섹션 2
        tabDiet             = findViewById(R.id.tabDiet);
        tabExercise         = findViewById(R.id.tabExercise);
        tabLife             = findViewById(R.id.tabLife);
        layoutDietContent   = findViewById(R.id.layoutDietContent);
        layoutExerciseContent = findViewById(R.id.layoutExerciseContent);
        layoutLifeContent   = findViewById(R.id.layoutLifeContent);
        tvNutrientDesc      = findViewById(R.id.tvNutrientDesc);
        ivFood1             = findViewById(R.id.ivFood1);
        ivFood2             = findViewById(R.id.ivFood2);
        ivFood3             = findViewById(R.id.ivFood3);
        tvFood1             = findViewById(R.id.tvFood1);
        tvFood2             = findViewById(R.id.tvFood2);
        tvFood3             = findViewById(R.id.tvFood3);

        // 섹션 3
        recommendCard1      = findViewById(R.id.recommendCard1);
        ivRecommend1        = findViewById(R.id.ivRecommend1);
        tvRecommendName1    = findViewById(R.id.tvRecommendName1);
        btnRecommendLink1   = findViewById(R.id.btnRecommendLink1);
        recommendCard2      = findViewById(R.id.recommendCard2);
        ivRecommend2        = findViewById(R.id.ivRecommend2);
        tvRecommendName2    = findViewById(R.id.tvRecommendName2);
        btnRecommendLink2   = findViewById(R.id.btnRecommendLink2);

        // 섹션 4
        btnHome         = findViewById(R.id.btnHome);
        btnOpenRecord   = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage   = findViewById(R.id.btnOpenMyPage);
    }

    // --- ▼▼▼ [기존] 헬퍼 메서드 (수정 없음) ▼▼▼ ---
    private double calcBmi(float heightCm, float weightKg) {
        double h = heightCm / 100.0;
        return weightKg / (h * h);
    }

    private void setBmiProgress(double bmi) {
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
            case "저체중": color = 0xFF4FC3F7; break; // 파랑
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

        // [수정] JSON summary가 없을 때만 이 기본 문구를 설정
        if (TextUtils.isEmpty(tvDescription.getText())) {
            tvDescription.setText(desc + constitutionTip(constitution));
        }
    }

    private String constitutionTip(String constitution) {
        if (constitution == null) return "";
        switch (constitution) {
            case "taeyangin":
                return " (상체 열 관리와 규칙적인 수면이 도움됩니다.)";
            case "soyangin":
                return " (과식/야식 줄이고 가벼운 유산소 위주가 좋아요.)";
            case "souemin":
                return " (따뜻한 음식과 소화에 부담 적은 식단이 좋아요.)";
            default: // taeumin
                return " (꾸준한 유산소와 기름진 음식 줄이기가 핵심입니다.)";
        }
    }

    private String koFromEng(String constitutionEng) {
        switch (constitutionEng) {
            case "taeyangin": return "태양인";
            case "soyangin": return "소양인";
            case "souemin": return "소음인";
            case "taeumin":
            default:
                return "태음인";
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}