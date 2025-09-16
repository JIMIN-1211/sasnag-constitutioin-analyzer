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

public class QuestionActivity2 extends AppCompatActivity {

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
        questionList.add(new Question(4, "성격이 외향적인 편인가요?",
                Arrays.asList("외향적이다", "보통이다", "내성적이다")));
        questionList.add(new Question(5, "남성적인 성향이 강한가요, 여성적인 성향이 강한가요?",
                Arrays.asList("남성적이다", "보통이다", "여성적이다")));
        questionList.add(new Question(6, "가끔 쉽게 흥분하거나 감정 기복이 있는 편인가요?",
                Arrays.asList("자주 그렇다", "가끔 그렇다", "거의 없다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity2.this, QuestionActivity3.class);
            startActivity(intent);
        });
    }
}
