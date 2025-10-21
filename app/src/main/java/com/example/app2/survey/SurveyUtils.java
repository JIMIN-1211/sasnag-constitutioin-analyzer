package com.example.app2.survey;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SurveyUtils {
    private SurveyUtils(){}

    /** 루트 아래 모든 RadioGroup을 화면 표시 순서대로 수집 */
    public static List<RadioGroup> findRadioGroups(View root) {
        List<RadioGroup> out = new ArrayList<>();
        traverse(root, out);
        return out;
    }
    private static void traverse(View v, List<RadioGroup> out){
        if (v instanceof RadioGroup) out.add((RadioGroup) v);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i=0;i<vg.getChildCount();i++) traverse(vg.getChildAt(i), out);
        }
    }

    /** 선택된 라디오버튼이 몇 번째인지(1..n) → answerId 로 사용 */
    public static int selectedIndex1Based(RadioGroup g){
        int checkedId = g.getCheckedRadioButtonId();
        if (checkedId == -1) return -1;
        int k = 0;
        for (int i=0;i<g.getChildCount();i++){
            View child = g.getChildAt(i);
            if (child instanceof RadioButton){
                k++;
                if (child.getId() == checkedId) return k; // 1..n
            }
        }
        return -1;
    }

    /** groups를 startQ, startQ+1,… 에 매핑하여 (questionId→answerId) 수집 */
    public static Map<Integer,Integer> collectAnswers(List<RadioGroup> groups, int startQ){
        Map<Integer,Integer> out = new LinkedHashMap<>();
        for (int i=0;i<groups.size();i++){
            int idx = selectedIndex1Based(groups.get(i));
            if (idx <= 0) return null; // 미선택
            out.put(startQ + i, idx);
        }
        return out;
    }

    /** 레이아웃에서 적당한 ‘주요 버튼’ 하나 찾기(버튼 id 몰라도 동작) */
    public static Button findAnyPrimaryButton(Activity a){
        View root = ((ViewGroup)a.findViewById(android.R.id.content)).getChildAt(0);
        return findFirstButton(root);
    }
    private static Button findFirstButton(View v){
        if (v instanceof Button && v.isShown() && v.isEnabled()) return (Button)v;
        if (v instanceof ViewGroup){
            ViewGroup g = (ViewGroup)v;
            for (int i=0;i<g.getChildCount();i++){
                Button b = findFirstButton(g.getChildAt(i));
                if (b != null) return b;
            }
        }
        return null;
    }
}
