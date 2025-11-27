package com.example.app2.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar; // 💡 [추가] ProgressBar import
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
import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthRecordActivity extends AppCompatActivity {

    private LinearLayout weekStrip;
    private Button btnStartWorkout;
    private TextView btnHome, btnOpenRecord, btnOpenMyPage, tvUser, tvBadge, tvScoreMeal, tvScoreExercise, tvScoreSleep;
    private ImageButton btnCalendar, btnAddMeal;
    private ProgressBar progressMeal, progressExercise, progressSleep;
    private TextView tvCalories;
    private TextView tvCaloriesUnit;
    private TextView tvWorkoutMeta;
    private TextView tvSleep;

    private Calendar selectedCalendar = Calendar.getInstance();
    private ApiService apiService;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        ApiClient.init(getApplicationContext());
        apiService = ApiClient.getApiService(this);

        bindViews();
        initWeekStrip();
        setupClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHealthData();
    }

    private void bindViews() {
        weekStrip       = findViewById(R.id.weekStrip);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnAddMeal      = findViewById(R.id.btnAddMeal);
        btnHome         = findViewById(R.id.btnHome);
        btnOpenRecord   = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage   = findViewById(R.id.btnOpenMyPage);
        btnCalendar     = findViewById(R.id.btnCalendar);
        tvUser          = findViewById(R.id.tvUser);
        tvBadge         = findViewById(R.id.tvBadge);
        progressMeal    = findViewById(R.id.progressMeal);
        tvScoreMeal     = findViewById(R.id.tvScoreMeal);
        progressExercise = findViewById(R.id.progressExercise);
        tvScoreExercise  = findViewById(R.id.tvScoreExercise);
        progressSleep   = findViewById(R.id.progressSleep);
        tvScoreSleep    = findViewById(R.id.tvScoreSleep);
        tvCalories      = findViewById(R.id.tvCalories);
        tvCaloriesUnit  = findViewById(R.id.tvCaloriesUnit);
        tvWorkoutMeta   = findViewById(R.id.tvWorkoutMeta);
        tvSleep         = findViewById(R.id.tvSleep);
    }

    // =================================================================
    //  API 호출 로직
    // =================================================================

    private void loadHealthData() {
        String targetDate = apiDateFormat.format(selectedCalendar.getTime());
        Log.d("HealthRecordActivity", "Loading data for date: " + targetDate);

        apiService.getHomeData(targetDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("HealthRecordActivity", "Data loaded: " + response.body().toString());
                    updateUI(response.body());
                } else {
                    Log.e("HealthRecordActivity", "Data load failed: " + response.code());
                    if (response.code() == 400) {
                        updateUI(null); // 0으로 리셋
                    } else {
                        Toast.makeText(HealthRecordActivity.this, "데이터 로딩 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("HealthRecordActivity", "Network error: " + t.getMessage());
                Toast.makeText(HealthRecordActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(JsonObject data) {

        if (data == null) {
            tvUser.setText("사용자 님");
            tvBadge.setText("체질");
            tvCalories.setText("0");
            tvCaloriesUnit.setText("/ 0 kcal");
            tvWorkoutMeta.setText("총 0 kcal 소모");
            tvSleep.setText("0h 00m");

            // "건강 밸런스" 섹션 초기화 (요청하신 부분)
            progressMeal.setProgress(0);
            tvScoreMeal.setText("0점");
            progressExercise.setProgress(0);
            tvScoreExercise.setText("0점");
            progressSleep.setProgress(0);
            tvScoreSleep.setText("0점");
            return;
        }

        try {
            // 1. 유저 정보 (수정 없음)
            if (data.has("user_info")) {
                JsonObject userInfo = data.getAsJsonObject("user_info");
                String name = "사용자";
                String constitution = "체질";

                if (userInfo.has("name") && !userInfo.get("name").isJsonNull()) {
                    name = userInfo.get("name").getAsString();
                }
                if (userInfo.has("constituion") && !userInfo.get("constituion").isJsonNull()) {
                    constitution = userInfo.get("constituion").getAsString();
                }

                tvUser.setText(name + " 님");
                tvBadge.setText(constitution);
            }

            // 2. 오늘의 리포트 (수정 없음)
            if (data.has("today_report")) {
                JsonObject report = data.getAsJsonObject("today_report");
                int mealKcal = 0;
                int mealGoal = 2000;
                if (report.has("meal_calories") && !report.get("meal_calories").isJsonNull()) {
                    mealKcal = report.get("meal_calories").getAsInt();
                }
                if (report.has("meal_goal") && !report.get("meal_goal").isJsonNull()) {
                    mealGoal = report.get("meal_goal").getAsInt();
                }
                tvCalories.setText(String.format(Locale.KOREA, "%,d", mealKcal));
                tvCaloriesUnit.setText(String.format(Locale.KOREA, "/ %,d kcal", mealGoal));

                int exerciseKcal = 0;
                if (report.has("exercise_calories") && !report.get("exercise_calories").isJsonNull()) {
                    exerciseKcal = report.get("exercise_calories").getAsInt();
                }
                tvWorkoutMeta.setText(String.format(Locale.KOREA, "총 %,d kcal 소모", exerciseKcal));

                double sleepHours = 0.0;
                if (report.has("sleep_duration_hours") && !report.get("sleep_duration_hours").isJsonNull()) {
                    sleepHours = report.get("sleep_duration_hours").getAsDouble();
                }
                int hours = (int) sleepHours;
                int minutes = (int) ((sleepHours - hours) * 60);
                tvSleep.setText(String.format(Locale.KOREA, "%dh %02dm", hours, minutes));

                // --- ▼▼▼ [수정] "건강 밸런스" 점수 파싱 로직 추가 ▼▼▼ ---
                int mealScore = 0;
                int exerciseScore = 0;
                int sleepScore = 0;

                // home.js가 보내준 health_balance 객체를 꺼냅니다.
                if (report.has("health_balance") && report.get("health_balance").isJsonObject()) {
                    JsonObject balance = report.getAsJsonObject("health_balance");
                    if (balance.has("meal") && !balance.get("meal").isJsonNull()) {
                        mealScore = balance.get("meal").getAsInt();
                    }
                    if (balance.has("exercise") && !balance.get("exercise").isJsonNull()) {
                        exerciseScore = balance.get("exercise").getAsInt();
                    }
                    if (balance.has("sleep") && !balance.get("sleep").isJsonNull()) {
                        sleepScore = balance.get("sleep").getAsInt();
                    }
                }

                // 1. 식단 점수 UI 업데이트
                progressMeal.setProgress(mealScore);
                tvScoreMeal.setText(String.format(Locale.KOREA, "%d점", mealScore));

                // 2. 운동 점수 UI 업데이트
                progressExercise.setProgress(exerciseScore);
                tvScoreExercise.setText(String.format(Locale.KOREA, "%d점", exerciseScore));

                // 3. 수면 점수 UI 업데이트
                progressSleep.setProgress(sleepScore);
                tvScoreSleep.setText(String.format(Locale.KOREA, "%d점", sleepScore));
                // --- ▲▲▲ [수정] 추가 끝 ▲▲▲ ---
            }

        } catch (Exception e) {
            Log.e("HealthRecordActivity_UI", "Error parsing JSON", e);
            Toast.makeText(this, "데이터 표시에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // =================================================================
    //  날짜 스트립 로직 (수정 없음)
    // =================================================================

    private void initWeekStrip() {
        if (weekStrip == null) return;
        weekStrip.removeAllViews();

        Calendar c = (Calendar) selectedCalendar.clone();

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int mondayOffset = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        c.add(Calendar.DAY_OF_MONTH, mondayOffset);

        SimpleDateFormat dayNum = new SimpleDateFormat("d", Locale.KOREA);
        SimpleDateFormat dayKr  = new SimpleDateFormat("E", Locale.KOREA);

        for (int i = 0; i < 7; i++) {
            boolean isSelected = isSameDay(c, selectedCalendar);
            final Date dateOfItem = c.getTime();

            View item = createDayItem(
                    dayNum.format(dateOfItem),
                    dayKr.format(dateOfItem),
                    isSelected
            );

            item.setOnClickListener(v -> {
                selectedCalendar.setTime(dateOfItem);
                initWeekStrip();
                loadHealthData();
            });

            weekStrip.addView(item);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private View createDayItem(String day, String week, boolean selected) {
        int baseWidth = dp(48);
        int margin = dp(4);
        LinearLayout wrap = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(baseWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, margin, 0);
        wrap.setLayoutParams(params);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView tvDay = new TextView(this);
        tvDay.setText(day);
        tvDay.setTextSize(18);
        tvDay.setGravity(Gravity.CENTER);
        tvDay.setPadding(0, 0, 0, dp(4));
        TextView tvWeek = new TextView(this);
        tvWeek.setText(week);
        tvWeek.setTextSize(14);
        tvWeek.setGravity(Gravity.CENTER);

        if (selected) {
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));
            wrap.setBackground(getDrawable(R.drawable.bg_day_selected_tall));
            wrap.setElevation(dp(4));
            tvDay.setTextColor(0xFF000000);
            tvDay.setTypeface(Typeface.DEFAULT_BOLD);
            tvWeek.setTextColor(0xFF000000);
            tvWeek.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));
            wrap.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            tvDay.setTextColor(0xFFFFFFFF);
            tvWeek.setTextColor(0xFFC8E6C9);
        }
        wrap.addView(tvDay);
        wrap.addView(tvWeek);
        return wrap;
    }

    // =================================================================
    //  클릭 리스너 로직 (수정 없음)
    // =================================================================

    private void setupClicks() {
        btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(HealthRecordActivity.this, WorkoutRecordActivity.class);
            intent.putExtra("TARGET_DATE", apiDateFormat.format(selectedCalendar.getTime()));
            startActivity(intent);
        });
        btnAddMeal.setOnClickListener(v -> {
            Intent intent = new Intent(HealthRecordActivity.this, MealRecordActivity.class);
            intent.putExtra("TARGET_DATE", apiDateFormat.format(selectedCalendar.getTime()));
            startActivity(intent);
        });
        btnCalendar.setOnClickListener(v ->
                Toast.makeText(this, "달력 열기", Toast.LENGTH_SHORT).show());

        btnHome.setOnClickListener(v ->{
            Intent intent = new Intent(HealthRecordActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnOpenRecord.setOnClickListener(v ->
                Toast.makeText(this, "현재 화면입니다", Toast.LENGTH_SHORT).show());
        btnOpenMyPage.setOnClickListener(v->{
            Intent intent = new Intent(HealthRecordActivity.this, MyPageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}