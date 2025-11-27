package com.example.app2.ui;

import android.content.Intent;
import android.content.SharedPreferences; // [추가]
import android.os.Bundle;
import android.util.Log; // [추가]
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageActivity extends AppCompatActivity {

    // 1. 뷰 변수 선언
    private Button btnLogout;
    private TextView tvUserName, tvUserEmail;

    // 메뉴 아이템들
    private TextView btnDiagnosisHistory, btnRediagnosis, btnChangePassword,
            btnSocialLink, btnNotice, btnInquiry, btnTerms, btnWithdraw;

    // 하단 탭
    private TextView btnHome, btnOpenRecord, btnOpenMyPage;

    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        ApiClient.init(getApplicationContext());
        apiService = ApiClient.getApiService(this);

        bindViews();
        setupClicks();
    }

    /**
     * [신규] onResume() 추가
     * 이 화면이 다시 보일 때마다 사용자 정보를 새로고침합니다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // 4. 사용자 정보 로드
    }

    /**
     * XML의 뷰들을 자바 변수와 연결합니다. (수정 없음)
     */
    private void bindViews() {
        btnLogout = findViewById(R.id.btnLogout);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        // 메뉴
        btnDiagnosisHistory = findViewById(R.id.btnDiagnosisHistory);
        btnRediagnosis = findViewById(R.id.btnRediagnosis);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSocialLink = findViewById(R.id.btnSocialLink);
        btnNotice = findViewById(R.id.btnNotice);
        btnInquiry = findViewById(R.id.btnInquiry);
        btnTerms = findViewById(R.id.btnTerms);
        btnWithdraw = findViewById(R.id.btnWithdraw);

        // 하단 탭
        btnHome = findViewById(R.id.btnHome);
        btnOpenRecord = findViewById(R.id.btnOpenRecord);
        btnOpenMyPage = findViewById(R.id.btnOpenMyPage);
    }

    /**
     * 💡 [수정] 클릭 리스너 (로그아웃, 하단 탭) 수정
     */
    private void setupClicks() {
        // 뒤로가기

        // 💡 [수정] 로그아웃 로직
        btnLogout.setOnClickListener(v ->{

            // 1. SharedPreferences에서 토큰 삭제 (MainActivity의 PREFS_NAME 참조)
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // 💡 (토큰 키 이름은 "AUTH_TOKEN"이라고 가정합니다. ApiClient가 사용하는 실제 키로 변경해야 합니다.)
            editor.remove("AUTH_TOKEN");

            // --- [💡 팁 적용] ---
            // 로그아웃 시, 웰컴 팝업 "봤음" 기록도 함께 삭제합니다.
            // (MainActivity의 상수를 직접 참조)
            editor.remove(MainActivity.KEY_HAS_SEEN_WELCOME);
            // --- [팁 적용 완료] ---

            // 2. [중요] 모든 변경 사항을 한 번에 저장합니다.
            editor.apply();

            // 3. ApiClient의 내부 토큰도 리셋 (중요)
            // 💡 (ApiClient에 토큰을 리셋하는 static 메서드가 필요할 수 있습니다. 예: ApiClient.resetToken();)
            //    만약 ApiClient.getApiService(this)가 매번 SharedPreferences를 읽는다면 이 줄은 필요 없습니다.

            // 4. 모든 액티비티 스택을 지우고 로그인 화면으로 이동
            Intent intent = new Intent(MyPageActivity.this, IdLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 5. 현재 MyPageActivity 종료
        });

        // 메뉴 아이템 (임시 토스트)
        btnDiagnosisHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, ResultActivity.class);
            startActivity(intent);
        });
        btnRediagnosis.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, BodyInfoActivity.class);
            startActivity(intent);
        });
        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(this, "비밀번호 변경 (미구현)", Toast.LENGTH_SHORT).show());
        btnSocialLink.setOnClickListener(v ->
                Toast.makeText(this, "소셜 계정 연동 (미구현)", Toast.LENGTH_SHORT).show());
        btnNotice.setOnClickListener(v ->
                Toast.makeText(this, "공지사항 (미구현)", Toast.LENGTH_SHORT).show());
        btnInquiry.setOnClickListener(v ->
                Toast.makeText(this, "문의하기 (미구현)", Toast.LENGTH_SHORT).show());
        btnTerms.setOnClickListener(v ->
                Toast.makeText(this, "서비스 약관 (미구현)", Toast.LENGTH_SHORT).show());
        btnWithdraw.setOnClickListener(v ->
                Toast.makeText(this, "계정 탈퇴 (미구현)", Toast.LENGTH_SHORT).show());

        // 하단 탭
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnOpenRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, HealthRecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnOpenMyPage.setOnClickListener(v ->
                Toast.makeText(this, "현재 화면입니다.", Toast.LENGTH_SHORT).show());
    }

    /**
     * GET /v1/me API를 호출하여 사용자 정보를 불러옵니다. (수정 없음)
     */
    private void loadUserData() {
        apiService.getMyInfo().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject user = response.body();
                    String name = "사용자";
                    String email = "이메일 정보 없음";

                    if (user.has("name") && !user.get("name").isJsonNull()) {
                        name = user.get("name").getAsString();
                    }
                    if (user.has("email") && !user.get("email").isJsonNull()) {
                        email = user.get("email").getAsString();
                    }

                    tvUserName.setText(name + " 님");
                    tvUserEmail.setText(email);

                } else {
                    Log.e("MyPageActivity", "loadUserData failed: " + response.code());
                    Toast.makeText(MyPageActivity.this, "사용자 정보 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MyPageActivity", "loadUserData network error: " + t.getMessage());
                Toast.makeText(MyPageActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}