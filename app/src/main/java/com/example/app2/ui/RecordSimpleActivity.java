package com.example.app2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

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
import retrofit2.http.Header;
import retrofit2.http.POST;

public class RecordSimpleActivity extends AppCompatActivity {

    // ===== 서버 설정 =====
    private static final String BASE_URL = "http://10.0.2.2:3000/"; // 에뮬레이터 → 로컬 서버

    interface ApiService {
        @POST("v1/meals")
        Call<ResponseBody> postMeal(@Header("Authorization") String bearer, @Body MealRequest body);

        @POST("v1/sleep")
        Call<ResponseBody> postSleep(@Header("Authorization") String bearer, @Body SleepRequest body);

        @POST("v1/exercise")
        Call<ResponseBody> postExercise(@Header("Authorization") String bearer, @Body ExerciseRequest body);

        @GET("v1/home")
        Call<ResponseBody> getHome(@Header("Authorization") String bearer);
    }

    static class MealRequest {
        public Integer calories;
        public String record_date; // "YYYY-MM-DD" (옵션)
        MealRequest(Integer calories, String record_date) {
            this.calories = calories; this.record_date = record_date;
        }
    }
    static class SleepRequest {
        public Integer duration_hours;
        public String record_date;
        SleepRequest(Integer duration_hours, String record_date) {
            this.duration_hours = duration_hours; this.record_date = record_date;
        }
    }
    static class ExerciseRequest {
        public Integer duration_minutes;
        public String record_date;
        ExerciseRequest(Integer duration_minutes, String record_date) {
            this.duration_minutes = duration_minutes; this.record_date = record_date;
        }
    }

    private ApiService api;
    private EditText etDate, etMealKcal, etSleepHours, etExerciseMinutes;
    private TextView btnSubmit, btnFetchHome, tvLog, tvHomeJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_simple);
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
        btnFetchHome = findViewById(R.id.btnFetchHome);   // 원시 JSON 조회 버튼
        tvLog = findViewById(R.id.tvLog);
        tvHomeJson = findViewById(R.id.tvHomeJson);

    }

    private void setupApi() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(log)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        api = retrofit.create(ApiService.class);
    }

    private String getBearer() {
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEwLCJpYXQiOjE3NjEwMTcyMzksImV4cCI6MTc2MTAxOTAzOX0.gfAR85ls-MMAs-DwUg_VbRV9QNSP-u4jZ8bf9OZ342s";
    }

    private void setupActions() {
        // 입력된 것만 각각 POST
        btnSubmit.setOnClickListener(v -> {
            String bearer = getBearer();
            if (bearer == null) {
                Toast.makeText(this, "JWT 토큰이 없습니다. 먼저 로그인하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = trimOrNull(etDate.getText().toString());
            Integer meal = parseIntOrNull(etMealKcal.getText().toString());
            Integer sleep = parseIntOrNull(etSleepHours.getText().toString());
            Integer exercise = parseIntOrNull(etExerciseMinutes.getText().toString());

            if (meal == null && sleep == null && exercise == null) {
                Toast.makeText(this, "최소 1개 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            tvLog.setText("");
            AtomicInteger remain = new AtomicInteger(0);
            if (meal != null) remain.incrementAndGet();
            if (sleep != null) remain.incrementAndGet();
            if (exercise != null) remain.incrementAndGet();

            if (meal != null) {
                enqueue("MEAL", api.postMeal(bearer, new MealRequest(meal, date)), remain);
            }
            if (sleep != null) {
                enqueue("SLEEP", api.postSleep(bearer, new SleepRequest(sleep, date)), remain);
            }
            if (exercise != null) {
                enqueue("EXERCISE", api.postExercise(bearer, new ExerciseRequest(exercise, date)), remain);
            }
        });

        // /v1/home 원시 JSON 조회
        btnFetchHome.setOnClickListener(v -> fetchHome());
    }

    private void fetchHome() {
        String bearer = getBearer();
        if (bearer == null) {
            Toast.makeText(this, "JWT 토큰이 없습니다. 먼저 로그인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        tvHomeJson.setText("요청 중…");
        api.getHome(bearer).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                if (r.isSuccessful()) {
                    try {
                        String raw = (r.body()!=null) ? r.body().string() : "";
                        tvHomeJson.setText(raw);
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
    }

    private void enqueue(String tag, Call<ResponseBody> call, AtomicInteger remain) {
        appendLog("[" + tag + "] 요청 전송…");
        call.enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) {
                    String bodyStr = "";
                    try { bodyStr = (r.body()!=null) ? r.body().string() : ""; } catch (Exception ignore) {}
                    appendLog("[" + tag + "] OK " + r.code() + (bodyStr.isEmpty() ? "" : (" " + bodyStr)));
                } else {
                    appendLog("[" + tag + "] FAIL " + r.code() + " " + safeErr(r));
                }
                if (remain.decrementAndGet() == 0) onAllDone();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                appendLog("[" + tag + "] ERROR " + t.getMessage());
                if (remain.decrementAndGet() == 0) onAllDone();
            }
        });
    }

    private void onAllDone() {
        Toast.makeText(this, "전송 완료. 필요하면 /v1/home으로 확인해 보세요.", Toast.LENGTH_SHORT).show();
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
