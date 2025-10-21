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

public class QuestionActivity3 extends AppCompatActivity {

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
        tvSectionTitle.setText("소화 및 식습관");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        int currentStep = 3, totalStep = 6;
        if (progressBar != null) { progressBar.setMax(totalStep); progressBar.setProgress(currentStep); }
        if (tvProgress != null) tvProgress.setText(currentStep + " / " + totalStep + "단계");

        if (btnNext != null) ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});
        if (btnPrev != null) ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});

        // 문항(7~10)
        questionList = new ArrayList<>();
        questionList.add(new Question(7,  "평소 소화 상태는 어떠신가요?", Arrays.asList("매우 좋다", "보통이다", "소화가 잘 되지\n않는다")));
        questionList.add(new Question(8,  "식욕 상태는 어떤가요?", Arrays.asList("매우 좋다", "보통이다", "입맛이 없다")));
        questionList.add(new Question(9,  "야식을 자주 드시나요?", Arrays.asList("거의 먹지 않는다", "가끔 먹는다", "자주 먹는다")));
        questionList.add(new Question(10, "기름진 음식이나 단 음식을 선호하시나요?", Arrays.asList("좋아한다", "보통이다", "별로 좋아하지\n않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (btnPrev != null) btnPrev.setOnClickListener(v -> {
            startActivity(new Intent(this, QuestionActivity2.class));
            try { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);} catch (Exception ignore) {}
        });

        if (btnNext != null) btnNext.setOnClickListener(v -> {
            Map<Integer,Integer> ans = adapter.getAnswerMap();
            int[] must = {7,8,9,10};
            for (int qid : must) {
                Integer a = ans.get(qid);
                if (a == null || a < 1) { Toast.makeText(this, "7~10번 문항을 모두 선택해주세요", Toast.LENGTH_SHORT).show(); return; }
            }
            SharedPreferences sp = getSharedPreferences("survey_answers", MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            for (int qid : must) ed.putInt("ans_q_" + qid, ans.get(qid));
            ed.apply();

            startActivity(new Intent(this, QuestionActivity4.class));
        });
    }
}
