package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OriginalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        Button backButton = findViewById(R.id.backbutton_ori);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 Activity 종료
            }
        });
        // 데이터 받아오기
        Intent intent = getIntent();
        String stockName = intent.getStringExtra("stock_name");
        String stockTitle = intent.getStringExtra("stock_title");
        String bank = intent.getStringExtra("bank");
        String script = intent.getStringExtra("script");
        int id = intent.getIntExtra("id", -1);

        // TextView에 데이터 설정
        TextView stockNameText = findViewById(R.id.oriName);
        TextView stockTitleText = findViewById(R.id.oriTitle);
        TextView bankText = findViewById(R.id.oriBank);
        TextView scriptText = findViewById(R.id.oriScript);
        TextView idText = findViewById(R.id.oriId);

        stockNameText.setText(stockName);
        stockTitleText.setText(stockTitle);
        bankText.setText(bank);
        scriptText.setText(script);
        idText.setText(String.valueOf(id));
    }
}
