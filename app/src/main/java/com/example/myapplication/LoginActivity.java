package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private SignInButton googleSignInButton; // 구글 로그인을 위한 버튼
    private TextView signUpText;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;

    private static final int RC_SIGN_IN = 9001; // 구글 로그인 요청 코드
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // 레이아웃 파일명이 login.xml인지 확인하세요

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // login.xml 파일의 EditText ID가 다음과 같다고 가정합니다:
        // Email (이메일 입력용)
        // password (비밀번호 입력용)
        // loginButton (로그인 버튼)
        // signUp (회원가입 텍스트뷰)
        // 그리고 구글 로그인 버튼(예: google_sign_in_button)을 추가하세요
        emailInput = findViewById(R.id.Email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUp);
        googleSignInButton = findViewById(R.id.google_sign_in_button); // 이 ID를 레이아웃에 추가하세요

        // 구글 로그인 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // 중요: Firebase와 연동 시 필요
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton.setOnClickListener(v -> loginUserWithEmail());
        //googleSignInButton.setOnClickListener(v -> signInWithGoogle()); // 구글 로그인 버튼 클릭 리스너 (주석 처리됨)

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 사용자가 로그인되어 있는지 (null이 아닌지) 확인하고 UI를 업데이트합니다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 사용자가 이미 로그인되어 있으므로, 다음 화면(TestActivity)으로 이동합니다.
            updateUI(currentUser);
        }
    }

    private void loginUserWithEmail() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("이메일은 필수입니다.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("비밀번호는 필수입니다.");
            return;
        }

        // 진행률 대화상자 표시 (실제 코드는 없음, 주석만 존재)

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // 진행률 대화상자 숨김 (실제 코드는 없음, 주석만 존재)
                    if (task.isSuccessful()) {
                        Log.d(TAG, "이메일 로그인 성공");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "이메일 로그인 실패", task.getException());
                        Toast.makeText(LoginActivity.this, "인증 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 구글 로그인이 성공했으며, Firebase로 인증합니다.
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "구글 계정으로 Firebase 인증 시도:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // 구글 로그인이 실패했으며, UI를 적절하게 업데이트합니다.
                Log.w(TAG, "구글 로그인 실패", e);
                Toast.makeText(this, "구글 로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        // 진행률 대화상자 표시 (실제 코드는 없음, 주석만 존재)
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // 진행률 대화상자 숨김 (실제 코드는 없음, 주석만 존재)
                    if (task.isSuccessful()) {
                        Log.d(TAG, "구글 계정으로 Firebase 인증 성공");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // 구글 로그인을 통해 새로 가입한 사용자인 경우, 사용자 정보를
                        // 실시간 데이터베이스의 'users' 노드에도 저장할 수 있습니다.
                        if (task.getResult().getAdditionalUserInfo().isNewUser() && user != null) {
                            saveGoogleUserToDatabase(user);
                        } else {
                            updateUI(user);
                        }
                    } else {
                        Log.w(TAG, "구글 계정으로 Firebase 인증 실패", task.getException());
                        Toast.makeText(LoginActivity.this, "구글 인증 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void saveGoogleUserToDatabase(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String displayName = firebaseUser.getDisplayName(); // 구글은 표시 이름을 제공합니다.

        // 구글에서 제공하는 표시 이름을 닉네임으로 사용하거나, null인 경우 기본값을 사용합니다.
        String nickname = (displayName != null && !displayName.isEmpty()) ? displayName : "GoogleUser";

        SignUpActivity.User userObject = new SignUpActivity.User(email, nickname);

        mDatabase.child("users").child(userId).setValue(userObject)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "구글 사용자 정보가 데이터베이스에 저장되었습니다.");
                    updateUI(firebaseUser); // 저장 후 UI 업데이트 진행
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "구글 사용자 정보를 데이터베이스에 저장하는 데 실패했습니다.", e);
                    // 계속 진행하되 오류를 기록합니다.
                    updateUI(firebaseUser);
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "로그인됨: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // 사용자 정보를 다음 화면(TestActivity)으로 전달할 수 있습니다.
            // intent.putExtra("USER_EMAIL", user.getEmail()); // 사용자 이메일 전달 예시
            // intent.putExtra("USER_UID", user.getUid()); // 사용자 UID 전달 예시
            startActivity(intent);
            finish(); // LoginActivity를 종료하여 사용자가 뒤로 가기 할 수 없도록 합니다.
        } else {
            // 로그아웃되었거나 로그인이 실패했습니다.
            // UI는 이를 반영해야 합니다 (예: 입력 필드 초기화, 오류 메시지 표시).
        }
    }
}