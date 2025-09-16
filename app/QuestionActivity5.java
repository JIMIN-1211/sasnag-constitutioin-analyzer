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

public class QuestionActivity5 extends AppCompatActivity {

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
        questionList.add(new Question(13, "대변이 마려울 때 참기 어려운가요?",
                Arrays.asList("자주 그렇다", "가끔 그렇다", "거의 없다")));
        questionList.add(new Question(14, "야간에 소변을 보러 몇 번이나 가나요?",
                Arrays.asList("0회", "1회", "2회 이상")));
        questionList.add(new Question(15, "추위와 더위 중 어느 것이 더 힘든가요?",
                Arrays.asList("추위가\n더 힘들다", "더위가\n더 힘들다", "둘 다 괜찮다")));
        questionList.add(new Question(16, "평소 마시는 물의 온도는?",
                Arrays.asList("따듯한 물을\n선호한다", "찬 물을\n선호한다", "특별히\n신경쓰지 않는다")));

        adapter = new QuestionAdapter(this, questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 다음 페이지 이동
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionActivity5.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
