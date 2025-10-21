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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<Question> questionList;

    // key = position(0..N-1), value = 선택 인덱스(0..2). 미선택은 저장하지 않음(기본 -1로 취급)
    private final SparseIntArray selections = new SparseIntArray();

    public QuestionAdapter(Context context, List<Question> questionList) {
        this.context = context;
        this.questionList = questionList != null ? questionList : new ArrayList<>();
        setHasStableIds(false);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questionList.get(position);

        // 제목
        holder.tvQuestion.setText(question.getId() + ". " + question.getQuestionText());

        // 답변 버튼 텍스트 (최소 3개 가정)
        List<String> opts = question.getOptions();
        holder.btnOption1.setText(opts.size() > 0 ? opts.get(0) : "옵션1");
        holder.btnOption2.setText(opts.size() > 1 ? opts.get(1) : "옵션2");
        holder.btnOption3.setText(opts.size() > 2 ? opts.get(2) : "옵션3");

        // 저장된 선택 상태 복원 (우선순위: selections -> model)
        int saved = selections.get(position, -1);
        if (saved == -1) saved = question.getSelectedOption(); // 0..2 or -1

        resetButtonStates(holder);
        if (saved >= 0) {
            highlightButton(holder, saved);
        }

        // 클릭 리스너: 즉시 UI 반영 + 상태 저장
        holder.btnOption1.setOnClickListener(v -> select(holder, 0));
        holder.btnOption2.setOnClickListener(v -> select(holder, 1));
        holder.btnOption3.setOnClickListener(v -> select(holder, 2));
    }

    private void select(@NonNull QuestionViewHolder holder, int idx0) {
        // 호환성 있는 위치 API
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        // 상태 저장(어댑터 & 모델)
        selections.put(pos, idx0);
        Question q = questionList.get(pos);
        q.setSelectedOption(idx0);

        // UI 갱신(현재 아이템만 즉시)
        resetButtonStates(holder);
        highlightButton(holder, idx0);
        // notifyItemChanged(pos); // 현재 방식은 직접 갱신하므로 불필요
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQuestion;
        final Button btnOption1, btnOption2, btnOption3;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            btnOption1 = itemView.findViewById(R.id.btnOption1);
            btnOption2 = itemView.findViewById(R.id.btnOption2);
            btnOption3 = itemView.findViewById(R.id.btnOption3);
        }
    }

    // ------ 공개 메서드: 액티비티에서 쉽게 사용 ------

    /** position 0..N-1 순서대로 answerId(1..n)를 반환. 미선택은 -1 */
    public List<Integer> getSelections() {
        int n = getItemCount();
        List<Integer> out = new ArrayList<>(n);
        for (int pos = 0; pos < n; pos++) {
            int sel0 = selections.get(pos, questionList.get(pos).getSelectedOption());
            out.add(sel0 >= 0 ? (sel0 + 1) : -1); // 1-based 로 변환
        }
        return out;
    }

    /** questionId -> answerId(1..n) 매핑을 반환. 미선택은 -1 */
    public Map<Integer, Integer> getAnswerMap() {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        int n = getItemCount();
        for (int pos = 0; pos < n; pos++) {
            int qid = questionList.get(pos).getId();
            int sel0 = selections.get(pos, questionList.get(pos).getSelectedOption());
            map.put(qid, sel0 >= 0 ? (sel0 + 1) : -1);
        }
        return map;
    }

    /** 외부에서 저장해둔 (questionId -> answerId 1..n) 를 어댑터에 주입해 복원 */
    public void restoreFrom(Map<Integer, Integer> qidToAns) {
        if (qidToAns == null) return;
        selections.clear();
        for (int pos = 0; pos < questionList.size(); pos++) {
            Question q = questionList.get(pos);
            Integer ans1 = qidToAns.get(q.getId());
            int sel0 = (ans1 != null && ans1 > 0) ? (ans1 - 1) : -1;
            q.setSelectedOption(sel0);
            if (sel0 >= 0) selections.put(pos, sel0);
        }
        notifyDataSetChanged();
    }

    // ------ 내부 헬퍼 ------

    private void resetButtonStates(QuestionViewHolder holder) {
        holder.btnOption1.setSelected(false);
        holder.btnOption2.setSelected(false);
        holder.btnOption3.setSelected(false);
    }

    private void highlightButton(QuestionViewHolder holder, int selectedIndex0) {
        if (selectedIndex0 == 0) holder.btnOption1.setSelected(true);
        if (selectedIndex0 == 1) holder.btnOption2.setSelected(true);
        if (selectedIndex0 == 2) holder.btnOption3.setSelected(true);
    }
}
