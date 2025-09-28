package com.example.app2.models;

import java.util.List;

public class Question {
    private int id;                 // 질문 번호
    private String questionText;    // 질문 내용
    private List<String> options;   // 답변 3개
    private int selectedOption = -1; // 선택된 답변 인덱스 (-1: 선택 안함)

    public Question(int id, String questionText, List<String> options) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
    }

    public int getId() { return id; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getSelectedOption() { return selectedOption; }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }
}