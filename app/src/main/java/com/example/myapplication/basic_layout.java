package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.example.myapplication.util.PreferenceUtil;

public class basic_layout extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button logoutButton;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);
        logoutButton = findViewById(R.id.logout);

        viewPager = findViewById(R.id.frame);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        BottomNavigationView navigationMenu = findViewById(R.id.navigation_menu);

        viewPager.setCurrentItem(1);
        navigationMenu.setSelectedItemId(R.id.home);

        navigationMenu.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.search) {
                viewPager.setCurrentItem(0); // SearchFragment
            } else if (item.getItemId() == R.id.home) {
                viewPager.setCurrentItem(1); // HomeFragment
            } else if (item.getItemId() == R.id.rcm) {
                viewPager.setCurrentItem(2); // MainFragment
            }
            return true;
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase 콘솔에 등록된 ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        logoutButton.setOnClickListener(v -> {
            // Firebase 로그아웃
            FirebaseAuth.getInstance().signOut();

            // Google 로그아웃
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // SharedPreferences 초기화
                PreferenceUtil.clear();

                Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();

                // LoginActivity로 이동
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 백스택 제거
                startActivity(intent);
                finish();
            });
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    navigationMenu.setSelectedItemId(R.id.search);
                } else if (position == 1) {
                    navigationMenu.setSelectedItemId(R.id.home);
                } else if (position == 2) {
                    navigationMenu.setSelectedItemId(R.id.rcm);
                }
            }
        });
    }


}
