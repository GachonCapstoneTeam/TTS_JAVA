package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OriginalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        TextView titleText = findViewById(R.id.ori_title);
        Button backButton = findViewById(R.id.backbutton_ori);

        // 전달된 데이터 설정
        Intent intent = getIntent();
        String itemTitle = intent.getStringExtra("item_title");
        titleText.setText(itemTitle);

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());
    }
}
