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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QuestionActivity6 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private List<Question> questionList;
    private Button btnNext, btnPrev;
    private ProgressBar progressBar;
    private TextView tvProgress;

    @Override protected void onCreate(Bundle savedInstanceState) {
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

        if (btnNext != null) ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});
        if (btnPrev != null) ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});

        // 문항(15~16)
        questionList = new ArrayList<>();
        questionList.add(new Question(15, "추위와 더위 중 어느 것이 더 힘든가요?", Arrays.asList("추위가\n더 힘들다", "더위가\n더 힘들다", "둘 다 괜찮다")));
        questionList.add(new Question(16, "평소 마시는 물의 온도는?", Arrays.asList("따듯한 물을\n선호한다", "찬 물을\n선호한다", "특별히\n신경쓰지 않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (btnPrev != null) btnPrev.setOnClickListener(v -> {
            startActivity(new Intent(this, QuestionActivity5.class));
            try { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);} catch (Exception ignore) {}
        });

        if (btnNext != null) {
            btnNext.setText("제출");
            btnNext.setOnClickListener(v -> {
                Map<Integer,Integer> ans = adapter.getAnswerMap();
                int[] must = {15,16};
                for (int qid : must) {
                    Integer a = ans.get(qid);
                    if (a == null || a < 1) { Toast.makeText(this, "15~16번 문항을 모두 선택해주세요", Toast.LENGTH_SHORT).show(); return; }
                }
                SharedPreferences sp = getSharedPreferences("survey_answers", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                for (int qid : must) ed.putInt("ans_q_" + qid, ans.get(qid));
                ed.apply();

                // (선택) 결과화면으로 이동 — constitution/score는 서버 연동 후 세팅 권장
                Intent intent = new Intent(this, ResultActivity.class);

                // 온보딩에서 저장해둔 기본 신체정보가 있으면 결과 화면에 전달 (없어도 안전)
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                float height = (float) Double.longBitsToDouble(prefs.getLong("height", Double.doubleToLongBits(170.0)));
                float weight = (float) Double.longBitsToDouble(prefs.getLong("weight", Double.doubleToLongBits(65.0)));
                String name = prefs.getString("name", "홍길동");

                intent.putExtra("name", name);
                intent.putExtra("heightCm", height);
                intent.putExtra("weightKg", weight);
                // intent.putExtra("constitution", "태음인"); // 서버 결과 있으면 설정

                startActivity(intent);
                // next 애니메이션 없음
                finish();
            });
        }
    }
}
