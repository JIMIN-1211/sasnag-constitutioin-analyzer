package com.example.app2.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvToday, tvHeadline, tvSubHeadline;
    private TextView btnPrevDay, btnNextDay, btnOpenRecordSimple;
    private TextView tabHome, tabHealth, tabMy;

    private final Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   // (내가 만든 메인 XML 그대로 사용)

        // ===== 기존 메인 기능들 바인딩/설정 =====
        tvToday = findViewById(R.id.tvToday);
        tvHeadline = findViewById(R.id.tvHeadline);
        tvSubHeadline = findViewById(R.id.tvSubHeadline);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        btnOpenRecordSimple = findViewById(R.id.btnOpenRecordSimple);
        btnOpenRecordSimple.setOnClickListener(v -> {
            Intent i = new Intent(this, com.example.app2.ui.RecordSimpleActivity.class);
            startActivity(i);
        });
        tabHome = findViewById(R.id.tabHome);
        tabHealth = findViewById(R.id.tabHealth);
        tabMy = findViewById(R.id.tabMy);

        // 샘플 데이터 (필요 시 Intent로 교체)
        String name = "홍길동";
        String constitution = "태음인";
        if (tvHeadline != null) tvHeadline.setText(constitution + " " + name + "님,");
        if (tvSubHeadline != null) tvSubHeadline.setText("오늘의 컨디션은 어떠신가요?");
        renderDate();

        if (btnPrevDay != null) btnPrevDay.setOnClickListener(v -> { cal.add(Calendar.DATE, -1); renderDate(); });
        if (btnNextDay != null) btnNextDay.setOnClickListener(v -> { cal.add(Calendar.DATE, 1); renderDate(); });

        if (tabHome != null)   tabHome.setOnClickListener(v -> Toast.makeText(this, "홈", Toast.LENGTH_SHORT).show());
        if (tabHealth != null) tabHealth.setOnClickListener(v -> Toast.makeText(this, "건강기록", Toast.LENGTH_SHORT).show());
        if (tabMy != null)     tabMy.setOnClickListener(v -> Toast.makeText(this, "마이페이지", Toast.LENGTH_SHORT).show());

        // ===== 기존에 있던 환영 팝업 유지 =====
        showWelcomeDialog();   // 앱 진입 시 팝업 표시
    }

    private void renderDate() {
        // 예: "오늘 (9월 5일 금요일)"
        if (tvToday == null) return;
        String dayName = new SimpleDateFormat("E", Locale.KOREAN).format(cal.getTime()); // 요일
        String monthDay = new SimpleDateFormat("M월 d일", Locale.KOREAN).format(cal.getTime());
        tvToday.setText("오늘 (" + monthDay + " " + dayName + ")");
    }

    /** 기존 팝업 로직 그대로 유지 */
    private void showWelcomeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_welcome);
        dialog.setCancelable(false); // 바깥 클릭으로 닫히지 않게

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("환영합니다!");

        Button btnStart = dialog.findViewById(R.id.btnStartQuestion); // 초록 버튼
        Button btnHome  = dialog.findViewById(R.id.btnGoHome);        // 회색 버튼

        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BodyInfoActivity.class);
                startActivity(intent);
                dialog.dismiss();
            });
        }
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> dialog.dismiss()); // 팝업만 닫고 홈 유지
        }

        dialog.show();
    }
}
