package com.example.app2.models;

import java.util.List;
import com.example.app2.survey.AnswerDto;

public class AnalyzeRequest {
    public int userId;
    public UserInfo userInfo;
    public List<AnswerDto> answers;

    public static class UserInfo {
        public int age;
        public double height;
        public double weight;
        public double bmi;
    }
}
