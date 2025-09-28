package com.example.app2.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app2.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.Map;

public class AgreementActivity extends AppCompatActivity {

    private CheckBox cbAll, cbAge, cbService, cbPrivacy, cbSensitive, cbMarketing;
    private TextView arrowAge, arrowService, arrowPrivacy, arrowSensitive, arrowMarketing;
    private Button btnContinue;

    // 약관 텍스트 저장용
    private Map<String, String> termsContent = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        // 뷰 바인딩
        cbAll = findViewById(R.id.cbAll);
        cbAge = findViewById(R.id.cbAge);
        cbService = findViewById(R.id.cbService);
        cbPrivacy = findViewById(R.id.cbPrivacy);
        cbSensitive = findViewById(R.id.cbSensitive);
        cbMarketing = findViewById(R.id.cbMarketing);

        arrowAge = findViewById(R.id.arrowAge);
        arrowService = findViewById(R.id.arrowService);
        arrowPrivacy = findViewById(R.id.arrowPrivacy);
        arrowSensitive = findViewById(R.id.arrowSensitive);
        arrowMarketing = findViewById(R.id.arrowMarketing);

        btnContinue = findViewById(R.id.btnContinue);

        // 초기 텍스트 기본값
        termsContent.put("age", "만 14세 이상임을 확인합니다.");
        termsContent.put("service", "서비스 이용약관 기본내용");
        termsContent.put("privacy", "개인정보 수집 및 이용에 대한 안내");
        termsContent.put("sensitive", "민감정보 수집 안내(선택)");
        termsContent.put("marketing", "마케팅 정보 수신 동의(선택)");

        // 전체동의 토글
        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cbAge.setChecked(isChecked);
            cbService.setChecked(isChecked);
            cbPrivacy.setChecked(isChecked);
            cbSensitive.setChecked(isChecked);
            cbMarketing.setChecked(isChecked);
            updateButtonState();
        });

        // 필수 체크박스 변화 감지
        cbAge.setOnCheckedChangeListener((b, s) -> updateButtonState());
        cbService.setOnCheckedChangeListener((b, s) -> updateButtonState());
        cbPrivacy.setOnCheckedChangeListener((b, s) -> updateButtonState());

        cbSensitive.setOnCheckedChangeListener((b, s) -> {/* 필요시 처리 */});
        cbMarketing.setOnCheckedChangeListener((b, s) -> {/* 필요시 처리 */});

        // 화살표 클릭 → 바텀시트 열기 (각 키마다)
        arrowAge.setOnClickListener(v -> showBottomSheet("age"));
        arrowService.setOnClickListener(v -> showBottomSheet("service"));
        arrowPrivacy.setOnClickListener(v -> showBottomSheet("privacy"));
        arrowSensitive.setOnClickListener(v -> showBottomSheet("sensitive"));
        arrowMarketing.setOnClickListener(v -> showBottomSheet("marketing"));

        //  필수 동의가 되어야 활성화
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AgreementActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        updateButtonState();
        applyCheckboxColors();
    }

    private void updateButtonState() {
        boolean enabled = cbAge.isChecked() && cbService.isChecked() && cbPrivacy.isChecked();
        btnContinue.setEnabled(enabled);
        int colorId = enabled ? android.R.color.holo_green_dark : android.R.color.darker_gray;
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorId)));
    }

    // 바텀시트 열기
    private void showBottomSheet(String key) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_terms, null);
        EditText et = sheetView.findViewById(R.id.bs_edit);
        TextView title = sheetView.findViewById(R.id.bs_title);
        Button btnSave = sheetView.findViewById(R.id.bs_btn_save);

        // 제목, 기존 텍스트 불러오기
        title.setText(getTitleForKey(key));
        et.setText(termsContent.getOrDefault(key, ""));

        btnSave.setOnClickListener(v -> {
            String newText = et.getText().toString();
            termsContent.put(key, newText); // 메모리에 저장
            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private String getTitleForKey(String key) {
        switch (key) {
            case "age": return "만 14세 이상 동의 내용";
            case "service": return "서비스 이용약관";
            case "privacy": return "개인정보 수집 및 이용";
            case "sensitive": return "민감정보 수집 안내";
            case "marketing": return "마케팅 정보 수신 안내";
            default: return "약관 상세";
        }
    }

    // 체크박스 체크색상을 기본으로 적용
    private void applyCheckboxColors() {
        // 기본 체크박스 색상 유지
    }
}