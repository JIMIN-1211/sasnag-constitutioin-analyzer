package com.example.app2.ui;

import android.content.Intent;
import android.content.SharedPreferences; // SharedPreferences 임포트는 유지해도 괜찮습니다.
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Log 임포트 확인
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.example.app2.api.ApiClient;
import com.example.app2.api.ApiService;
import com.example.app2.auth.TokenManager; // TokenManager 임포트 확인
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IdLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnKakaoLogin, btnGoogleLogin;
    private TextView tvMsg, tvSignUp;
    private ProgressBar progress;
    // SharedPreferences 변수는 TokenManager를 사용하므로 제거해도 되지만, 남겨둬도 문제는 없습니다.
    // private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_login);

        // ApiClient 초기화 시 TokenManager.init()이 내부에서 호출됩니다.
        ApiClient.init(getApplicationContext());

        // NullPointerException 방지 로직 포함된 바인딩
        if (!bindViewsAndValidate()) {
            return;
        }

        wireUp();
    }

    /** 뷰를 바인딩하고, 필수 뷰가 null이면 false 반환 */
    private boolean bindViewsAndValidate() {
        etUsername     = findViewById(R.id.etUsername);
        etPassword     = findViewById(R.id.etPassword);
        btnLogin       = findViewById(R.id.btnLogin);
        btnKakaoLogin  = findViewById(R.id.btnKakaoLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvMsg          = findViewById(R.id.tvMsg);
        tvSignUp       = findViewById(R.id.tvSignUp);
        progress       = findViewById(R.id.progress);

        if (tvMsg == null) {
            Log.e("IdLoginActivity", "CRITICAL: tvMsg ID not found in layout!");
            Toast.makeText(this, "화면 구성 오류 (tvMsg 없음)", Toast.LENGTH_LONG).show();
            return false;
        }
        if (etUsername == null) {
            showMessage("오류: XML 레이아웃에 'etUsername' ID가 없습니다.");
            return false;
        }
        if (etPassword == null) {
            showMessage("오류: XML 레이아웃에 'etPassword' ID가 없습니다.");
            return false;
        }
        if (btnLogin == null) {
            showMessage("오류: XML 레이아웃에 'btnLogin' ID가 없습니다.");
            return false;
        }
        if (progress == null) {
            showMessage("오류: XML 레이아웃에 'progress' ID가 없습니다.");
            return false;
        }
        return true;
    }

    /** 클릭 리스너 설정 */
    private void wireUp() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> doLogin());
        }
        if (btnKakaoLogin != null) {
            btnKakaoLogin.setOnClickListener(v -> showMessage("카카오 로그인은 준비 중입니다."));
        }
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> showMessage("구글 로그인은 준비 중입니다."));
        }
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                showMessage("");
                Intent intent = new Intent(IdLoginActivity.this, EmailActivity.class);
                startActivity(intent);
            });
        }
    }

    /** 로그인 시도 */
    private void doLogin() {
        showMessage("");
        String u = safeText(etUsername);
        String p = safeText(etPassword);

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            showMessage("아이디와 비밀번호를 입력해 주세요.");
            return;
        }

        setLoading(true);

        JsonObject body = new JsonObject();
        body.addProperty("username", u);
        body.addProperty("password", p);

        ApiService api = ApiClient.getApiService();

        // --- 1단계: 로그인 API 호출 ---
        api.login(body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> res) {
                // setLoading(false); // 아직 로딩 중

                if (!res.isSuccessful() || res.body() == null) {
                    setLoading(false);
                    // [수정] 오류 코드를 포함하여 더 자세한 메시지 표시
                    String errorMsg = "로그인 실패 (" + res.code() + ")";
                    if(res.code() == 401) {
                        errorMsg += ": 아이디 또는 비밀번호가 일치하지 않습니다.";
                    } else if (res.code() == 400) {
                        errorMsg += ": 잘못된 요청 형식입니다.";
                    }
                    showMessage(errorMsg);
                    return;
                }

                JsonObject root = res.body();
                String token = root.has("token") && !root.get("token").isJsonNull()
                        ? root.get("token").getAsString()
                        : null;

                if (TextUtils.isEmpty(token)) {
                    setLoading(false);
                    showMessage("서버 응답에 토큰이 없습니다.");
                    return;
                }

                // [수정] TokenManager를 통해 토큰 저장
                TokenManager.saveToken(getApplicationContext(), token);

                showMessage("로그인 성공. 사용자 정보를 가져오는 중...");
                fetchUserIdAndProceed(api); // 2단계 호출
            }

            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                setLoading(false);
                showMessage("네트워크 오류: " + t.getMessage());
            }
        });
    }

    /** 2단계: '내 정보' API 호출하여 user_id 가져오기 */
    private void fetchUserIdAndProceed(ApiService api) {
        api.getMe().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> res) {
                setLoading(false); // 모든 로딩 종료

                if (!res.isSuccessful() || res.body() == null) {
                    // [수정] 오류 코드 포함 메시지, TokenManager로 롤백
                    showMessage("로그인은 성공했으나, 사용자 정보 로드 실패 (" + res.code() + ")");
                    TokenManager.clear(getApplicationContext());
                    return;
                }

                JsonObject userObj = res.body();
                Integer userId = 0;
                if (userObj.has("id") && !userObj.get("id").isJsonNull()) {
                    userId = userObj.get("id").getAsInt();
                }

                if (userId <= 0) {
                    // [수정] TokenManager로 롤백
                    showMessage("사용자 정보에 ID가 없습니다.");
                    TokenManager.clear(getApplicationContext());
                    return;
                }

                // [수정] TokenManager를 통해 user_id 저장
                TokenManager.saveUserId(getApplicationContext(), userId);

                // MainActivity로 이동
                Intent intent = new Intent(IdLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 로그인 화면 종료
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setLoading(false);
                // [수정] TokenManager로 롤백
                showMessage("사용자 정보 로드 실패 (네트워크): " + t.getMessage());
                TokenManager.clear(getApplicationContext());
            }
        });
    }

    /** 로딩 상태 UI 업데이트 */
    private void setLoading(boolean on) {
        if (progress != null) progress.setVisibility(on ? View.VISIBLE : View.GONE);
        if (btnLogin != null) {
            btnLogin.setEnabled(!on);
            btnLogin.setAlpha(on ? 0.5f : 1f);
        }
    }

    /** EditText에서 안전하게 텍스트 가져오기 */
    private static String safeText(EditText et) {
        // bindViewsAndValidate에서 null 아님 보장됨
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** 메시지 표시 (tvMsg 또는 Toast) */
    private void showMessage(String s) {
        if (tvMsg != null) {
            tvMsg.setText(s);
        } else {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            Log.w("IdLoginActivity", "showMessage called but tvMsg is null. Message: " + s);
        }
    }
}