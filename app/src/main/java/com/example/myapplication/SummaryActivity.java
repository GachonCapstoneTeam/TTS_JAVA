package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);

        Button backButton = findViewById(R.id.backbutton_sum);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String stockName = intent.getStringExtra("stock_name");
        String stockTitle = intent.getStringExtra("stock_title");
        String bank = intent.getStringExtra("bank");
        String script = intent.getStringExtra("script");
        int id = intent.getIntExtra("id", -1);

        TextView stockNameText = findViewById(R.id.sumName);
        TextView stockTitleText = findViewById(R.id.sumTitle);
        TextView bankText = findViewById(R.id.sumBank);
        TextView scriptText = findViewById(R.id.sumScript);
        TextView idText = findViewById(R.id.sumId);

        stockNameText.setText(stockName);
        stockTitleText.setText(stockTitle);
        bankText.setText(bank);
        scriptText.setText(script);
        idText.setText(String.valueOf(id));
    }
}
