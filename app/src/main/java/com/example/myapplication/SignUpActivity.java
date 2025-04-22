package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText idEditText;
    private EditText passwordEditText;
    private EditText passwordCheckEditText;
    private EditText nicknameEditText;
    private Button signUpSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup); // 너가 제공한 회원가입 XML과 연결

        idEditText = findViewById(R.id.su_id);
        passwordEditText = findViewById(R.id.su_password);
        passwordCheckEditText = findViewById(R.id.su_check);
        nicknameEditText = findViewById(R.id.su_nickname);
        signUpSubmitButton = findViewById(R.id.submit_button);

        signUpSubmitButton.setOnClickListener(v -> {
            String userId = idEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String passwordCheck = passwordCheckEditText.getText().toString().trim();
            String nickname = nicknameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(passwordCheck) || TextUtils.isEmpty(nickname)) {
                Toast.makeText(SignUpActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordCheck)) {
                Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId.length() < 6 || password.length() < 8 || nickname.length() > 8) {
                Toast.makeText(SignUpActivity.this, "입력 조건을 확인해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버 통신, 다음 로직 처리
            submitSignUp(userId, password, nickname);
        });
    }

    private void submitSignUp(String userId, String password, String nickname) {
        //  서버로 POST 요청 전송

        /* 가입 성공 시 LoginActivity로 돌아가기
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        */
    }
}
