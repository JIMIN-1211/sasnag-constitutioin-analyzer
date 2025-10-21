package com.example.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // (테스트용) 혹시 남아있는 토큰도 제거해 두고
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().remove("jwt").apply();

        startActivity(new Intent(this, IdLoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

}