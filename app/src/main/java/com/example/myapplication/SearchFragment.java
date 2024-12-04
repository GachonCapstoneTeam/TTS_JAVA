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

import com.example.myapplication.adapter.ItemAdapter;
import com.example.myapplication.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        populateItemList();

        itemAdapter = new ItemAdapter(getContext());
        recyclerView.setAdapter(itemAdapter);

        itemAdapter.setItems(itemList);

        return view;
    }
//test
    private void populateItemList() {
        for (int i = 1; i <= 10; i++) {

            itemList.add(new Item("종목명 " + i,
                    "제목 " + i,
                    "증권사 " + i,
                    "스크립트 내용 " + i,
                    (int)(Math.random() * 100) * i));
        }
    }
}
