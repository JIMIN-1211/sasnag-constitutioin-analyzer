package com.example.app2.ui;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
// 선택: 토큰 저장 유틸이 있다면 사용
import com.example.app2.auth.TokenManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailActivity extends AppCompatActivity {

    private EditText etEmail, etCode;
    private Button btnRequestCode, btnSubmit;
    private TextView tvTimer, tvStatus;

    private String usernameFromInfo, passwordFromInfo, nameFromInfo, genderFromInfo;
    private Integer birthYearFromInfo = null;

    private CountDownTimer timer;
    private static final int RESEND_SECONDS = 180; // 재전송 쿨다운(초)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        bindViews();
        readExtrasFromInfo();
        wireEvents();
        validateButtons();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        etCode = findViewById(R.id.etCode);
        btnRequestCode = findViewById(R.id.btnRequestCode);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void readExtrasFromInfo() {
        Intent i = getIntent();
        usernameFromInfo = i.getStringExtra("username");
        passwordFromInfo = i.getStringExtra("password");
        nameFromInfo     = i.getStringExtra("name");       // Info에서 받지 않으면 null일 수 있음
        genderFromInfo   = i.getStringExtra("gender");     // "male" / "female"
        if (i.hasExtra("birth_year")) {
            try { birthYearFromInfo = Integer.valueOf(i.getIntExtra("birth_year", 0)); }
            catch (Exception ignored) {}
        }
    }

    private void wireEvents() {

        btnRequestCode.setOnClickListener(v -> sendRegisterCreds());
        btnSubmit.setOnClickListener(v -> submitRegister());

        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validateButtons(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etEmail.addTextChangedListener(w);
        etCode.addTextChangedListener(w);
    }

    private void validateButtons() {
        boolean emailOk = etEmail.getText() != null &&
                Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches();
        boolean codeOk = etCode.getText() != null && etCode.getText().toString().trim().length() > 0;

        btnRequestCode.setEnabled(emailOk && (timer == null));               // 타이머 중엔 비활성화
        btnSubmit.setEnabled(emailOk && codeOk);
        btnRequestCode.setAlpha(btnRequestCode.isEnabled() ? 1f : .5f);
        btnSubmit.setAlpha(btnSubmit.isEnabled() ? 1f : .5f);
    }

    /** 1) 인증코드 발송: POST /v1/auth/register-creds (username + password + email) */
    private void sendRegisterCreds() {
        String email = etEmail.getText().toString().trim();
        if (usernameFromInfo == null || passwordFromInfo == null) {
            showError("먼저 아이디/비밀번호 입력 화면을 완료해주세요.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("이메일 형식이 올바르지 않습니다.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("username", usernameFromInfo);
        body.addProperty("password", passwordFromInfo);
        body.addProperty("email", email);

        ApiService api = ApiClient.getApiService();
        btnRequestCode.setEnabled(false);
        api.sendRegisterCreds(body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> res) {
                if (res.isSuccessful()) {
                    showOk("인증 메일을 보냈습니다. 받은 코드를 입력하세요.");
                    startResendTimer(RESEND_SECONDS);
                } else if (res.code() == 409) {
                    showError("이미 존재하는 아이디 또는 이메일입니다.");
                    stopResendTimerAllow();
                } else if (res.code() == 400) {
                    showError("필수 값이 누락되었습니다.");
                    stopResendTimerAllow();
                } else {
                    showError("코드 발송 실패 (" + res.code() + ")");
                    stopResendTimerAllow();
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                showError("네트워크 오류: " + t.getMessage());
                stopResendTimerAllow();
            }
        });
    }

    /** 2) 최종 회원가입: POST /v1/auth/register (email + auth_code + name + birth_year + gender) */
    private void submitRegister() {
        String email = etEmail.getText().toString().trim();
        String code  = etCode.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("이메일 형식이 올바르지 않습니다."); return;
        }
        if (code.isEmpty()) {
            showError("인증 코드를 입력하세요."); return;
        }
        // name/birth_year/gender가 필수인 스펙이므로 누락 시 안내
        if (nameFromInfo == null || birthYearFromInfo == null || birthYearFromInfo == 0 || genderFromInfo == null) {
            showError("이름/출생년도/성별 정보가 필요합니다. Info 화면에서 값을 전달해주세요.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("auth_code", code);
        body.addProperty("name", nameFromInfo);
        body.addProperty("birth_year", birthYearFromInfo);
        body.addProperty("gender", genderFromInfo);

        ApiService api = ApiClient.getApiService();
        btnSubmit.setEnabled(false);
        api.register(body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> res) {
                if (res.isSuccessful()) {
                    showOk("회원 가입 성공! 자동 로그인 중...");
                    loginThenGoMain();
                } else if (res.code() == 401) {
                    showError("인증 코드가 만료되었거나 유효하지 않습니다.");
                    btnSubmit.setEnabled(true);
                } else if (res.code() == 400) {
                    showError("필수 값이 누락되었습니다.");
                    btnSubmit.setEnabled(true);
                } else {
                    showError("회원가입 실패 (" + res.code() + ")");
                    btnSubmit.setEnabled(true);
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                showError("네트워크 오류: " + t.getMessage());
                btnSubmit.setEnabled(true);
            }
        });
    }

    /** 3) 자동 로그인 → Main 이동 */
    private void loginThenGoMain() {
        JsonObject login = new JsonObject();
        login.addProperty("username", usernameFromInfo);
        login.addProperty("password", passwordFromInfo);

        ApiClient.getApiService().login(login).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> res) {
                if (res.isSuccessful() && res.body() != null) {
                    String token = res.body().get("token").getAsString();
                    try { TokenManager.saveToken(EmailActivity.this, token); } catch (Exception ignored) {}
                    // (선택) /users/me 호출해 userId 확보 필요하면 여기서 추가
                    startActivity(new Intent(EmailActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    showError("로그인 실패 (" + res.code() + ")");
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                showError("로그인 네트워크 오류: " + t.getMessage());
            }
        });
    }

    // ===== helpers =====
    private void startResendTimer(int seconds) {
        if (timer != null) timer.cancel();
        btnRequestCode.setEnabled(false);
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override public void onTick(long ms) {
                int s = (int) (ms / 1000);
                tvTimer.setText(String.format("재전송 가능: %02d:%02d", s / 60, s % 60));
                validateButtons();
            }
            @Override public void onFinish() {
                tvTimer.setText("");
                btnRequestCode.setEnabled(true);
                timer = null;
                validateButtons();
            }
        }.start();
    }

    private void stopResendTimerAllow() {
        if (timer != null) { timer.cancel(); timer = null; }
        btnRequestCode.setEnabled(true);
        tvTimer.setText("");
        validateButtons();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    private void showOk(String msg) {
        if (tvStatus != null) {
            tvStatus.setTextColor(0xFF0F9D58);
            tvStatus.setText(msg);
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String msg) {
        if (tvStatus != null) {
            tvStatus.setTextColor(0xFFFF4444);
            tvStatus.setText(msg);
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
