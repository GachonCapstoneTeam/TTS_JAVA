package com.example.myapplication;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ReportAdapter;
import com.example.myapplication.data.Report;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage); // mainpage.xml 파일과 연결

        RecyclerView reportRecyclerView = findViewById(R.id.reportRecycler);

        // 샘플 데이터 생성
        List<Report> reportList = new ArrayList<>();
        reportList.add(new Report("종목명1", "제목1", "증권사1", "script1"));
        reportList.add(new Report("종목명2", "제목2", "증권사2", "script2"));
        // 필요한 만큼의 데이터를 추가

        // Adapter 설정
        ReportAdapter adapter = new ReportAdapter(reportList);
        reportRecyclerView.setAdapter(adapter);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

