package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ReportAdapter;
import com.example.myapplication.data.Report;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView reportRecyclerView = view.findViewById(R.id.reportRecycler);

        // 샘플 데이터 생성
        List<Report> reportList = new ArrayList<>();
        reportList.add(new Report("종목명1", "제목1", "증권사1", "script1"));
        reportList.add(new Report("종목명2", "제목2", "증권사2", "script2"));
        reportList.add(new Report("종목명3", "제목3", "증권사3", "script3"));
        reportList.add(new Report("종목명4", "제목4", "증권사4", "script4"));
        // 필요한 만큼의 데이터를 추가

        ReportAdapter adapter = new ReportAdapter(reportList);
        reportRecyclerView.setAdapter(adapter);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }
}
