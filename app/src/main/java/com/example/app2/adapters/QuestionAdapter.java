package com.example.app2.adapters;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.models.Question;
import com.example.app2.survey.AnswerStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 3지선다 설문 어댑터 (단일 페이지 스크롤)
 * - item_question.xml (tvQuestion, btnOption1~3)과 호환
 * - AnswerStore(질문ID 기준)와 내부 selections(포지션 기준) 양쪽 복원
 * - 선택 시 진행률 콜백 제공
 */
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.VH> {

    /** 선택 건수 바뀔 때 알려주고 싶을 때 (예: 진행률/제출버튼 활성화) */
    public interface OnAnswerChanged {
        void onChanged(int answeredCount, int totalCount);
    }

    private final Context context;
    private final List<Question> items = new ArrayList<>();
    // key = position(0..N-1), value = 선택 인덱스(0..2). (-1은 미선택)
    private final SparseIntArray selections = new SparseIntArray();
    private final OnAnswerChanged progressListener;

    public QuestionAdapter(@NonNull Context context,
                           @NonNull List<Question> questionList,
                           OnAnswerChanged listener) {
        this.context = context;
        if (questionList != null) this.items.addAll(questionList);
        this.progressListener = listener;
        setHasStableIds(true);
    }
    public QuestionAdapter(@NonNull Context context,
                           @NonNull List<Question> questionList) {
        this(context, questionList, null); // 3-인자 생성자에 위임
    }

    // Question의 고유 id가 있으면 안정적인 ID로 사용
    @Override public long getItemId(int position) {
        if (position < 0 || position >= items.size()) return RecyclerView.NO_ID;
        return items.get(position).getId();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Question q = items.get(position);

        // 질문 텍스트
        h.tvQuestion.setText(q.getId() + ". " + q.getQuestionText());

        // 보기 텍스트
        List<String> opts = q.getOptions();
        h.btn1.setText(opts.size() > 0 ? opts.get(0) : "옵션1");
        h.btn2.setText(opts.size() > 1 ? opts.get(1) : "옵션2");
        h.btn3.setText(opts.size() > 2 ? opts.get(2) : "옵션3");

        // ----- 선택 상태 복원 -----
        // 1) 전역 저장소(QuestionId 기준)가 최우선
        int stored = AnswerStore.get().get(q.getId()); // 0..2 또는 -1
        // 2) 없으면 - 포지션 기준 selections / 모델의 selectedOption 순서로 폴백
        int sel0 = (stored >= 0)
                ? stored
                : selections.get(position,
                q.getSelectedOption() >= 0 ? q.getSelectedOption() : -1);

        applySelectedState(h, sel0);

        // ----- 클릭 핸들러 -----
        h.btn1.setOnClickListener(v -> handleSelect(h, 0));
        h.btn2.setOnClickListener(v -> handleSelect(h, 1));
        h.btn3.setOnClickListener(v -> handleSelect(h, 2));
    }

    private void handleSelect(@NonNull VH h, int idx0) {
        int pos = h.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        Question q = items.get(pos);

        // 선택 저장 (포지션/질문ID/모델 세 군데 모두 동기화)
        selections.put(pos, idx0);
        q.setSelectedOption(idx0);
        AnswerStore.get().put(q.getId(), idx0);

        applySelectedState(h, idx0);

        if (progressListener != null) {
            progressListener.onChanged(getAnsweredCount(), getItemCount());
        }
    }

    private void applySelectedState(@NonNull VH h, int sel0) {
        // 버튼 상태 초기화
        setBtnSelected(h.btn1, false);
        setBtnSelected(h.btn2, false);
        setBtnSelected(h.btn3, false);
        // 선택 반영
        if (sel0 == 0) setBtnSelected(h.btn1, true);
        else if (sel0 == 1) setBtnSelected(h.btn2, true);
        else if (sel0 == 2) setBtnSelected(h.btn3, true);
    }

    // selector가 state_selected/activated 둘 다 대응할 수 있게 둘 다 세팅
    private void setBtnSelected(Button b, boolean selected) {
        b.setSelected(selected);
        b.setActivated(selected);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ------------------ 외부 접근용 유틸 ------------------

    /** 현재까지 선택한 문항 수 */
    public int getAnsweredCount() {
        int count = 0;
        final int n = getItemCount();
        for (int pos = 0; pos < n; pos++) {
            int sel0 = selections.get(pos,
                    items.get(pos).getSelectedOption() >= 0 ? items.get(pos).getSelectedOption() : -1);
            if (sel0 >= 0) count++;
        }
        return count;
    }

    /** position 순서대로 answerId(1..3), 미선택은 -1 */
    public List<Integer> getSelections() {
        int n = getItemCount();
        List<Integer> out = new ArrayList<>(n);
        for (int pos = 0; pos < n; pos++) {
            int sel0 = selections.get(pos,
                    items.get(pos).getSelectedOption() >= 0 ? items.get(pos).getSelectedOption() : -1);
            out.add(sel0 >= 0 ? (sel0 + 1) : -1);
        }
        return out;
    }

    /** questionId -> answerId(1..3), 미선택은 -1 */
    public Map<Integer, Integer> getAnswerMap() {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        int n = getItemCount();
        for (int pos = 0; pos < n; pos++) {
            Question q = items.get(pos);
            int sel0 = selections.get(pos,
                    q.getSelectedOption() >= 0 ? q.getSelectedOption() : -1);
            map.put(q.getId(), sel0 >= 0 ? (sel0 + 1) : -1);
        }
        return map;
    }

    /** (questionId -> answerId 1..3) 외부 저장값으로 전체 복원 */
    public void restoreFrom(Map<Integer, Integer> qidToAns) {
        if (qidToAns == null) return;
        selections.clear();
        for (int pos = 0; pos < items.size(); pos++) {
            Question q = items.get(pos);
            Integer ans1 = qidToAns.get(q.getId());
            int sel0 = (ans1 != null && ans1 > 0) ? (ans1 - 1) : -1;
            q.setSelectedOption(sel0);
            if (sel0 >= 0) selections.put(pos, sel0);
        }
        notifyDataSetChanged();
        if (progressListener != null) {
            progressListener.onChanged(getAnsweredCount(), getItemCount());
        }
    }

    /** 리스트 교체 (예: 16문항 세팅 교체) */
    public void setData(List<Question> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        selections.clear();
        notifyDataSetChanged();
        if (progressListener != null) {
            progressListener.onChanged(getAnsweredCount(), getItemCount());
        }
    }

    // ------------------ 뷰홀더 ------------------

    public static class VH extends RecyclerView.ViewHolder {
        final TextView tvQuestion;
        final Button btn1, btn2, btn3;
        public VH(@NonNull View v) {
            super(v);
            tvQuestion = v.findViewById(R.id.tvQuestion);
            btn1 = v.findViewById(R.id.btnOption1);
            btn2 = v.findViewById(R.id.btnOption2);
            btn3 = v.findViewById(R.id.btnOption3);
        }
    }
}
