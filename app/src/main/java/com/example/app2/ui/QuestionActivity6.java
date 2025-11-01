package com.example.app2.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.adapters.QuestionAdapter;
import com.example.app2.models.Question;
import com.example.app2.survey.AnswerStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;
import android.util.Log;
import okhttp3.logging.HttpLoggingInterceptor;

public class QuestionActivity6 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private List<Question> questionList;
    private Button btnNext, btnPrev;
    private ProgressBar progressBar;
    private TextView tvProgress;

    // ✅ 하드토큰 테스트용: 반드시 실제 유효한 토큰으로 교체하세요. (앞에 "Bearer " 포함!)
    private static final String TEST_BEARER =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEwLCJpYXQiOjE3NjEyMjcwMzEsImV4cCI6MTc2MTIyODgzMX0.N9T5ELtgYt2T3dSD2lB90szDbawa2v-BKVgEPJAq8Cw";

    // 간단한 Retrofit 서비스 정의 (이 파일 안에서 바로 사용)
    interface ConstitutionService {
        @POST("v1/constitution/analyze")
        Call<ResponseBody> analyze(@Body RequestBody body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        recyclerView = findViewById(R.id.recyclerView);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        TextView tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSectionTitle.setText("온도 및 물 섭취 습관");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        int currentStep = 6, totalStep = 6; // 마지막 단계 표기
        if (progressBar != null) { progressBar.setMax(totalStep); progressBar.setProgress(currentStep); }
        if (tvProgress != null) tvProgress.setText(currentStep + " / " + totalStep + "단계");

        if (btnNext != null) ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets) -> {
            v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
        if (btnPrev != null) ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets) -> {
            v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // 문항(15~16)
        questionList = new ArrayList<>();
        questionList.add(new Question(15, "추위와 더위 중 어느 것이 더 힘든가요?", Arrays.asList("추위가\n더 힘들다", "더위가\n더 힘들다", "둘 다 괜찮다")));
        questionList.add(new Question(16, "평소 마시는 물의 온도는?", Arrays.asList("따듯한 물을\n선호한다", "찬 물을\n선호한다", "특별히\n신경쓰지 않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                startActivity(new Intent(this, QuestionActivity5.class));
                try { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);} catch (Exception ignore) {}
            });
        }

        if (btnNext != null) {
            btnNext.setText("제출");
            btnNext.setOnClickListener(v -> {
                // 1) 이 페이지(15,16) 필수 체크
                Map<Integer, Integer> ans = adapter.getAnswerMap();
                int[] must = {15, 16};
                for (int qid : must) {
                    Integer a = ans.get(qid);
                    if (a == null || a < 1) {
                        Toast.makeText(this, "15~16번 문항을 모두 선택해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 2) 전체(1~16) 미응답 검사 (AnswerStore 기준)
                for (int qid = 1; qid <= 16; qid++) {
                    if (AnswerStore.get().get(qid) < 0) {
                        Toast.makeText(this, qid + "번 문항이 미응답입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 3) 사용자 기본정보 로드
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                float height = (float) Double.longBitsToDouble(prefs.getLong("height", Double.doubleToLongBits(170.0)));
                float weight = (float) Double.longBitsToDouble(prefs.getLong("weight", Double.doubleToLongBits(65.0)));
                String name = prefs.getString("name", "홍길동");
                int userId = prefs.getInt("user_id", 10);
                int age = prefs.getInt("age", 30);

                // 4) 요청 JSON 생성
                final JSONObject body = new JSONObject();
                try {
                    body.put("userId", userId);
                    body.put("userInfo", new JSONObject()
                            .put("age", age)
                            .put("height", height / 100.0f) // cm 저장이라면 m로 변환
                            .put("weight", weight));
                    body.put("answers", AnswerStore.get().toJsonArray1Based());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청 바디 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 5) Retrofit 호출 준비 — 하드토큰 고정
                Log.d("ANALYZE_BODY", body.toString());

                HttpLoggingInterceptor httpLogger = new HttpLoggingInterceptor(msg -> Log.d("HTTP", msg));
                httpLogger.setLevel(HttpLoggingInterceptor.Level.HEADERS);

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(chain -> chain.proceed(
                                chain.request().newBuilder()
                                        .header("Authorization", TEST_BEARER) // ← 항상 하드토큰 사용
                                        .build()
                        ))
                        .addInterceptor(httpLogger)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:3000/") // 에뮬레이터 → 로컬 서버
                        .client(client)
                        .build();

                ConstitutionService svc = retrofit.create(ConstitutionService.class);
                RequestBody reqBody = RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                // 6) 호출
                btnNext.setEnabled(false);
                svc.analyze(reqBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                        btnNext.setEnabled(true);
                        if (!res.isSuccessful()) {
                            String serverMsg = "";
                            try { serverMsg = res.errorBody() != null ? res.errorBody().string() : ""; } catch (Exception ignore) {}
                            Toast.makeText(QuestionActivity6.this,
                                    "응답 오류: " + res.code() + " " + serverMsg, Toast.LENGTH_LONG).show();
                            Log.e("API", "analyze error " + res.code() + " " + serverMsg);
                            return;
                        }
                        try {
                            String analyzeJson = res.body().string();
                            Intent intent = new Intent(QuestionActivity6.this, ResultActivity.class);
                            intent.putExtra("analyzeJson", analyzeJson);
                            intent.putExtra("name", name);
                            intent.putExtra("heightCm", height);
                            intent.putExtra("weightKg", weight);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(QuestionActivity6.this, "응답 처리 중 오류", Toast.LENGTH_SHORT).show();
                            Log.e("API", "parse error", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        btnNext.setEnabled(true);
                        Toast.makeText(QuestionActivity6.this, "네트워크 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("API", "network failure", t);
                    }
                });
            });
        }
    }
}
