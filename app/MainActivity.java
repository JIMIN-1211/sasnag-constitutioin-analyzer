package com.example.app; 

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 앱 진입 시 환영 팝업 띄우기
        showWelcomeDialog();
    }

    private void showWelcomeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_welcome);
        dialog.setCancelable(false); // 바깥 클릭으로 닫히지 않게 (원하면 true)

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("환영합니다!");

        Button btnStart = dialog.findViewById(R.id.btnStartQuestion);
        Button btnHome = dialog.findViewById(R.id.btnGoHome);

        // 초록 버튼: 체질 진단 페이지로 이동
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BodyInfoActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });


        // 회색 버튼: 홈으로 이동하기 (팝업만 닫기)
        btnHome.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
