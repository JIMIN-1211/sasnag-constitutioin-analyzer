package com.example.app2.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // (사용되지 않지만 원본에 있어 유지)

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // 뷰 변수
    private TextView tvHeadline;
    private TextView btnOpenRecord, btnOpenMyPage, btnHome;
    private TextView tvScoreExercise;
    private TextView tvScoreDiet;
    private TextView tvScoreSleep;
    private ImageView ivBannerImage;
    private TextView tvGuideCardTitle;
    private TextView tvGuideCardDesc;

    public static final String PREFS_NAME = "AppPrefs";
    public static final String KEY_HAS_SEEN_WELCOME = "hasSeenWelcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiClient.init(getApplicationContext());

        bindViews();
        setupClicks();

        // [제거] checkWelcomePopup();
        // -> 이 호출은 loadHomeData()의 서버 응답 시점으로 이동합니다.
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData(); // 홈 데이터 불러오기
    }

    /** 💡 [수정] 새 XML 레이아웃에 맞게 뷰 바인딩 */
    private void bindViews() {
        // 상단 헤더
        tvHeadline = findViewById(R.id.tvHeadline);

        // 하단 탭
        btnOpenRecord = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage = findViewById(R.id.btnOpenMyPage);
        btnHome = findViewById(R.id.btnHome);

        // "목표" (건강 점수)
        tvScoreExercise = findViewById(R.id.tvScoreExercise);
        tvScoreDiet = findViewById(R.id.tvScoreDiet);
        tvScoreSleep = findViewById(R.id.tvScoreSleep);

        // [추가] 광고 배너
        ivBannerImage = findViewById(R.id.ivBannerImage);

        // [추가] 오늘의 생활 가이드
        tvGuideCardTitle = findViewById(R.id.tvGuideCardTitle);
        tvGuideCardDesc = findViewById(R.id.tvGuideCardDesc);
    }

    /** 💡 [수정] 새 XML 레이아웃에 맞게 클릭 리스너 설정 */
    private void setupClicks() {
        // 하단 탭
        btnOpenRecord.setOnClickListener(v -> {
            Intent i = new Intent(this, HealthRecordActivity.class);
            startActivity(i);
        });
        btnOpenMyPage.setOnClickListener(v->{
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        });
        btnHome.setOnClickListener(v -> Toast.makeText(this, "현재 화면입니다", Toast.LENGTH_SHORT).show());


        // 배너
        ivBannerImage.setOnClickListener(v -> {
            String url = "https://allbio.kr/"; // (임시 링크)
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadHomeData() {
        ApiService api = ApiClient.getApiService(this);
        api.getHomeData(null).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "Home data loaded: " + response.body().toString());

                    // --- 💡 [수정] 팝업 로직 시작 ---
                    boolean hasConstitution = false;
                    try {
                        JsonObject data = response.body();
                        // (원본 코드의 'constituion' 철자를 그대로 따름)
                        if (data.has("user_info")) {
                            JsonObject userInfo = data.getAsJsonObject("user_info");
                            if (userInfo.has("constituion") && !userInfo.get("constituion").isJsonNull()) {
                                String constitution = userInfo.get("constituion").getAsString();
                                // 실제 체질 값이 있는지 확인
                                if (constitution != null && !constitution.isEmpty() && !constitution.equalsIgnoreCase("null")) {
                                    hasConstitution = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error parsing constitution for popup check", e);
                    }

                    // DB에서 가져온 체질 정보 유무를 바탕으로 팝업 체크
                    checkWelcomePopup(hasConstitution);
                    // --- 💡 [수정] 팝업 로직 종료 ---

                    updateUI(response.body());

                } else {
                    Log.e("MainActivity", "Home data load failed: " + response.code());

                    if (response.code() == 400) { // 신규 유저

                        //400 오류(신규 유저)는 체질 정보가 없으므로 'false'를 전달
                        checkWelcomePopup(false);

                        updateUI(null); // 0으로 리셋
                    } else if (response.code() == 401) {
                        Toast.makeText(MainActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                        // TODO: 로그인 화면으로 강제 이동
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MainActivity", "Home data network error: " + t.getMessage());
            }
        });
    }


    /** 💡 [수정] 새 XML 레이아웃에 맞게 UI 업데이트 */
    private void updateUI(JsonObject data) {

        // [신규] data가 null일 경우(400 오류) 모든 값을 0 또는 기본값으로 리셋
        if (data == null) {
            tvHeadline.setText("사용자 님,");
            tvScoreDiet.setText("0 점");
            tvScoreExercise.setText("0 점");
            tvScoreSleep.setText("0 점");
            tvGuideCardTitle.setText("[가이드 제목]");
            tvGuideCardDesc.setText("신체 정보를 입력하면 맞춤 가이드가 제공됩니다.");
            return;
        }

        try {
            // 1. 유저 정보 업데이트
            if (tvHeadline != null && data.has("user_info")) {
                JsonObject userInfo = data.getAsJsonObject("user_info");
                String name = "사용자";
                String constitution = "체질";
                if (userInfo.has("name") && !userInfo.get("name").isJsonNull()) {
                    name = userInfo.get("name").getAsString();
                }
                // (원본 코드의 'constituion' 철자를 그대로 따름)
                if (userInfo.has("constituion") && !userInfo.get("constituion").isJsonNull()) {
                    constitution = userInfo.get("constituion").getAsString();
                }
                tvHeadline.setText(constitution + " " + name + "님,");
            }

            // 2. "목표" (건강 점수) 패널 업데이트
            int mealScore = 0;
            int sleepScore = 0;
            int exerciseScore = 0;

            if (data.has("today_report") && !data.get("today_report").isJsonNull()) {
                JsonObject report = data.getAsJsonObject("today_report");

                if (report.has("health_balance") && !report.get("health_balance").isJsonNull()) {
                    JsonObject balance = report.getAsJsonObject("health_balance");
                    if (balance.has("meal") && !balance.get("meal").isJsonNull()) {
                        mealScore = balance.get("meal").getAsInt();
                    }
                    if (balance.has("sleep") && !balance.get("sleep").isJsonNull()) {
                        sleepScore = balance.get("sleep").getAsInt();
                    }
                    if (balance.has("exercise") && !balance.get("exercise").isJsonNull()) {
                        exerciseScore = balance.get("exercise").getAsInt();
                    }
                }
            }

            tvScoreDiet.setText(String.format(Locale.KOREA, "%d 점", mealScore));
            tvScoreExercise.setText(String.format(Locale.KOREA, "%d 점", exerciseScore));
            tvScoreSleep.setText(String.format(Locale.KOREA, "%d 점", sleepScore));


            // 💡 [신규] 3. "오늘의 생활 가이드" 카드 업데이트
            if (data.has("health_tips") && data.get("health_tips").isJsonArray()) {
                JsonArray tips = data.getAsJsonArray("health_tips");
                if (tips.size() > 0) {
                    JsonObject guideTip = (tips.size() > 1) ? tips.get(1).getAsJsonObject() : tips.get(0).getAsJsonObject();
                    String title = guideTip.get("title").getAsString();
                    String content = guideTip.get("content").getAsString();

                    if (tvGuideCardTitle != null) {
                        tvGuideCardTitle.setText(title);
                    }
                    if (tvGuideCardDesc != null) {
                        tvGuideCardDesc.setText(content);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("MainActivity_UI", "Error parsing home data JSON", e);
            Toast.makeText(this, "데이터 표시에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    /** 💡 [수정] DB 결과를 인자로 받도록 메서드 시그니처 및 로직 변경 */
    private void checkWelcomePopup(boolean dbHasConstitution) {

        // 1. [DB 우선] 서버 DB에 이미 체질 결과가 있는 경우
        if (dbHasConstitution) {
            // 팝업을 띄울 필요가 없습니다.
            // (혹시 모를 상황을 대비해, 로컬에도 '봤음'으로 기록 동기화)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_HAS_SEEN_WELCOME, true).apply();
            return; // 팝업 로직 종료
        }

        // 2. [로컬 확인] DB에 결과가 없는 경우 (신규 유저 또는 '홈으로' 누른 유저)
        //    -> 로컬 SharedPreferences를 확인하여 '팝업을 본 기록'이 있는지 체크
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSeenWelcomeLocally = prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false);

        if (!hasSeenWelcomeLocally) {
            // DB에도 결과가 없고 (dbHasConstitution == false)
            // 로컬에도 팝업 본 기록이 없으면 (!hasSeenWelcomeLocally)
            // -> [결론] 팝업을 띄웁니다.
            showWelcomeDialog();
        }
        // (만약 DB에 결과가 없지만 로컬에 본 기록이 있다면(true),
        //  사용자가 '홈으로' 버튼을 눌렀던 것이므로 팝업을 띄우지 않고 넘어갑니다.)
    }


    // (showWelcomeDialog 메서드는 수정 없이 그대로 둡니다)
    private void showWelcomeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_welcome);
        dialog.setCancelable(false);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("환영합니다!");
        Button btnStart = dialog.findViewById(R.id.btnStartQuestion);
        Button btnHome  = dialog.findViewById(R.id.btnGoHome);

        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                prefs.edit().putBoolean(KEY_HAS_SEEN_WELCOME, true).apply();
                Intent intent = new Intent(MainActivity.this, BodyInfoActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });
        }
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                prefs.edit().putBoolean(KEY_HAS_SEEN_WELCOME, true).apply();
                dialog.dismiss();
            });
        }
        dialog.show();
    }
}