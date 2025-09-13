package com.example.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionActivity extends AppCompatActivity {

    private Button btnOption1, btnOption2, btnOption3, btnPrev, btnNext;
    private ProgressBar progressBar;
    private TextView tvProgress, tvQuestion;

    // 질문 16개
    private String[] questions = {
            "성격이 대범한 편인가요?",
            "행동 속도는 어떤가요?",
            "모든 일에 적극적인 편인가요?",
            "성격이 외향적인 편인가요?",
            "남성적인 성향이 강한가요, 여성적인 성향이 강한가요?",
            "가끔 쉽게 흥분하거나 감정 기복이 있는 편인가요?",
            "평소 소화 상태는 어떠신가요?",
            "식욕 상태는 어떤가요?",
            "야식을 자주 드시나요?",
            "기름진 음식이나 단 음식을 선호하시나요?",
            "평소 땀을 많이 흘리는 편인가요?",
            "땀을 흘리고 난 뒤 기분이 어떤가요?",
            "대변이 마려울 때 참기 어려운가요?",
            "야간에 소변을 보러 몇 번이나 가나요?",
            "추위와 더위 중 어느 것이 더 힘든가요?",
            "평소 마시는 물의 온도는?"
    };

    // 질문별 답변 (질문 개수 × 3)
    private String[][] options = {
            {"매우 대범하다", "보통이다", "섬세한 편이다"},
            {"빠른 편이다", "보통이다", "느린 편이다"},
            {"적극적이다", "보통이다", "소극적이다"},
            {"외향적이다", "보통이다", "내성적이다"},
            {"남성적이다", "보통이다", "여성적이다"},
            {"자주 그렇다", "가끔 그렇다", "거의 없다"},
            {"소화가 잘 된다", "소화가 잘 안 되지만 불편하지 않다", "소화가 잘 안되고 불편함이 있다"},
            {"매우 좋다", "보통이다", "입맛이 없다"},
            {"거의 먹지 않는다", "가끔 먹는다", "자주 먹는다"},
            {"좋아한다", "보통이다", "별로 좋아하지 않는다"},
            {"많이 흘린다", "보통이다", "거의 흘리지 않는다"},
            {"상쾌하다", "피곤하다", "아무런 차이가 없다"},
            {"자주 그렇다", "가끔 그렇다", "거의 없다"},
            {"0회", "1회", "2회 이상"},
            {"추위가 더 힘들다", "더위가 더 힘들다", "둘 다 괜찮다"},
            {"따뜻한 물을 선호한다", "찬 물을 선호한다", "특별히 신경 쓰지 않는다"}
    };

    private int currentIndex = 0;
    private int[] answers = new int[questions.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        String height = getIntent().getStringExtra("height");
        String weight = getIntent().getStringExtra("weight");


        bindViews();
        initAnswers();
        updateQuestion();

        btnOption1.setOnClickListener(v -> selectOption(1));
        btnOption2.setOnClickListener(v -> selectOption(2));
        btnOption3.setOnClickListener(v -> selectOption(3));

        btnNext.setOnClickListener(v -> {
            if (currentIndex < questions.length - 1) {
                currentIndex++;
                updateQuestion();
            } else {
                // TODO: 마지막 질문 완료 후 결과 페이지로 이동
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateQuestion();
            }
        });
    }

    private void bindViews() {
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
    }

    private void initAnswers() {
        for (int i = 0; i < answers.length; i++) {
            answers[i] = -1;
        }
    }

    private void updateQuestion() {
        tvQuestion.setText(questions[currentIndex]);
        tvProgress.setText(((currentIndex + 1)/3) + " / " + (questions.length/3));
        progressBar.setMax(questions.length/3);
        progressBar.setProgress((currentIndex + 1)/3);
        // 질문 업데이트
        tvQuestion.setText(questions[currentIndex]);
        // 버튼에 해당 질문의 답변 설정
        btnOption1.setText(options[currentIndex][0]);
        btnOption2.setText(options[currentIndex][1]);
        btnOption3.setText(options[currentIndex][2]);

        resetOptions();

        if (answers[currentIndex] != -1) {
            if (answers[currentIndex] == 1) highlight(btnOption1);
            else if (answers[currentIndex] == 2) highlight(btnOption2);
            else if (answers[currentIndex] == 3) highlight(btnOption3);
            btnNext.setEnabled(true);
        } else {
            btnNext.setEnabled(false);
        }
    }

    private void selectOption(int value) {
        answers[currentIndex] = value;
        resetOptions();

        if (value == 1) highlight(btnOption1);
        else if (value == 2) highlight(btnOption2);
        else if (value == 3) highlight(btnOption3);

        btnNext.setEnabled(true);
    }

    private void resetOptions() {
        btnOption1.setBackgroundColor(0xFFF9F9F9);
        btnOption2.setBackgroundColor(0xFFF9F9F9);
        btnOption3.setBackgroundColor(0xFFF9F9F9);
    }

    private void highlight(Button btn) {
        btn.setBackgroundColor(0xFFDCEDC8); // 연두색 강조
    }
}

