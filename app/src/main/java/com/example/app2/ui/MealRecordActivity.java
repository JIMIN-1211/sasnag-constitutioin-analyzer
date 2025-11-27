package com.example.app2.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView; // [추가]
import android.widget.ArrayAdapter; // [추가]
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner; // [추가]
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// 💡 import 경로가 .adapters.MealAdapter로 변경되었습니다.
import com.example.app2.adapters.MealAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList; // [추가]
import java.util.Calendar;
import java.util.List; // [추가]
import java.util.Locale;
import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 💡 [내부 클래스] Spinner용 헬퍼 클래스
class FoodItem {
    private int id;
    private String foodName;
    private double calPerGram;

    public FoodItem(int id, String foodName, double calPerGram) {
        this.id = id;
        this.foodName = foodName;
        this.calPerGram = calPerGram;
    }
    public int getId() { return id; }
    public String getFoodName() { return foodName; }
    public double getCalPerGram() { return calPerGram; }

    /**
     * 💡 [핵심] Spinner는 이 메서드가 반환하는 값을 표시합니다.
     * "닭가슴살", "메밀" 등 'foodName'을 반환해야 합니다.
     */
    @Override
    public String toString() {
        return this.foodName;
    }
}


// 💡 어댑터의 '삭제' 리스너 인터페이스를 구현
public class MealRecordActivity extends AppCompatActivity implements MealAdapter.OnMealDeleteListener {

    private ImageView btnBack;
    private FloatingActionButton fabAddFood;
    private LinearLayout weekStrip;
    private final Calendar calToday = Calendar.getInstance();

    private ApiService apiService;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    // 💡 RecyclerView, Adapter, 빈 화면 TextView
    private RecyclerView mealRecyclerView;
    private LinearLayout tvEmptyMeal; // "기록이 없습니다" 레이아웃
    private MealAdapter mealAdapter;
    private JsonArray mealListData = new JsonArray();
    private String selectedDate;

