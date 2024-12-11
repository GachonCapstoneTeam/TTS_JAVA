package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationBarView;

public class basic_layout extends AppCompatActivity {

    MainFragment mainFragment;
    SearchFragment searchFragment;
    SettingFragment settingFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_layout);

        mainFragment = new MainFragment();
        searchFragment = new SearchFragment();
        settingFragment = new SettingFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.navigation_menu);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.main) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();

                    return true;

                } /*else if (itemId == R.id.search) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                    return true;
                } else if (itemId == R.id.setting) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, settingFragment).commit();
                    return true;
                }*/ else {
                    return false;
                }

            }
        });


    }


}