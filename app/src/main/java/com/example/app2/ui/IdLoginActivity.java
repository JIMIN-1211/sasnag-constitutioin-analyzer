package com.example.app2.ui;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import com.example.app2.R;
import com.example.app2.models.LoginRequest;
import com.example.app2.models.LoginResponse;
import android.graphics.Paint;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;

public class IdLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnKakao, btnGoogle;
    private ProgressBar progress;
    private TextView tvMsg, tvSignUp;
    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        progress   = findViewById(R.id.progress);
        tvMsg      = findViewById(R.id.tvMsg);
        btnKakao = findViewById(R.id.btnKakaoLogin);
        btnGoogle = findViewById(R.id.btnGoogleLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        api = ApiClient.getApiService();
        btnLogin.setOnClickListener(v -> tryLogin());
        tvSignUp.setPaintFlags(tvSignUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //카카오, 구글로그인 클릭 시
        btnKakao.setOnClickListener(v ->
                Toast.makeText(this, "카카오 로그인은 추후 연결 예정", Toast.LENGTH_SHORT).show());
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "구글 로그인은 추후 연결 예정", Toast.LENGTH_SHORT).show());
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, AgreementActivity.class)));


    }

    private void tryLogin() {
        String u = etUsername.getText().toString().trim();
        String p = etPassword.getText().toString();

        tvMsg.setText("");
        if (u.isEmpty()) { tvMsg.setText("아이디를 입력하세요"); return; }
        if (p.isEmpty()) { tvMsg.setText("비밀번호를 입력하세요"); return; }

        setLoading(true);

        api.login(new LoginRequest(u, p)).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                setLoading(false);
                if (res.isSuccessful() && res.body()!=null && res.body().token!=null) {
                    // 토큰 저장
                    SharedPreferences sp = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    sp.edit().putString("jwt", res.body().token).apply();

                    // MainActivity로 이동
                    Intent i = new Intent(IdLoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    tvMsg.setText("로그인 실패: " + res.code());
                }
            }
            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                tvMsg.setText("네트워크 오류: " + t.getMessage());
            }
        });
    }

    private void setLoading(boolean on) {
        progress.setVisibility(on ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!on);
    }
}
