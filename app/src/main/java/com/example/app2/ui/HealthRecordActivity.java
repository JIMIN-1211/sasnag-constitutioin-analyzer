package com.example.app2.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
// [수정] import android.widget.ImageView; -> ImageButton으로 변경
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// [수정] androidx.annotation.ColorInt; (사용 안 함)
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.content.Intent;

public class HealthRecordActivity extends AppCompatActivity {

    private LinearLayout weekStrip;
    private Button btnStartWorkout;
    private TextView btnHome, btnOpenRecord, btnOpenMyPage, tvUser;

    private ImageButton btnCalendar, btnAddMeal;

    private final Calendar calToday = Calendar.getInstance(); // 오늘 기준

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        bindViews();
        initWeekStrip();      // 상단 주간 날짜 동적 생성
        setupClicks();        // 버튼/탭 클릭

        // [추가] 유저 정보 로드 (예시)
        loadUserInfo();
    }

    // [추가] 유저 정보 설정 예시
    private void loadUserInfo() {
        // TODO: 실제 데이터로 교체
        tvUser.setText("홍길동 님");
    }

    private void bindViews() {
        weekStrip       = findViewById(R.id.weekStrip);
        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnAddMeal      = findViewById(R.id.btnAddMeal);
        btnHome         = findViewById(R.id.btnHome);
        btnOpenRecord   = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage   = findViewById(R.id.btnOpenMyPage);
        btnCalendar     = findViewById(R.id.btnCalendar); // 타입 일치

        // [추가] 새 뷰 바인딩
        tvUser          = findViewById(R.id.tvUser);
    }

    /** 상단 주간(월~일) 스트립 생성 — 오늘 날짜 강조 */
    private void initWeekStrip() {
        if (weekStrip == null) return;

        // 주초(월요일)로 이동
        Calendar c = (Calendar) calToday.clone();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);                      // 1=일, 2=월 ...
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

    // [수정] createDayItem 메서드 전체를 새 디자인으로 교체
    /** 날짜 아이템 하나(“9”, “목”) 생성 (디자인 수정됨) */
    private View createDayItem(String day, String week, boolean selected) {
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
            // (디자인처럼 더 길게 만들기 위해 상하 패딩을 더 줌)
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));

            // [필수] 2번에서 만든 bg_day_selected_tall (흰색 둥근 사각형) 사용
            wrap.setBackground(getDrawable(R.drawable.bg_day_selected_tall));
            wrap.setElevation(dp(4)); // 그림자

            tvDay.setTextColor(0xFF000000); // 검은색
            tvDay.setTypeface(Typeface.DEFAULT_BOLD);

            tvWeek.setTextColor(0xFF000000); // 검은색
            tvWeek.setTypeface(Typeface.DEFAULT_BOLD);

        } else {
            // [선택되지 않은 날짜]
            // (선택된 것과 높이를 맞추기 위해 동일한 패딩 적용)
            wrap.setPadding(dp(8), dp(16), dp(8), dp(16));
            wrap.setBackgroundColor(android.graphics.Color.TRANSPARENT); // 배경 투명

            tvDay.setTextColor(0xFFFFFFFF); // 흰색
            tvWeek.setTextColor(0xFFC8E6C9); // 연한 녹색
        }

        wrap.addView(tvDay);
        wrap.addView(tvWeek);
        return wrap;
    }


    private void setupClicks() {
        btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(HealthRecordActivity.this, WorkoutRecordActivity.class);
            startActivity(intent);
        });
        btnAddMeal.setOnClickListener(v -> {
            Intent intent = new Intent(HealthRecordActivity.this, MealRecordActivity.class);
            startActivity(intent);
        });
        btnCalendar.setOnClickListener(v ->
                Toast.makeText(this, "달력 열기", Toast.LENGTH_SHORT).show());

        btnHome.setOnClickListener(v ->{
            Intent intent = new Intent(HealthRecordActivity.this, MainActivity.class);
            // [추가] 홈으로 갈 땐 스택을 정리하는 것이 좋습니다.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnOpenRecord.setOnClickListener(v ->
                Toast.makeText(this, "현재 화면입니다", Toast.LENGTH_SHORT).show());
        btnOpenMyPage.setOnClickListener(v->{
            Intent intent = new Intent(HealthRecordActivity.this, MyPageActivity.class);
            // [추가] 다른 탭으로 갈 땐 스택을 재사용하는 것이 좋습니다.
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}