package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.auth.TokenManager;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat; // [추가]
import java.util.Date;             // [추가]
import java.util.Locale;           // [추가]
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class RecordSimpleActivity extends AppCompatActivity {

    // (API 인터페이스 및 데이터 클래스 정의)
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    interface LocalApi {
        // [수정] v1/meals -> v1/diet (Notion/meal.js 기준)
        @POST("v1/diet")
        Call<ResponseBody> postMeal(@Body MealRequest body);

        @POST("v1/exercise")
        Call<ResponseBody> postExercise(@Body ExerciseRequest body);

        @GET("v1/home")
        Call<ResponseBody> getHome();
    }

    // [수정] 식단 요청 Body (백엔드 기준)
    static class MealRequest {
        public String targetDate;
        public int food_id;
        public double intake_gram;
        public String meal_type;

        MealRequest(double intake_gram, String targetDate, int food_id) { // food_id를 받도록 수정
            this.targetDate = targetDate;
            this.intake_gram = intake_gram;
            this.food_id = food_id; // (ID 7, 닭가슴살)
            this.meal_type = "BREAKFAST"; // 테스트용 '아침' 고정
        }
    }

    // [수정] 운동 요청 Body (백엔드 기준)
    static class ExerciseRequest {
        public String targetDate;
        public int exercise_id;
        public int duration_minutes;

        ExerciseRequest(int duration_minutes, String targetDate) {
            this.targetDate = targetDate;
            this.duration_minutes = duration_minutes;
            this.exercise_id = 1; // 테스트용 '걷기(ID 1)' 고정
        }
    }

    private LocalApi api;
    private EditText etDate, etMealKcal, etSleepHours, etExerciseMinutes;
    private TextView btnSubmit, btnFetchHome, tvLog, tvHomeJson;

    // [수정] DB의 'g당 칼로리' 값 (닭가슴살 0.165)
    final double DB_CAL_PER_GRAM = 0.165;
    final int FOOD_ID_TEST = 7; // 닭가슴살 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_simple);
        ApiClient.init(getApplicationContext());
        bindViews();
        setupApi();
        setupActions();
    }

    private void bindViews() {
        etDate = findViewById(R.id.etDate);
        etMealKcal = findViewById(R.id.etMealKcal);
        etSleepHours = findViewById(R.id.etSleepHours);
        etExerciseMinutes = findViewById(R.id.etExerciseMinutes);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnFetchHome = findViewById(R.id.btnFetchHome);
        tvLog = findViewById(R.id.tvLog);
        tvHomeJson = findViewById(R.id.tvHomeJson);
    }

    private void setupApi() {
        // (API 설정 코드는 동일)
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String path = chain.request().url().encodedPath();
                    boolean isAuthEndpoint = path.contains("/v1/auth/");
                    okhttp3.Request.Builder b = chain.request().newBuilder();
                    if (!isAuthEndpoint) {
                        String raw = TokenManager.getRawToken(getApplicationContext());
                        if (raw != null && !raw.isEmpty()) {
                            b.header("Authorization", "Bearer " + raw);
                        }
                    }
                    return chain.proceed(b.build());
                })
                .addInterceptor(log)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        api = retrofit.create(LocalApi.class);
    }

    private void setupActions() {
        btnSubmit.setOnClickListener(v -> {
            if (TokenManager.getRawToken(getApplicationContext()) == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // [수정] 날짜가 비어있으면 오늘 날짜로 자동 설정
            String date = trimOrNull(etDate.getText().toString());
            if (date == null) {
                date = getTodayDateString();
                etDate.setText(date);
            }

            Integer mealKcal = parseIntOrNull(etMealKcal.getText().toString());
            Integer exercise = parseIntOrNull(etExerciseMinutes.getText().toString());
            Integer sleep = parseIntOrNull(etSleepHours.getText().toString()); // (수면은 현재 API 없음)

            if (mealKcal == null && exercise == null && sleep == null) {
                Toast.makeText(this, "최소 1개 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            tvLog.setText("");
            AtomicInteger remain = new AtomicInteger(0);
            if (mealKcal != null) remain.incrementAndGet();
            if (exercise != null) remain.incrementAndGet();
            // (sleep은 API가 없으므로 카운트 X)

            final int totalCalls = remain.get();
            if (totalCalls == 0) {
                Toast.makeText(this, "저장할 항목(식단/운동)이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // API 완료 콜백 (버튼 활성화)
            Callback<ResponseBody> commonCallback = new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                    if (r.isSuccessful()) appendLog("[API] OK " + r.code());
                    else appendLog("[API] FAIL " + r.code() + " " + safeErr(r));

                    if (remain.decrementAndGet() == 0) onAllDone();
                }
                @Override
                public void onFailure(Call<ResponseBody> c, Throwable t) {
                    appendLog("[API] ERROR " + t.getMessage());
                    if (remain.decrementAndGet() == 0) onAllDone();
                }
            };

            // API 호출
            if (mealKcal != null && mealKcal > 0) {
                enqueue("MEAL", api.postMeal(new MealRequest(mealKcal, date, FOOD_ID_TEST)), remain, commonCallback);
            }

            if (exercise != null && exercise > 0) {
                enqueue("EXERCISE", api.postExercise(new ExerciseRequest(exercise, date)), remain, commonCallback);
            }
        });

        // '불러오기' 버튼 (변경 없음)
        btnFetchHome.setOnClickListener(v -> {
            if (TokenManager.getRawToken(getApplicationContext()) == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            tvHomeJson.setText("요청 중…");
            api.getHome().enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                    if (r.isSuccessful()) {
                        try {
                            String raw = (r.body()!=null) ? r.body().string() : "";
                            tvHomeJson.setText(raw); // JSON 원본 텍스트 표시
                            appendLog("[HOME] OK " + r.code());
                        } catch (Exception e) {
                            tvHomeJson.setText("본문 읽기 오류");
                        }
                    } else {
                        tvHomeJson.setText("실패 " + r.code() + " " + safeErr(r));
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                    tvHomeJson.setText("네트워크 오류: " + t.getMessage());
                }
            });
        });
    }

    private void enqueue(String tag, Call<ResponseBody> call, AtomicInteger remain, Callback<ResponseBody> callback) {
        appendLog("[" + tag + "] 요청 전송…");
        call.enqueue(callback);
    }

    private String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
    }

    private void onAllDone() {
        Toast.makeText(this, "전송 완료. '불러오기' 버튼으로 확인해 보세요.", Toast.LENGTH_SHORT).show();
    }

    private void appendLog(String line) {
        runOnUiThread(() -> {
            String prev = tvLog.getText().toString();
            tvLog.setText((TextUtils.isEmpty(prev) ? "" : prev + "\n") + line);
        });
    }

    private static String safeErr(Response<?> r) {
        try { return r.errorBody() != null ? r.errorBody().string() : ""; }
        catch (Exception e) { return ""; }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}