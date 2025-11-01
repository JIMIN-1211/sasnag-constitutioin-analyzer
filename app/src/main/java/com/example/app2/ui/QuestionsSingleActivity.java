package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.adapters.QuestionAdapter;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.example.app2.auth.TokenManager;
import com.example.app2.models.Question;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionsSingleActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView btnSubmit, btnBack;
    private ProgressBar progress;
    private QuestionAdapter adapter;

    private int userId = 0;
    private Float heightCmFromBody = null;
    private Float weightKgFromBody = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_single);

        ApiClient.init(getApplicationContext());

        rv = findViewById(R.id.rvQuestions);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);

        if (getIntent().hasExtra("heightCm")) {
            heightCmFromBody = getIntent().getFloatExtra("heightCm", 0f);
        }
        if (getIntent().hasExtra("weightKg")) {
            weightKgFromBody = getIntent().getFloatExtra("weightKg", 0f);
        }

        // userId는 로그인 후 me 결과 저장분 사용
        userId = TokenManager.getUserId(getApplicationContext());

        List<Question> questions = buildQuestions();

        adapter = new QuestionAdapter(
                this,
                questions,
                (answered, total) -> {
                    progress.setMax(total);
                    progress.setProgress(answered);
                    boolean all = answered == total;
                    btnSubmit.setEnabled(all);
                    btnSubmit.setAlpha(all ? 1f : 0.5f);
                });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        progress.setMax(questions.size());
        progress.setProgress(adapter.getAnsweredCount());
        btnSubmit.setEnabled(adapter.getAnsweredCount() == questions.size());
        btnSubmit.setAlpha(btnSubmit.isEnabled() ? 1f : 0.5f);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnSubmit.setOnClickListener(v -> submitToServer());
    }

    private void submitToServer() {
        if (adapter.getAnsweredCount() < adapter.getItemCount()) {
            Toast.makeText(this, "모든 문항에 응답해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId <= 0) {
            Toast.makeText(this, "userId가 없습니다. 먼저 로그인하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = buildRequestJson(adapter.getAnswerMap());
        android.util.Log.d("ANALYZE_BODY", body.toString());

        ApiService api = ApiClient.getApiService();
        Call<JsonObject> call = api.analyze(body); // Authorization은 Interceptor가 자동 첨부

        btnSubmit.setEnabled(false);
        btnSubmit.setAlpha(0.5f);

        call.enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> res) {
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1f);

                if (!res.isSuccessful() || res.body() == null) {
                    String serverMsg = null;
                    try {
                        if (res.errorBody() != null) serverMsg = res.errorBody().string();
                    } catch (Exception ignore) {}
                    android.util.Log.e("ANALYZE_FAIL","code=" + res.code() + " serverMsg=" + serverMsg);
                    Toast.makeText(QuestionsSingleActivity.this,
                            "분석 실패 (" + res.code() + ") " + (serverMsg != null ? serverMsg : ""),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String resultJson = res.body().toString();

                Intent i = new Intent(QuestionsSingleActivity.this, ResultActivity.class);
                i.putExtra("resultJson", resultJson);
                if (heightCmFromBody != null && heightCmFromBody > 0f) {
                    i.putExtra("heightCm", heightCmFromBody);
                }
                if (weightKgFromBody != null && weightKgFromBody > 0f) {
                    i.putExtra("weightKg", weightKgFromBody);
                }
                startActivity(i);
            }

            @Override public void onFailure(Call<JsonObject> c, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1f);
                android.util.Log.e("ANALYZE_NET_FAIL", "error", t);
                Toast.makeText(QuestionsSingleActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>(16);
        list.add(new Question(1,  "성격이 대범한 편인가요?", Arrays.asList("매우 대범하다","보통이다","섬세한 편이다")));
        list.add(new Question(2,  "행동 속도는 어떤가요?", Arrays.asList("빠른 편이다","보통이다","느린 편이다")));
        list.add(new Question(3,  "모든 일에 적극적인 편인가요?", Arrays.asList("적극적이다","보통이다","소극적이다")));
        list.add(new Question(4,  "성격이 외향적인 편인가요?", Arrays.asList("외향적이다","보통이다","내성적이다")));
        list.add(new Question(5,  "남성적인 경향이 강한가요?\n여성적 성향이 강한가요?", Arrays.asList("남성적이다","보통이다","여성적이다")));
        list.add(new Question(6,  "가끔 쉽게 흥분하거나 감정 기복이 있는 편인가요?", Arrays.asList("자주 그렇다","가끔 그렇다","거의 없다")));
        list.add(new Question(7,  "평소 소화 상태는 어떠신가요?", Arrays.asList("소화가 잘 된다","소화가\n잘 안되지만\n불편하지 않다","소화가\n잘 안되고\n불편하다")));
        list.add(new Question(8,  "식욕 상태는 어떤가요?", Arrays.asList("매우 좋다","보통이다","입맛이 없다")));
        list.add(new Question(9,  "야식을 자주 드시나요?", Arrays.asList("거의\n먹지 않는다","가끔 먹는다","자주 먹는다")));
        list.add(new Question(10,"기름진 음식이나 단 음식을 선호하시나요?", Arrays.asList("좋아한다","보통이다","별로\n좋아하지 않는다")));
        list.add(new Question(11,"평소 땀을 많이 흘리는 편인가요?", Arrays.asList("많이 흘린다","보통이다","거의\n흘리지 않는다")));
        list.add(new Question(12,"땀을 흘리고 난 뒤 기분이 어떤가요?", Arrays.asList("상쾌하다","피곤하다","아무런\n차이가 없다")));
        list.add(new Question(13,"대변이 마려울 때 참기 어려운가요?", Arrays.asList("자주 그렇다","가끔 그렇다","거의 없다")));
        list.add(new Question(14,"야간에 소변을 보러 몇 번이나 가나요?", Arrays.asList("0회","1회","2회 이상")));
        list.add(new Question(15,"추위와 더위 중 어느 것이 더 힘든가요?", Arrays.asList("추위가\n힘들다","더위가\n힘들다","둘 다\n괜찮다")));
        list.add(new Question(16,"평소 마시는 물의 온도는?", Arrays.asList("따뜻한 물을\n선호한다","찬 물을\n선호한다","특별히\n신경쓰지 않는다")));
        return list;
    }

    /** 서버 사양에 맞는 Gson JsonObject 생성 — BodyInfo 입력값 반영 */
    private JsonObject buildRequestJson(Map<Integer, Integer> qidToAns) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);

        int h = (heightCmFromBody != null && heightCmFromBody > 0f)
                ? Math.round(heightCmFromBody)
                : 170;
        int w = (weightKgFromBody != null && weightKgFromBody > 0f)
                ? Math.round(weightKgFromBody)
                : 65;

        JsonObject userInfo = new JsonObject();
        userInfo.addProperty("age", 30);
        userInfo.addProperty("height", h); // cm (서버에서 cm→m 보정)
        userInfo.addProperty("weight", w); // kg
        body.add("userInfo", userInfo);

        JsonArray arr = new JsonArray();
        for (Map.Entry<Integer, Integer> e : qidToAns.entrySet()) {
            JsonObject item = new JsonObject();
            item.addProperty("questionId", e.getKey());
            item.addProperty("answerId", e.getValue());
            arr.add(item);
        }
        body.add("answers", arr);
        return body;
    }
}
