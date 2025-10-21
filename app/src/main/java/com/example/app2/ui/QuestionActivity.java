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

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private List<Question> questionList;
    private Button btnNext, btnPrev;
    private ProgressBar progressBar;
    private TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        recyclerView = findViewById(R.id.recyclerView);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);

        TextView tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSectionTitle.setText("성격 및 생활습관");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        int currentStep = 2;
        int totalStep = 6;
        if (progressBar != null) {
            progressBar.setMax(totalStep);
            progressBar.setProgress(currentStep);
        }
        if (tvProgress != null) tvProgress.setText(currentStep + " / " + totalStep + "단계");

        // 시스템 바 인셋 처리
        if (btnNext != null) {
            ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets) -> {
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setTranslationY(-bottomInset);
                return insets;
            });
        }
        if (btnPrev != null) {
            ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets) -> {
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setTranslationY(-bottomInset);
                return insets;
            });
        }

        // 문항(1~3)
        questionList = new ArrayList<>();
        questionList.add(new Question(1, "성격이 대범한 편인가요?",
                Arrays.asList("자주 그렇다", "가끔 그렇다", "거의 그렇지 않다")));
        questionList.add(new Question(2, "행동 속도는 어떤가요?",
                Arrays.asList("빠른 편이다", "보통이다", "느린 편이다")));
        questionList.add(new Question(3, "모든 일에 적극적인 편인가요?",
                Arrays.asList("적극적이다", "보통이다", "소극적이다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 이전 화면 (애니메이션 유지)
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                Intent intent = new Intent(QuestionActivity.this, BodyInfoActivity.class);
                startActivity(intent);
                try { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); } catch (Exception ignore) {}
            });
        }

        // 다음: 어댑터의 선택값 사용 → 검증 → 저장 → 다음 화면 (애니메이션 제거)
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                Map<Integer, Integer> ans = adapter.getAnswerMap(); // questionId -> answerId(1..n), 미선택은 -1

                // 이 페이지는 Q1~Q3 모두 선택되었는지 검사
                int[] must = {1, 2, 3};
                for (int qid : must) {
                    Integer a = ans.get(qid);
                    if (a == null || a < 1) {
                        Toast.makeText(this, "1~3번 문항을 모두 선택해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // 저장(SharedPreferences)
                SharedPreferences sp = getSharedPreferences("survey_answers", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                for (int qid : must) {
                    ed.putInt("ans_q_" + qid, ans.get(qid)); // 1..n 그대로 저장
                }
                ed.apply();

                // 다음 화면 (전환 애니메이션 호출 없이)
                Intent intent = new Intent(QuestionActivity.this, QuestionActivity2.class);
                startActivity(intent);
                // overridePendingTransition(...) 호출 없음
            });
        }
    }
}
