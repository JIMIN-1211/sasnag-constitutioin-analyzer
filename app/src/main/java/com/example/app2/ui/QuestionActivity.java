package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.adapters.QuestionAdapter;
import com.example.app2.models.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


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

        progressBar.setMax(totalStep);
        progressBar.setProgress(currentStep);
        tvProgress.setText(currentStep + " / " + totalStep + "단계");

        // WindowInsets 적용
        ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets) -> {
            // 네비게이션 바 높이만큼 버튼 아래쪽에 padding 추가
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setTranslationY(-bottomInset);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setTranslationY(-bottomInset);
            return insets;
        });


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

        btnPrev.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity.this, BodyInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        });

        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity.this, QuestionActivity2.class);
            startActivity(intent);
        });

    }
}