    // 💡 Spinner에 채울 하드코딩된 데이터
    private List<FoodItem> foodMasterList = new ArrayList<>();
    private ArrayAdapter<FoodItem> foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_record);

        ApiClient.init(getApplicationContext());
        apiService = ApiClient.getApiService(this);

        btnBack = findViewById(R.id.btnBack);
        fabAddFood = findViewById(R.id.fabAddFood);
        weekStrip = findViewById(R.id.weekStrip);
        mealRecyclerView = findViewById(R.id.mealRecyclerView);
        tvEmptyMeal = findViewById(R.id.tvEmptyMeal); // (XML의 "아직 기록된..." LinearLayout ID)

        setupClicks();
        initWeekStrip();
        setupRecyclerView();

        // 💡 [추가] 1. DB 데이터를 Java 코드로 직접 만듭니다.
        createHardcodedFoodList();

        // 💡 [추가] 2. 이 '수동' 목록으로 Spinner 어댑터를 초기화합니다.
        foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, foodMasterList);
    }

    /** [신규] master_foods 테이블의 데이터를 Java 코드로 하드코딩하는 메서드 */
    private void createHardcodedFoodList() {
        foodMasterList.clear();

        // (08:42:01에 보내주신 `SELECT * FROM master_foods` 이미지 기준)
        foodMasterList.add(new FoodItem(1, "메밀", 0.92));
        foodMasterList.add(new FoodItem(2, "전복", 0.82));
        foodMasterList.add(new FoodItem(3, "쇠고기 (안심)", 1.37));
        foodMasterList.add(new FoodItem(4, "콩 (대두)", 4.18));
        foodMasterList.add(new FoodItem(5, "돼지고기 (목살)", 2.05));
        foodMasterList.add(new FoodItem(6, "해삼", 0.45));
        foodMasterList.add(new FoodItem(7, "닭고기 (가슴살)", 1.65));
        foodMasterList.add(new FoodItem(8, "찹쌀", 3.7));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 '기록된 목록'을 새로고침합니다.
        loadMealList();
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter(this, mealListData);
        mealRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealRecyclerView.setAdapter(mealAdapter);
        mealAdapter.setOnDeleteListener(this);
    }

    /** GET /v1/meal API를 호출하여 '기록된' 목록을 불러옵니다. */
    private void loadMealList() {
        // TODO: (중요) 날짜 스트립(weekStrip)에서 선택된 날짜를 사용해야 합니다.
        selectedDate = apiDateFormat.format(calToday.getTime()); // (지금은 '오늘' 날짜 하드코딩)

        // ApiService.java에 'getDietList'가 선언되어 있어야 합니다.
        apiService.getDietList(selectedDate).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mealListData = response.body().getAsJsonArray("meal_list");
                    mealAdapter.updateData(mealListData);
                    checkIfListIsEmpty();
                } else {
                    Log.e("MealRecordActivity", "getDietList failed: " + response.code());
                    checkIfListIsEmpty();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MealRecordActivity", "getDietList network error: " + t.getMessage());
                checkIfListIsEmpty();
            }
        });
    }

    /** 목록이 비어있으면 "기록이 없습니다" 텍스트를 표시합니다. */
    private void checkIfListIsEmpty() {
        if (mealListData == null || mealListData.size() == 0) {
            tvEmptyMeal.setVisibility(View.VISIBLE);
            mealRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMeal.setVisibility(View.GONE);
            mealRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());
        fabAddFood.setOnClickListener(v -> showAddFoodSheet());
    }

    /**
     * [수정] 바텀 시트가 Spinner를 사용하도록 로직 수정
     */
    private void showAddFoodSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_food, null);
        bottomSheetDialog.setContentView(sheetView);

        // --- 뷰 바인딩 ---
        final Spinner spFoodItem = sheetView.findViewById(R.id.spinner_food_item); // 💡 XML에서 Spinner로 교체됨
        final TextView tvFoodInfo = sheetView.findViewById(R.id.tvFoodInfo); // 💡 "g당 kcal" 텍스트
        final EditText etFoodGram = sheetView.findViewById(R.id.et_food_gram);
        Button btnDone = sheetView.findViewById(R.id.btnDone);

        // 1. '하드코딩된' foodAdapter를 Spinner에 연결
        spFoodItem.setAdapter(foodAdapter);

        // 2. Spinner에서 음식을 선택할 때마다 '별개의 TextView' (tvFoodInfo)의 텍스트를 업데이트
        spFoodItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FoodItem selectedFood = (FoodItem) parent.getItemAtPosition(position);
                // 100g 기준으로 변환 (예: 1.65 -> 165)
                double calPer100Gram = selectedFood.getCalPerGram() * 100;
                tvFoodInfo.setText(String.format(Locale.KOREA, "100g 당 %.0f kcal", calPer100Gram));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // '완료' 버튼 클릭 시, API 전송
        btnDone.setOnClickListener(v_inner -> {

            // 3. 입력 값 가져오기
            FoodItem selectedFood = (FoodItem) spFoodItem.getSelectedItem();
            if (selectedFood == null) {
                Toast.makeText(this, "음식을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            int foodId = selectedFood.getId(); // '수동' 목록에서 ID(예: 7)를 가져옴

            String mealType = "BREAKFAST"; // (하드코딩: 아침)
            String targetDate = selectedDate; // (현재 선택된 날짜 사용)

            String gramStr = etFoodGram.getText().toString();
            if (gramStr.isEmpty()) {
                Toast.makeText(this, "섭취량(g)을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            double intakeGram = Double.parseDouble(gramStr);

            // 4. 서버로 보낼 JsonObject 생성
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("targetDate", targetDate);
            requestBody.addProperty("meal_type", mealType);
            requestBody.addProperty("food_id", foodId); // 선택된 ID 전송
            requestBody.addProperty("intake_gram", intakeGram);

            Log.d("MealRecordActivity", "POST /v1/diet: " + requestBody.toString());

            // 5. API 호출 (서버는 food_id=7인걸 보고 알아서 1.65를 곱함)
            apiService.postMeal(requestBody).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MealRecordActivity.this, "식단이 기록되었습니다.", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        loadMealList(); // 목록 새로고침
                    } else {
                        Log.e("MealRecordActivity", "postMeal failed: " + response.code());
                        Toast.makeText(MealRecordActivity.this, "저장 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("MealRecordActivity", "postMeal network error: " + t.getMessage());
                }
            });
        });

        bottomSheetDialog.show();
    }

    /**
     * 어댑터(MealAdapter)의 'X' 버튼이 클릭되면 이 메서드가 호출됩니다.
     */
    @Override
    public void onDeleteClick(long mealLogId, int position) {
        Log.d("MealRecordActivity", "Deleting meal log ID: " + mealLogId);

        apiService.deleteMeal(mealLogId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MealRecordActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                    if (mealListData != null && position < mealListData.size()) {
                        mealListData.remove(position);
                        mealAdapter.notifyItemRemoved(position);
                        mealAdapter.notifyItemRangeChanged(position, mealListData.size());
                        checkIfListIsEmpty();
                    }
                } else {
                    Log.e("MealRecordActivity", "deleteMeal failed: " + response.code());
                    Toast.makeText(MealRecordActivity.this, "삭제 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MealRecordActivity", "deleteMeal network error: " + t.getMessage());
            }
        });
    }

    // --- 날짜 스트립 로직 (수정 없음) ---

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
            View item = createDayItem(dayNum.format(c.getTime()), dayKr.format(c.getTime()), isToday);
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