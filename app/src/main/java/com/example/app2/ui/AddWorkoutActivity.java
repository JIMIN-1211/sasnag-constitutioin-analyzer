package com.example.app2.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddWorkoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnWorkoutWalk, btnWorkoutRun, btnWorkoutHike, btnWorkoutCycle;
    private Button btnWorkoutChest, btnWorkoutBack, btnWorkoutLegs, btnWorkoutShoulder;

    private ApiService apiService;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

    // (중요) 이 화면을 연 WorkoutRecordActivity에서 '선택된 날짜'를 받아와야 함
    private String targetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        ApiClient.init(getApplicationContext());
        apiService = ApiClient.getApiService(this);

        // TODO: (중요) 이전 화면(WorkoutRecord)에서 날짜를 넘겨받아야 함
        targetDate = getIntent().getStringExtra("TARGET_DATE");
        if (targetDate == null) {
            targetDate = apiDateFormat.format(Calendar.getInstance().getTime());
        }
        // 뷰 바인딩
        btnBack = findViewById(R.id.btnBack);
        btnWorkoutWalk = findViewById(R.id.btnWorkoutWalk);
        btnWorkoutRun = findViewById(R.id.btnWorkoutRun);
        btnWorkoutHike = findViewById(R.id.btnWorkoutHike);
        btnWorkoutCycle = findViewById(R.id.btnWorkoutCycle);
        btnWorkoutChest = findViewById(R.id.btnWorkoutChest);
        btnWorkoutBack = findViewById(R.id.btnWorkoutBack);
        btnWorkoutLegs = findViewById(R.id.btnWorkoutLegs);
        btnWorkoutShoulder = findViewById(R.id.btnWorkoutShoulder);

        btnBack.setOnClickListener(v -> finish());

        // 클릭 리스너 설정 (DB의 master_exercise_types ID와 일치해야 함)
        // 1: 걷기, 2: 달리기, 3: 등산, 4: 자전거, 5: 웨이트 트레이닝
        btnWorkoutWalk.setOnClickListener(v -> showDurationInputDialog(1, "걷기"));
        btnWorkoutRun.setOnClickListener(v -> showDurationInputDialog(2, "달리기"));
        btnWorkoutHike.setOnClickListener(v -> showDurationInputDialog(3, "등산"));
        btnWorkoutCycle.setOnClickListener(v -> showDurationInputDialog(4, "자전거"));

        // (참고) 헬스 "가슴/등/하체/어깨"는 모두 "웨이트 트레이닝 (ID: 5)"으로 보냅니다.
        btnWorkoutChest.setOnClickListener(v -> showDurationInputDialog(5, "가슴 (웨이트)"));
        btnWorkoutBack.setOnClickListener(v -> showDurationInputDialog(5, "등 (웨이트)"));
        btnWorkoutLegs.setOnClickListener(v -> showDurationInputDialog(5, "하체 (웨이트)"));
        btnWorkoutShoulder.setOnClickListener(v -> showDurationInputDialog(5, "어깨 (웨이트)"));
    }

    /**
     * [신규] 운동 시간(분)을 입력받는 간단한 'AlertDialog' 팝업을 띄웁니다.
     * @param exerciseId (예: 1)
     * @param exerciseName (예: "걷기")
     */
    private void showDurationInputDialog(int exerciseId, String exerciseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(exerciseName + " 시간 입력");

        // 팝업에 EditText 추가
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("운동 시간 (분)");
        input.setText("30"); // 기본값 30분
        input.setPadding(60, 40, 60, 40); // 여백
        builder.setView(input);

        // "저장" 버튼
        builder.setPositiveButton("저장", (dialog, which) -> {
            String minutesStr = input.getText().toString();
            if (minutesStr.isEmpty()) {
                Toast.makeText(this, "운동 시간(분)을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            int durationMinutes = Integer.parseInt(minutesStr);

            // API 전송
            postExerciseApi(exerciseId, durationMinutes);
        });

        // "취소" 버튼
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * [신규] POST /v1/exercise API를 호출하는 메서드
     */
    private void postExerciseApi(int exerciseId, int durationMinutes) {
        // 2. 서버로 보낼 JsonObject 생성
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("targetDate", targetDate);
        requestBody.addProperty("exercise_id", exerciseId);
        requestBody.addProperty("duration_minutes", durationMinutes);

        Log.d("AddWorkoutActivity", "POST /v1/exercise: " + requestBody.toString());

        // 3. API 호출
        // (참고: ApiService.java에 postExercise가 선언되어 있어야 합니다)
        apiService.postExercise(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddWorkoutActivity.this, "운동이 기록되었습니다.", Toast.LENGTH_SHORT).show();

                    // [중요] 저장이 완료되면 '운동 추가' 화면을 닫고
                    // '운동 기록' 화면(WorkoutRecordActivity)으로 돌아갑니다.
                    finish();
                } else {
                    Log.e("AddWorkoutActivity", "postExercise failed: " + response.code());
                    Toast.makeText(AddWorkoutActivity.this, "저장 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("AddWorkoutActivity", "postExercise network error: " + t.getMessage());
            }
        });
    }
}