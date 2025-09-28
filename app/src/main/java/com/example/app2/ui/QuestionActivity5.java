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

public class QuestionActivity5 extends AppCompatActivity {

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
        tvSectionTitle.setText("배변 및 소변 습관");

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        int currentStep = 5;
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
        questionList.add(new Question(13, "대변이 마려울 때 참기 어려운가요?",
                Arrays.asList("자주 그렇다", "가끔 그렇다", "거의 없다")));
        questionList.add(new Question(14, "야간에 소변을 보러 몇 번이나 가나요?",
                Arrays.asList("0회", "1회", "2회 이상")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        btnPrev.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity5.this, QuestionActivity4.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        });
        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity5.this, QuestionActivity6.class);
            startActivity(intent);

        });
    }
}