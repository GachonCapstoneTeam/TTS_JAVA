package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        // ItemAdapter 생성 및 설정
        itemAdapter = new ItemAdapter(getContext(), itemList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onSummaryClick(int position) {
                Toast.makeText(getContext(), "요약본 버튼 클릭: " + itemList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOriginalClick(int position) {
                Toast.makeText(getContext(), "원문 버튼 클릭: " + itemList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(itemAdapter);

        return view;
    }

    private void populateItemList() {
        for (int i = 1; i <= 10; i++) {
            itemList.add(new Item("종목명 " + i, i));
        }
    }
}
