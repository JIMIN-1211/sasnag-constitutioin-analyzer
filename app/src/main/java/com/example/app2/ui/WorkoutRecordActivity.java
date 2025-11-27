package com.example.app2.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.example.app2.adapters.WorkoutAdapter; // 💡 import 확인
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date; // [추가]
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 💡 어댑터의 '삭제' 리스너 인터페이스를 구현
public class WorkoutRecordActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutDeleteListener {

    private ImageView btnBack;
    private LinearLayout weekStrip;
    private FloatingActionButton fabAddWorkout;

    // 💡 [수정] '오늘' 기준이 아닌 '현재 선택된 날짜'를 저장할 변수
    private Calendar selectedCalendar = Calendar.getInstance();

    private ApiService apiService;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    private RecyclerView workoutRecyclerView;
    private TextView tvEmptyWorkout;
    private WorkoutAdapter workoutAdapter;
    private JsonArray workoutListData = new JsonArray();
    // private String selectedDate; // (selectedCalendar가 이 역할을 대신함)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_record);

        ApiClient.init(getApplicationContext());
        apiService = ApiClient.getApiService(this);

        // 뷰 바인딩
        btnBack = findViewById(R.id.btnBack);
        weekStrip = findViewById(R.id.weekStrip);
        fabAddWorkout = findViewById(R.id.fabAddWorkout);
        workoutRecyclerView = findViewById(R.id.workoutRecyclerView);
        tvEmptyWorkout = findViewById(R.id.tvEmptyWorkout);

        setupClicks();
        initWeekStrip(); // 💡 UI만 그리고
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 💡 onResume에서 API를 호출
        loadWorkoutList();
    }

    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(this, workoutListData);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutAdapter.setOnDeleteListener(this);
    }

    /**
     * 💡 [수정] loadWorkoutList()가 selectedCalendar를 사용하도록 변경
     */
    private void loadWorkoutList() {
        // [수정] '오늘' 날짜가 아닌, '선택된' 날짜(selectedCalendar)를 포맷팅
        String targetDate = apiDateFormat.format(selectedCalendar.getTime());

        // ApiService.java에 getWorkoutList가 선언되어 있어야 합니다.
        apiService.getWorkoutList(targetDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    workoutListData = data.getAsJsonArray("workout_list");

                    workoutAdapter.updateData(workoutListData);
                    checkIfListIsEmpty();
                } else {
                    Log.e("WorkoutRecordActivity", "getWorkoutList failed: " + response.code());
                    checkIfListIsEmpty();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("WorkoutRecordActivity", "getWorkoutList network error: " + t.getMessage());
                checkIfListIsEmpty();
            }
        });
    }

    /**
     * 목록이 비어있으면 "기록이 없습니다" 텍스트를 표시합니다. (수정 없음)
     */
    private void checkIfListIsEmpty() {
        if (workoutListData == null || workoutListData.size() == 0) {
            tvEmptyWorkout.setVisibility(View.VISIBLE);
            workoutRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyWorkout.setVisibility(View.GONE);
            workoutRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 💡 [수정] AddWorkoutActivity로 날짜를 넘겨주도록 수정
     */
    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());

        fabAddWorkout.setOnClickListener(v->{
            Intent intent = new Intent(this, AddWorkoutActivity.class);

            // 💡 [신규] 현재 선택된 날짜(selectedDate)를 다음 화면으로 넘겨줍니다.
            String targetDate = apiDateFormat.format(selectedCalendar.getTime());
            intent.putExtra("TARGET_DATE", targetDate);

            startActivity(intent);
        });
    }

    /**
     * 어댑터(WorkoutAdapter)의 'X' 버튼이 클릭되면 호출됩니다. (수정 없음)
     */
    @Override
    public void onDeleteClick(long workoutLogId, int position) {
        Log.d("WorkoutRecordActivity", "Deleting workout log ID: " + workoutLogId);

        apiService.deleteWorkout(workoutLogId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WorkoutRecordActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                    if (workoutListData != null && position < workoutListData.size()) {
                        workoutListData.remove(position);
                        workoutAdapter.notifyItemRemoved(position);
                        workoutAdapter.notifyItemRangeChanged(position, workoutListData.size());
                        checkIfListIsEmpty();
                    }
                } else {
                    Log.e("WorkoutRecordActivity", "deleteWorkout failed: " + response.code());
                    Toast.makeText(WorkoutRecordActivity.this, "삭제 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("WorkoutRecordActivity", "deleteWorkout network error: " + t.getMessage());
            }
        });
    }

    // =================================================================
    //  💡 [수정] 날짜 스트립 로직 (MealRecordActivity와 동일하게 수정)
    // =================================================================

    /**
     * 💡 [수정] 상단 주간 스트립 생성 (선택된 날짜 기준)
     */
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

            // 💡 [신규] 날짜 아이템에 클릭 리스너 설정
            item.setOnClickListener(v -> {
                // 1. '선택된 날짜' 변수를 클릭한 날짜로 변경
                selectedCalendar.setTime(dateOfItem);

                // 2. UI 새로고침 (클릭한 날짜가 강조되도록)
                initWeekStrip();

                // 3. API 호출 (새 날짜의 데이터 불러오기)
                loadWorkoutList();
            });

            weekStrip.addView(item);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** 날짜 아이템 하나(“9”, “목”) 생성 (수정 없음) */
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
            tvDay.setTextColor(0xFF424242);
            tvWeek.setTextColor(0xFF8A8A8A);
        }
        wrap.addView(tvDay);
        wrap.addView(tvWeek);
        return wrap;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}