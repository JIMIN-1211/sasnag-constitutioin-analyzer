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

public class QuestionActivity4 extends AppCompatActivity {

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
        tvSectionTitle.setText("땀 및 체온조절");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        int currentStep = 4;
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
        questionList.add(new Question(11, "평소 땀을 많이 흘리는 편인가요?",
                Arrays.asList("많이 흘린다", "보통이다", "거의 흘리지 않는다")));
        questionList.add(new Question(12, "땀을 흘리고 난 뒤 기분이 어떤가요?",
                Arrays.asList("상쾌하다", "피곤하다", "아무런 차이가\n없다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        btnPrev.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity4.this, QuestionActivity3.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        });
        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity4.this, QuestionActivity5.class);
            startActivity(intent);
        });
    }
}