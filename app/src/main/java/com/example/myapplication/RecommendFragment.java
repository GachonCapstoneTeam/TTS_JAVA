package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.RankPagerAdapter;
import com.example.myapplication.adapter.RecommendPagerAdapter;
import com.example.myapplication.entity.Item;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecommendFragment extends Fragment {

    private ViewPager2 recommendViewPager, rankViewPager;
    private SpringDotsIndicator recommendDots, rankDots;
    private RecommendPagerAdapter recommendAdapter;
    private RankPagerAdapter rankAdapter;

    private List<Item> recommendItems = new ArrayList<>();
    private List<Item> rankItems = new ArrayList<>();

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private int recommendCurrentPage = 0;
    private int rankCurrentPage = 0;

    private final String API_URL = "http://10.0.2.2:8000/textload/content/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommand, container, false);

        // View 초기화
        recommendViewPager = view.findViewById(R.id.rcm_recommend_page);
        recommendDots = view.findViewById(R.id.rcm_recommend_indicator);

        rankViewPager = view.findViewById(R.id.rcm_swiper);
        rankDots = view.findViewById(R.id.rcm_dots_indicator);

        // 어댑터 초기화
        recommendAdapter = new RecommendPagerAdapter(recommendItems);
        recommendViewPager.setAdapter(recommendAdapter);
        recommendDots.setViewPager2(recommendViewPager);

        rankAdapter = new RankPagerAdapter(getContext(), rankItems);
        rankViewPager.setAdapter(rankAdapter);
        rankDots.setViewPager2(rankViewPager);

        // 데이터 불러오기 (로드 후 슬라이더 시작)
        fetchDataForRecommend();
        fetchDataForRank();

        return view;
    }

    // 추천 리포트 데이터 가져오기
    private void fetchDataForRecommend() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "추천 리포트 로딩 실패, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                    loadDummyRecommendData();
                    startAutoSlide();  // 더미 데이터 로드 후 슬라이더 시작
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        recommendItems = parseItemsFromJson(jsonData);
                        requireActivity().runOnUiThread(() -> {
                            recommendAdapter.updateItems(recommendItems);
                            startAutoSlide();  // 데이터 로드 후 슬라이더 시작
                        });
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "추천 리포트 파싱 실패, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                            loadDummyRecommendData();
                            startAutoSlide();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "추천 리포트 API 오류, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                        loadDummyRecommendData();
                        startAutoSlide();
                    });
                }
            }
        });
    }

    // 인기 리포트 데이터 가져오기
    private void fetchDataForRank() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "인기 리포트 로딩 실패, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                    loadDummyRankData();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        rankItems = parseItemsFromJson(jsonData);
                        requireActivity().runOnUiThread(() -> rankAdapter.updateItems(rankItems));
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "인기 리포트 파싱 실패, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                            loadDummyRankData();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "인기 리포트 API 오류, 더미 데이터 로드", Toast.LENGTH_SHORT).show();
                        loadDummyRankData();
                    });
                }
            }
        });
    }

    // JSON 데이터 파싱
    private List<Item> parseItemsFromJson(String jsonData) throws JSONException {
        List<Item> items = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray contentsArray = jsonObject.getJSONArray("contents");

        for (int i = 0; i < contentsArray.length(); i++) {
            JSONObject obj = contentsArray.getJSONObject(i);

            String category = obj.getString("Category");
            String title = obj.getString("Title");
            String stockName = obj.getString("증권사");
            String pdfUrl = obj.getString("PDF URL");
            String date = obj.getString("작성일");
            String views = obj.getString("Views");
            String content = obj.getString("Content");

            items.add(new Item(category, title, stockName, pdfUrl, date, views, content));
        }

        return items;
    }

    // 더미 데이터 (추천 리포트)
    private void loadDummyRecommendData() {
        recommendItems.clear();
        recommendItems.add(new Item("IT", "삼성전자 반도체 전망", "삼성증권", "https://example.com/sample1.pdf", "2024-02-15", "120", "반도체 시장의 향후 전망을 분석한 리포트입니다."));
        recommendItems.add(new Item("플랫폼", "네이버 AI 전략", "NH투자증권", "https://example.com/sample2.pdf", "2024-02-14", "98", "네이버 AI 서비스 전략에 대한 분석."));
        recommendItems.add(new Item("자동차", "현대차 전기차 전망", "미래에셋", "https://example.com/sample3.pdf", "2024-02-13", "76", "현대차의 전기차 시장 전략과 전망."));
        recommendAdapter.updateItems(recommendItems);
    }

    // 더미 데이터 (인기 리포트)
    private void loadDummyRankData() {
        rankItems.clear();
        rankItems.add(new Item("화학", "LG화학 배터리 시장", "키움증권", "https://example.com/sample4.pdf", "2024-02-15", "150", "배터리 시장 동향 및 LG화학의 전략."));
        rankItems.add(new Item("IT", "SK하이닉스 메모리 시장", "하나금융투자", "https://example.com/sample5.pdf", "2024-02-14", "110", "메모리 반도체 시장의 향후 전망."));
        rankItems.add(new Item("유통", "쿠팡 성장성 분석", "대신증권", "https://example.com/sample6.pdf", "2024-02-13", "85", "쿠팡의 성장성과 미래 전략."));
        rankAdapter.updateItems(rankItems);
    }

    // 자동 슬라이드 시작 (데이터 로드 후 실행)
    private void startAutoSlide() {
        Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                // 추천 리포트 슬라이드
                if (recommendViewPager.getAdapter() != null && recommendViewPager.getAdapter().getItemCount() > 0) {
                    int totalRecommendItems = recommendViewPager.getAdapter().getItemCount();
                    recommendCurrentPage = (recommendCurrentPage + 1) % totalRecommendItems;
                    recommendViewPager.setCurrentItem(recommendCurrentPage, true);
                }

                // 인기 리포트 슬라이드
                if (rankViewPager.getAdapter() != null && rankViewPager.getAdapter().getItemCount() > 0) {
                    int totalRankItems = rankViewPager.getAdapter().getItemCount();
                    rankCurrentPage = (rankCurrentPage + 1) % totalRankItems;
                    rankViewPager.setCurrentItem(rankCurrentPage, true);
                }

                // 3초마다 슬라이드
                sliderHandler.postDelayed(this, 3000);
            }
        };

        // 처음 실행
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacksAndMessages(null);  // 메모리 누수 방지
    }
}