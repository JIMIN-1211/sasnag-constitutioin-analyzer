package com.example.app2.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 설문 문항 모델
 * - id: questionId (1..16)
 * - questionText: 질문 본문
 * - options: 보기 리스트(예: 3개)
 * - selectedOption: 선택된 보기 인덱스 (0..n-1), 미선택은 -1
 */
public class Question {

    private final int id;
    private final String questionText;
    private final List<String> options;
    private int selectedOption = -1; // 기본: 미선택

    public Question(int id, String questionText, List<String> options) {
        this.id = id;
        this.questionText = questionText != null ? questionText : "";
        // 외부에서 리스트를 변경해도 안전하도록 방어적 복사
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    /** 보기 리스트(읽기 전용) */
    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /** 선택된 보기 인덱스(0..n-1). 미선택은 -1 */
    public int getSelectedOption() {
        return selectedOption;
    }

    /**
     * 선택 인덱스 설정
     * @param index 0..(options.size()-1), 그 외 값은 -1(미선택)로 처리
     */
    public void setSelectedOption(int index) {
        if (index >= 0 && index < options.size()) {
            this.selectedOption = index;
        } else {
            this.selectedOption = -1;
        }
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", options=" + options +
                ", selectedOption=" + selectedOption +
                '}';
    }
}
