package com.example.app2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.example.app2.models.Question;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private Context context;
    private List<Question> questionList;

    public QuestionAdapter(Context context, List<Question> questionList) {
        this.context = context;
        this.questionList = questionList;
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
        holder.tvQuestion.setText(question.getId() + ". " + question.getQuestionText());

        // 답변 버튼 텍스트 설정
        holder.btnOption1.setText(question.getOptions().get(0));
        holder.btnOption2.setText(question.getOptions().get(1));
        holder.btnOption3.setText(question.getOptions().get(2));

        // 버튼 상태 초기화
        resetButtonStates(holder);

        // 이전에 선택된 값 반영
        if (question.getSelectedOption() != -1) {
            highlightButton(holder, question.getSelectedOption());
        }

        // 클릭 이벤트 처리
        holder.btnOption1.setOnClickListener(v -> {
            question.setSelectedOption(0);
            notifyItemChanged(position);
        });

        holder.btnOption2.setOnClickListener(v -> {
            question.setSelectedOption(1);
            notifyItemChanged(position);
        });

        holder.btnOption3.setOnClickListener(v -> {
            question.setSelectedOption(2);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        Button btnOption1, btnOption2, btnOption3;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            btnOption1 = itemView.findViewById(R.id.btnOption1);
            btnOption2 = itemView.findViewById(R.id.btnOption2);
            btnOption3 = itemView.findViewById(R.id.btnOption3);
        }
    }

    // 모든 버튼을 기본 상태로
    private void resetButtonStates(QuestionViewHolder holder) {
        holder.btnOption1.setSelected(false);
        holder.btnOption2.setSelected(false);
        holder.btnOption3.setSelected(false);
    }

    // 선택된 버튼만 강조
    private void highlightButton(QuestionViewHolder holder, int selectedIndex) {
        if (selectedIndex == 0) holder.btnOption1.setSelected(true);
        if (selectedIndex == 1) holder.btnOption2.setSelected(true);
        if (selectedIndex == 2) holder.btnOption3.setSelected(true);
    }
}