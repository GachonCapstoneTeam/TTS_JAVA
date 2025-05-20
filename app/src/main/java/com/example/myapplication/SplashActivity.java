package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.PreferenceUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        PreferenceUtil.init(getApplicationContext());

        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean autoLoginEnabled = PreferenceUtil.getBoolean(PreferenceUtil.KEY_AUTO_LOGIN, false);

            Intent intent;
            if (user != null && autoLoginEnabled) {
                // 자동 로그인 조건 충족 -> basic으로 이동
                intent = new Intent(SplashActivity.this, basic_layout.class);
            } else {
                // 로그인 필요 -> 로그인 화면으로
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

}
