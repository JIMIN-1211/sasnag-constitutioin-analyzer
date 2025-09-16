package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.adapters.QuestionAdapter;
import com.example.app.models.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionActivity4 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private List<Question> questionList;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        recyclerView = findViewById(R.id.recyclerView);
        btnNext = findViewById(R.id.btnNext);

        questionList = new ArrayList<>();
        questionList.add(new Question(11, "평소 땀을 많이 흘리는 편인가요?",
                Arrays.asList("많이 흘린다", "보통이다", "거의 흘리지 않는다")));
        questionList.add(new Question(12, "땀을 흘리고 난 뒤 기분이 어떤가요?",
                Arrays.asList("상쾌하다", "피곤하다", "아무런 차이가\n없다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity4.this, QuestionActivity5.class);
            startActivity(intent);
        });
    }
}
