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

public class QuestionActivity3 extends AppCompatActivity {

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
        questionList.add(new Question(7, "평소 소화 상태는 어떠신가요?",
                Arrays.asList("소화가 잘 된다", "소화가 잘 안되지만\n불편하지 않다", "소화가 잘 안되고\n불편함이 있다")));
        questionList.add(new Question(8, "식욕 상태는 어떤가요?",
                Arrays.asList("매우 좋다", "보통이다", "입맛이 없다")));
        questionList.add(new Question(9, "야식을 자주 드시나요?",
                Arrays.asList("거의 먹지 않는다", "가끔 먹는다", "자주 먹는다")));
        questionList.add(new Question(10, "기름진 음식이나 단 음식을 선호하시나요?",
                Arrays.asList("좋아한다", "보통이다", "별로 좋아하지\n않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity3.this, QuestionActivity4.class);
            startActivity(intent);
        });
    }
}
