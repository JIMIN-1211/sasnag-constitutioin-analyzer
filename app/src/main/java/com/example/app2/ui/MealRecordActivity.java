// (신규 파일: /ui/MealRecordActivity.java)
package com.example.app2.ui;

import android.graphics.Typeface; // [추가]
import android.os.Bundle;
import android.view.Gravity; // [추가]
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // [추가]
import android.widget.TextView; // [추가]
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat; // [추가]
import java.util.Calendar; // [추가]
import java.util.Locale; // [추가]

public class MealRecordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private FloatingActionButton fabAddFood;

    // [추가] 날짜 스트립 뷰
    private LinearLayout weekStrip;
    private final Calendar calToday = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_record);

        // 1. 뷰 바인딩
        btnBack = findViewById(R.id.btnBack);
        fabAddFood = findViewById(R.id.fabAddFood);
        weekStrip = findViewById(R.id.weekStrip); // [추가]

        // 2. 클릭 리스너 설정
        setupClicks();

        // 3. [추가] 날짜 스트립 생성
        initWeekStrip();
    }

    private void setupClicks() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish()); // 현재 액티비티 종료

        // '+' FAB 버튼 (바텀 시트 열기)
        fabAddFood.setOnClickListener(v -> showAddFoodSheet());
    }

    /**
     * '음식 기록.png (1)' 이미지의 바텀 시트를 띄웁니다.
     */
    private void showAddFoodSheet() {
        // ... (기존 바텀 시트 코드는 동일)
    }

    // --- [추가] HealthRecordActivity에서 가져온 날짜 로직 ---

    /** 상단 주간(월~일) 스트립 생성 — 오늘 날짜 강조 */
    private void initWeekStrip() {
        if (weekStrip == null) return;

        // 주초(월요일)로 이동
        Calendar c = (Calendar) calToday.clone();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); // 1=일, 2=월 ...
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

    /** 날짜 아이템 하나(“9”, “목”) 생성 (새로운 디자인 적용) */
    private View createDayItem(String day, String week, boolean selected) {
        // HealthRecordActivity와 동일한 디자인 사용
        int baseWidth = dp(48); // 너비
        int margin = dp(4);     // 아이템 간 간격

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
        tvDay.setPadding(0, 0, 0, dp(4)); // 숫자와 요일 사이 간격

        TextView tvWeek = new TextView(this);
        tvWeek.setText(week);
        tvWeek.setTextSize(14);
        tvWeek.setGravity(Gravity.CENTER);

        if (selected) {
            // [선택된 날짜]
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));
            wrap.setBackground(getDrawable(R.drawable.bg_day_selected_tall)); // 흰색 둥근 사각형
            wrap.setElevation(dp(4));

            tvDay.setTextColor(0xFF000000); // 검은색
            tvDay.setTypeface(Typeface.DEFAULT_BOLD);
            tvWeek.setTextColor(0xFF000000); // 검은색
            tvWeek.setTypeface(Typeface.DEFAULT_BOLD);

        } else {
            // [선택되지 않은 날짜]
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));
            wrap.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            // (MealRecord는 HealthRecord와 달리 배경이 흰색이므로 텍스트 색상 변경)
            tvDay.setTextColor(0xFF424242); // 진한 회색
            tvWeek.setTextColor(0xFF8A8A8A); // 연한 회색
        }

        wrap.addView(tvDay);
        wrap.addView(tvWeek);
        return wrap;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    // --- [추가] 끝 ---
}