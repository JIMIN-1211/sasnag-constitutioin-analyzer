package com.example.app2.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // [수정됨] 날짜 관련 뷰 제거
    private TextView tvHeadline;
    private TextView btnOpenRecord, btnOpenBodyInfo, btnOpenMyPage, btnHome;

    private TextView tvScoreExercise;
    private TextView tvTodayWorkoutMinutes;
    private TextView tvTodayMealCalories;

    // [수정됨] 날짜 계산용 cal 변수 제거
    // private final Calendar cal = Calendar.getInstance();

    public static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_HAS_SEEN_WELCOME = "hasSeenWelcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiClient.init(getApplicationContext());

        // ===== 뷰 바인딩 =====
        // [수정됨] 날짜 관련 뷰 바인딩 제거
        tvHeadline = findViewById(R.id.tvHeadline);
        btnOpenRecord = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage = findViewById(R.id.btnOpenMyPage);
        btnOpenBodyInfo = findViewById(R.id.btnOpenBodyInfo);
        btnHome = findViewById(R.id.btnHome);

        tvScoreExercise = findViewById(R.id.tvScoreExercise);
        tvTodayWorkoutMinutes = findViewById(R.id.tvTodayWorkoutMinutes);
        tvTodayMealCalories = findViewById(R.id.tvTodayMealCalories);

        // ===== 클릭 리스너 설정 =====
        btnOpenRecord.setOnClickListener(v -> {
            Intent i = new Intent(this, RecordSimpleActivity.class);
            startActivity(i);
        });
        btnOpenBodyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, BodyInfoActivity.class);
            startActivity(intent);
        });
        btnOpenMyPage.setOnClickListener(v->{
            Intent intent = new Intent(this, MyPageActivity.class);
            startActivity(intent);
        });

        // [수정됨] 날짜 관련 로직 제거
        // renderDate();
        // if (btnPrevDay != null) ...
        // if (btnNextDay != null) ...

        if (btnHome != null)   btnHome.setOnClickListener(v -> Toast.makeText(this, "현재 화면입니다", Toast.LENGTH_SHORT).show());

        // 웰컴 팝업 로직
        checkWelcomePopup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData(); // 홈 데이터 불러오기
    }

    /** 홈 화면 데이터 API 호출 */
    private void loadHomeData() {
        ApiService api = ApiClient.getApiService();
        api.getHomeData().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "Home data loaded: " + response.body().toString());
                    updateUI(response.body());
                } else {
                    Log.e("MainActivity", "Home data load failed: " + response.code());
                    if(response.code() == 401) {
                        Toast.makeText(MainActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MainActivity", "Home data network error: " + t.getMessage());
            }
        });
    }

    // =================================================================
    //  ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ [수정된 부분] ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
    // =================================================================

    /** API 응답으로 UI 업데이트 */
    private void updateUI(JsonObject data) {
        try {
            // 1. 유저 정보 업데이트
            if (tvHeadline != null && data.has("user_info")) {
                JsonObject userInfo = data.getAsJsonObject("user_info");
                String name = "사용자";
                String constitution = "체질";
                if (userInfo.has("name") && !userInfo.get("name").isJsonNull()) {
                    name = userInfo.get("name").getAsString();
                }
                if (userInfo.has("constituion") && !userInfo.get("constituion").isJsonNull()) {
                    constitution = userInfo.get("constituion").getAsString();
                }
                tvHeadline.setText(constitution + " " + name + "님,");
            }

            // 2. 오늘 리포트 업데이트
            int calories = 0;
            int exerciseKcal = 0; // [수정] 변수명을 duration -> exerciseKcal 로 변경
            if (data.has("today_report") && !data.get("today_report").isJsonNull()) {
                JsonObject report = data.getAsJsonObject("today_report");
                if (report.has("meal_calories") && !report.get("meal_calories").isJsonNull()) {
                    calories = report.get("meal_calories").getAsInt();
                }
                // [수정] 서버가 보내주는 'exercise_calories'를 읽도록 변경
                if (report.has("exercise_calories") && !report.get("exercise_calories").isJsonNull()) {
                    exerciseKcal = report.get("exercise_calories").getAsInt();
                }
            }

            // 3. 운동 점수판 업데이트
            if (tvScoreExercise != null) {
                // [수정] "분" -> "kcal"
                tvScoreExercise.setText(exerciseKcal + "kcal");
            }

            // 4. 오늘의 기록 섹션 업데이트
            if (tvTodayWorkoutMinutes != null) {
                // [수정] "운동: ... 분" -> "운동: ... kcal"
                tvTodayWorkoutMinutes.setText("운동: " + exerciseKcal + "kcal");
            }
            if (tvTodayMealCalories != null) {
                tvTodayMealCalories.setText("식사: " + calories + "kcal");
            }

        } catch (Exception e) {
            Log.e("MainActivity_UI", "Error parsing home data JSON", e);
            Toast.makeText(this, "데이터 표시에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // =================================================================
    //  ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ [수정된 부분] ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
    // =================================================================


    // [수정됨] renderDate() 메서드 전체 삭제

    private void checkWelcomePopup() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSeenWelcome = prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false);
        if (!hasSeenWelcome) {
            showWelcomeDialog();
        }
    }

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