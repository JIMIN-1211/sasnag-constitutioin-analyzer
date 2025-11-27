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
 * [신규] 'workoutRecyclerView'를 제어하는 어댑터
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private JsonArray workoutList;
    private final LayoutInflater inflater;
    private OnWorkoutDeleteListener deleteListener;

    public WorkoutAdapter(Context context, JsonArray workoutList) {
        this.inflater = LayoutInflater.from(context);
        this.workoutList = workoutList;
    }

    public void setOnDeleteListener(OnWorkoutDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        JsonObject workout = workoutList.get(position).getAsJsonObject();
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workoutList != null ? workoutList.size() : 0;
    }

    public void updateData(JsonArray newList) {
        this.workoutList = newList;
        notifyDataSetChanged();
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemWorkoutName;
        private final TextView tvItemWorkoutInfo;
        private final ImageButton btnDeleteWorkout;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemWorkoutName = itemView.findViewById(R.id.tvItemWorkoutName);
            tvItemWorkoutInfo = itemView.findViewById(R.id.tvItemWorkoutInfo);
            btnDeleteWorkout = itemView.findViewById(R.id.btnDeleteWorkout);
        }

        public void bind(JsonObject workout) {
            String workoutName = workout.get("exercise_name").getAsString();
            double kcal = workout.get("calories_burned_session").getAsDouble();
            int minutes = workout.get("duration_min").getAsInt();
            long workoutLogId = workout.get("id").getAsLong();

            tvItemWorkoutName.setText(workoutName);
            tvItemWorkoutInfo.setText(String.format(Locale.KOREA, "%.0f kcal, %d분", kcal, minutes));

            btnDeleteWorkout.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(workoutLogId, getAdapterPosition());
                }
            });
        }
    }

    public interface OnWorkoutDeleteListener {
        void onDeleteClick(long workoutLogId, int position);
    }
}