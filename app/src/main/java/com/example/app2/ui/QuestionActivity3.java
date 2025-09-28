package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class QuestionActivity3 extends AppCompatActivity {

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
        tvSectionTitle.setText("소화 및 식습관");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        int currentStep = 3;
        int totalStep = 6;

        progressBar.setMax(totalStep);
        progressBar.setProgress(currentStep);
        tvProgress.setText(currentStep + " / " + totalStep + "단계");

        ViewCompat.setOnApplyWindowInsetsListener(btnNext, (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setTranslationY(-bottomInset);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(btnPrev, (v, insets) -> {
            // 네비게이션 바 높이만큼 버튼 아래쪽에 padding 추가
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setTranslationY(-bottomInset);
            return insets;
        });


        questionList = new ArrayList<>();
        questionList.add(new Question(7, "평소 소화 상태는 어떠신가요?",
                Arrays.asList("매우 좋다", "보통이다", "소화가 잘 되지\n않는다")));
        questionList.add(new Question(8, "식욕 상태는 어떤가요?",
                Arrays.asList("매우 좋다", "보통이다", "입맛이 없다")));
        questionList.add(new Question(9, "야식을 자주 드시나요?",
                Arrays.asList("거의 먹지 않는다", "가끔 먹는다", "자주 먹는다")));
        questionList.add(new Question(10, "기름진 음식이나 단 음식을 선호하시나요?",
                Arrays.asList("좋아한다", "보통이다", "별로 좋아하지\n않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        btnPrev.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity3.this, QuestionActivity2.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        });
        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity3.this, QuestionActivity4.class);
            startActivity(intent);
        });
    }
}