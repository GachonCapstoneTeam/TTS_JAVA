package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class basic_layout extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_layout);

        viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        BottomNavigationView navigationMenu = findViewById(R.id.navigation_menu);

        viewPager.setCurrentItem(1);
        navigationMenu.setSelectedItemId(R.id.home);

        navigationMenu.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.recent) {
                viewPager.setCurrentItem(0); // SearchFragment
            } else if (item.getItemId() == R.id.home) {
                viewPager.setCurrentItem(1); // HomeFragment
            } else if (item.getItemId() == R.id.thema) {
                viewPager.setCurrentItem(2); // MainFragment
            } else if (item.getItemId() == R.id.setting) {
                viewPager.setCurrentItem(3); // SettingFragment
            }
            return true;
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    navigationMenu.setSelectedItemId(R.id.recent);
                } else if (position == 1) {
                    navigationMenu.setSelectedItemId(R.id.home);
                } else if (position == 2) {
                    navigationMenu.setSelectedItemId(R.id.thema);
                } else if (position == 3) {
                    navigationMenu.setSelectedItemId(R.id.setting);
                }
            }
        });
    }
}
