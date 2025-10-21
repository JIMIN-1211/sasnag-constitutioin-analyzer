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

public class QuestionActivity4 extends AppCompatActivity {

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
        tvSectionTitle.setText("땀 및 체온 조절");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        int currentStep = 4, totalStep = 6;
        if (progressBar != null) { progressBar.setMax(totalStep); progressBar.setProgress(currentStep); }
        if (tvProgress != null) tvProgress.setText(currentStep + " / " + totalStep + "단계");

        if (btnNext != null) ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});
        if (btnPrev != null) ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets)->{ v.setTranslationY(-insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom); return insets;});

        // 문항(11~12)
        questionList = new ArrayList<>();
        questionList.add(new Question(11, "평소 땀을 많이 흘리는 편인가요?", Arrays.asList("많이 흘린다", "보통이다", "거의 흘리지 않는다")));
        questionList.add(new Question(12, "땀을 흘리고 난 뒤 기분이 어떤가요?", Arrays.asList("상쾌하다", "피곤하다", "아무런 차이가\n없다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (btnPrev != null) btnPrev.setOnClickListener(v -> {
            startActivity(new Intent(this, QuestionActivity3.class));
            try { overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);} catch (Exception ignore) {}
        });

        if (btnNext != null) btnNext.setOnClickListener(v -> {
            Map<Integer,Integer> ans = adapter.getAnswerMap();
            int[] must = {11,12};
            for (int qid : must) {
                Integer a = ans.get(qid);
                if (a == null || a < 1) { Toast.makeText(this, "11~12번 문항을 모두 선택해주세요", Toast.LENGTH_SHORT).show(); return; }
            }
            SharedPreferences sp = getSharedPreferences("survey_answers", MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            for (int qid : must) ed.putInt("ans_q_" + qid, ans.get(qid));
            ed.apply();

            startActivity(new Intent(this, QuestionActivity5.class));
        });
    }
}
