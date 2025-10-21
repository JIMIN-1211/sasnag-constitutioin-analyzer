package com.example.app2.survey;

import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireState {
    private static final QuestionnaireState I = new QuestionnaireState();
    private final SparseIntArray answers = new SparseIntArray();
    public static QuestionnaireState get(){ return I; }

    public void setAnswer(int questionId, int answerId){ answers.put(questionId, answerId); }

    public List<AnswerDto> toList(){
        List<AnswerDto> out = new ArrayList<>();
        for (int i=0;i<answers.size();i++){
            out.add(new AnswerDto(answers.keyAt(i), answers.valueAt(i)));
        }
        return out;
    }

    public void clear(){ answers.clear(); }
}

