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
import com.example.myapplication.item.Item;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ReportAdapter 초기화
        reportAdapter = new ReportAdapter(getContext());
        recyclerView.setAdapter(reportAdapter);

        // 데이터 추가
        List<Item> itemList = populateItems();
        reportAdapter.setItems(itemList); // 데이터 설정

        return view;
    }

    private List<Item> populateItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            items.add(new Item("종목명 " + i, "제목 " + i, "증권사 " + i, "스크립트 내용 " + i, i));
        }
        return items;
    }
}
