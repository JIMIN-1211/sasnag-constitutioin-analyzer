package com.example.app2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app2.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * [신규] 'mealRecyclerView'를 제어하는 어댑터
 */
public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private JsonArray mealList;
    private final LayoutInflater inflater;

    // 'X' 버튼 클릭 시 Activity에 알리기 위한 인터페이스
    private OnMealDeleteListener deleteListener;

    // 생성자
    public MealAdapter(Context context, JsonArray mealList) {
        this.inflater = LayoutInflater.from(context);
        this.mealList = mealList;
    }

    // Activity가 'X' 버튼 클릭을 감지할 수 있도록 리스너 설정
    public void setOnDeleteListener(OnMealDeleteListener listener) {
        this.deleteListener = listener;
    }

    /**
     * list_item_meal.xml (한 줄) 레이아웃을 Java 객체로 만듭니다.
     */
    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_meal, parent, false);
        return new MealViewHolder(view);
    }

    /**
     * '콩물' 데이터를 '콩물' 뷰에 바인딩(연결)합니다.
     */
    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        JsonObject meal = mealList.get(position).getAsJsonObject();
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return mealList != null ? mealList.size() : 0;
    }

    /**
     * [신규] 데이터를 새로고침(업데이트)하는 메서드
     */
    public void updateData(JsonArray newList) {
        this.mealList = newList;
        notifyDataSetChanged(); // RecyclerView 새로고침
    }

    // =================================================================
    //  ViewHolder (list_item_meal.xml의 뷰들을 제어)
    // =================================================================
    class MealViewHolder extends RecyclerView.ViewHolder {

        // list_item_meal.xml의 ID들
        private final TextView tvItemFoodName;
        private final TextView tvItemFoodInfo;
        private final ImageButton btnDeleteMeal;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            // findViewById는 여기서 실행합니다.
            tvItemFoodName = itemView.findViewById(R.id.tvItemFoodName);
            tvItemFoodInfo = itemView.findViewById(R.id.tvItemFoodInfo);
            btnDeleteMeal = itemView.findViewById(R.id.btnDeleteMeal);
        }

        /**
         * 데이터를 받아서 텍스트를 채웁니다.
         */
        public void bind(JsonObject meal) {
            String foodName = meal.get("food_name").getAsString();
            double kcal = meal.get("calories_consumed_session").getAsDouble();
            double gram = meal.get("intake_gram").getAsDouble();
            long mealLogId = meal.get("id").getAsLong(); // 삭제 시 사용할 '기록 ID'

            tvItemFoodName.setText(foodName);
            tvItemFoodInfo.setText(String.format(Locale.KOREA, "%.0f kcal, %.0fg", kcal, gram));

            // 'X' 버튼 클릭 리스너 설정
            btnDeleteMeal.setOnClickListener(v -> {
                if (deleteListener != null) {
                    // Activity에 "이 ID(mealLogId)를 삭제해줘"라고 알림
                    deleteListener.onDeleteClick(mealLogId, getAdapterPosition());
                }
            });
        }
    }

    // =================================================================
    //  클릭 리스너 인터페이스
    // =================================================================

    /**
     * 'X' 버튼 클릭을 Activity에 전달하기 위한 인터페이스
     */
    public interface OnMealDeleteListener {
        void onDeleteClick(long mealLogId, int position);
    }
}