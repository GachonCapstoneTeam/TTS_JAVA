package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private EditText emailInput, passwordInput, checkPasswordInput, nicknameInput;
    private Button submitButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // UI 요소 초기화
        emailInput = findViewById(R.id.su_id);
        passwordInput = findViewById(R.id.su_password);
        checkPasswordInput = findViewById(R.id.su_check);
        nicknameInput = findViewById(R.id.su_nickname);
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptSignUp() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String passwordCheck = checkPasswordInput.getText().toString().trim();
        String nickname = nicknameInput.getText().toString().trim();

        // 입력값 검증
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters.");
            return;
        }
        if (TextUtils.isEmpty(passwordCheck)) {
            checkPasswordInput.setError("Confirm password is required.");
            return;
        }
        if (!password.equals(passwordCheck)) {
            checkPasswordInput.setError("Passwords do not match.");
            return;
        }
        if (TextUtils.isEmpty(nickname)) {
            nicknameInput.setError("Nickname is required.");
            return;
        }

        // Firebase 인증으로 사용자 생성
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 사용자 프로필에 닉네임 설정
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nickname)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated with nickname.");
                                        }
                                    });

                            // 데이터베이스에 사용자 정보 저장
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            saveUserToDatabase(user.getUid(), email, nickname);
                            startActivity(intent);

                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email, String nickname) {
        User userData = new User(email, nickname);

        mDatabase.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                    // 선택적으로 이메일 인증 발송
                    // mAuth.getCurrentUser().sendEmailVerification();

                    // 로그인 화면으로 돌아가기
                    mAuth.signOut();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "saveUserToDatabase:failure", e);
                    Toast.makeText(SignUpActivity.this, "Sign up successful, but profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    finish();
                });
    }

    // Firebase 데이터베이스용 사용자 데이터 클래스
    public static class User {
        public String email;
        public String nickname;

        // Firebase에서 필요한 기본 생성자
        public User() {
        }

        public User(String email, String nickname) {
            this.email = email;
            this.nickname = nickname;
        }
    }
}