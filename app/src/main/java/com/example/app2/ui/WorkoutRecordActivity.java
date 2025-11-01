package com.example.app2.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

// [추가] FloatingActionButton 임포트
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WorkoutRecordActivity extends AppCompatActivity {

    private ImageView btnBack;

    // 날짜 스트립 뷰
    private LinearLayout weekStrip;
    private final Calendar calToday = Calendar.getInstance();

    // 예시 운동 삭제 버튼
    private ImageButton btnDeleteWorkout1, btnDeleteWorkout2;

    // [추가] FAB 변수
    private FloatingActionButton fabAddWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_record);

        // 1. 뷰 바인딩
        btnBack = findViewById(R.id.btnBack);
        weekStrip = findViewById(R.id.weekStrip);
        btnDeleteWorkout1 = findViewById(R.id.btnDeleteWorkout1);
        btnDeleteWorkout2 = findViewById(R.id.btnDeleteWorkout2);

        // [추가] FAB 바인딩
        fabAddWorkout = findViewById(R.id.fabAddWorkout);

        // 2. 클릭 리스너 설정
        setupClicks();

        // 3. 날짜 스트립 생성
        initWeekStrip();
    }

    private void setupClicks() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish()); // 현재 액티비티 종료

        // 예시 삭제 버튼 (기능 테스트용)
        btnDeleteWorkout1.setOnClickListener(v ->
                Toast.makeText(this, "빠르게 걷기 삭제됨 (예시)", Toast.LENGTH_SHORT).show());
        btnDeleteWorkout2.setOnClickListener(v ->
                Toast.makeText(this, "스쿼트 삭제됨 (예시)", Toast.LENGTH_SHORT).show());

        // [추가] FAB 클릭 리스너
        fabAddWorkout.setOnClickListener(v -> {
            // TODO: 운동 추가 바텀 시트 또는 팝업을 띄우는 로직
            Toast.makeText(this, "운동 추가하기", Toast.LENGTH_SHORT).show();
        });
    }

    // --- [복사] MealRecordActivity에서 가져온 날짜 로직 ---

    /** 상단 주간(월~일) 스트립 생성 — 오늘 날짜 강조 */
    private void initWeekStrip() {
        if (weekStrip == null) return;

        Calendar c = (Calendar) calToday.clone();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int mondayOffset = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        c.add(Calendar.DAY_OF_MONTH, mondayOffset);

        SimpleDateFormat dayNum = new SimpleDateFormat("d", Locale.KOREA);
        SimpleDateFormat dayKr  = new SimpleDateFormat("E", Locale.KOREA);

        weekStrip.removeAllViews();

        for (int i = 0; i < 7; i++) {
            boolean isToday = isSameDay(c, calToday);
            View item = createDayItem(
                    dayNum.format(c.getTime()),
                    dayKr.format(c.getTime()),
                    isToday
            );
            weekStrip.addView(item);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** 날짜 아이템 하나(“9”, “목”) 생성 (MealRecordActivity와 동일) */
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
            tvDay.setTextColor(0xFF424242); // 흰색 배경에 맞는 글자색
            tvWeek.setTextColor(0xFF8A8A8A); // 흰색 배경에 맞는 글자색
        }

        wrap.addView(tvDay);
        wrap.addView(tvWeek);
        return wrap;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}