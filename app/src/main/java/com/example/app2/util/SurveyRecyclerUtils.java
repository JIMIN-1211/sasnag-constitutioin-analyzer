package com.example.app2.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public final class SurveyRecyclerUtils {
    private SurveyRecyclerUtils(){}

    /** 화면에 보이는 RecyclerView를 트리에서 찾아 반환 */
    public static RecyclerView findFirstRecyclerView(View root){
        if (root instanceof RecyclerView) return (RecyclerView) root;
        if (root instanceof ViewGroup){
            ViewGroup g = (ViewGroup) root;
            for (int i=0;i<g.getChildCount();i++){
                RecyclerView rv = findFirstRecyclerView(g.getChildAt(i));
                if (rv != null) return rv;
            }
        }
        return null;
    }

    /** 하나의 item view 안에서 첫 번째 RadioGroup을 찾아 반환 */
    private static RadioGroup findFirstRadioGroup(View root){
        if (root instanceof RadioGroup) return (RadioGroup) root;
        if (root instanceof ViewGroup){
            ViewGroup g = (ViewGroup) root;
            for (int i=0;i<g.getChildCount();i++){
                RadioGroup rg = findFirstRadioGroup(g.getChildAt(i));
                if (rg != null) return rg;
            }
        }
        return null;
    }

    /** 선택된 라디오버튼의 "순서"(1..n)를 answerId로 계산 */
    private static int selectedIndex1Based(RadioGroup g){
        if (g == null) return -1;
        int checkedId = g.getCheckedRadioButtonId();
        if (checkedId == -1) return -1;
        int k = 0;
        for (int i=0;i<g.getChildCount();i++){
            View c = g.getChildAt(i);
            if (c instanceof RadioButton){
                k++;
                if (c.getId() == checkedId) return k; // 1..n
            }
        }
        return -1;
    }

    /**
     * 현재 화면에 보이는 RecyclerView의 자식 item들을 순서대로 훑어서
     * 각 문항의 answerId(=선택 인덱스 1..n)를 뽑아낸다.
     * 주의: 한 화면에 해당 페이지 문항들이 "모두 보이는" 구조(현재 구현)일 때 적합.
     */
    public static List<Integer> collectAnswerIndicesFromRecycler(RecyclerView rv){
        List<Integer> out = new ArrayList<>();
        if (rv == null) return out;
        for (int i=0;i<rv.getChildCount();i++){
            View item = rv.getChildAt(i);
            RadioGroup rg = findFirstRadioGroup(item);
            int idx = selectedIndex1Based(rg);
            out.add(idx); // 선택 안했으면 -1
        }
        return out;
    }
}
