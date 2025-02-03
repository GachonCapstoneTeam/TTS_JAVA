package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.ViewPagerItemAdapter;
import com.example.myapplication.entity.Item;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment {

    private ViewPagerItemAdapter viewPagerItemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        // ViewPager2와 DotsIndicator 초기화
        ViewPager2 viewPager = view.findViewById(R.id.rec_swiper);
        SpringDotsIndicator dotsIndicator = view.findViewById(R.id.rec_dots_indicator);

        // 더미 데이터 생성
        List<Item> dummyItems = getDummyItems();

        // 어댑터 설정
        viewPagerItemAdapter = new ViewPagerItemAdapter(requireContext(), dummyItems);
        viewPager.setAdapter(viewPagerItemAdapter);

        // DotsIndicator와 ViewPager2 연결
        dotsIndicator.setViewPager2(viewPager);

        return view;
    }

    private List<Item> getDummyItems() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("경제", "추천 리포트 1", "삼성증권", "https://example.com/1.pdf", "2025-02-01", "123", "내용 1"));
        dummyItems.add(new Item("기술", "추천 리포트 2", "LG증권", "https://example.com/2.pdf", "2025-02-02", "234", "내용 2"));
        dummyItems.add(new Item("산업", "추천 리포트 3", "한화증권", "https://example.com/3.pdf", "2025-02-03", "345", "내용 3"));
        return dummyItems;
    }
}
