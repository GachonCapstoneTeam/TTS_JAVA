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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapter.ReportAdapter;
import com.example.myapplication.item.Item;

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

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private SwipeRefreshLayout swipeRefreshLayout; // SwipeRefreshLayout 추가
    private List<Item> itemList = new ArrayList<>();
    private boolean isLoading = false; // 데이터 로딩 상태
    private int currentPage = 1; // 현재 페이지
    private final int totalPage = 5; // 가정: 총 5페이지 (서버 응답에 따라 수정 가능)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // SwipeRefreshLayout 초기화
        swipeRefreshLayout = view.findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            itemList.clear();
            fetchDataFromServer(currentPage);
        });

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 설정
        reportAdapter = new ReportAdapter(getContext());
        recyclerView.setAdapter(reportAdapter);

        // 초기 데이터 로드
        fetchDataFromServer(currentPage);

        // 스크롤 이벤트 추가 (페이지네이션)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null &&
                        layoutManager.findLastVisibleItemPosition() == itemList.size() - 1) {
                    if (currentPage < totalPage) {
                        currentPage++;
                        fetchDataFromServer(currentPage);
                    }
                }
            }
        });

        return view;
    }

    // 서버에서 데이터 가져오기
    private void fetchDataFromServer(int page) {
        isLoading = true; // 데이터 로딩 중으로 설정
        String url = "http://10.0.2.2:8000/api/items?page=" + page; // 서버 URL

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                getActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false); // 로딩 UI 숨김
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);

                        List<Item> newItems = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            newItems.add(new Item(
                                    jsonObject.getString("stockName"), //종목명
                                    jsonObject.getString("stockTitle"), // 제목
                                    jsonObject.getString("bank"), // 증권사
                                    jsonObject.getString("script"), // 스크립트
                                    jsonObject.getInt("views"), // 조회수
                                    jsonObject.getString("date"), // 날짜
                                    jsonObject.getString("PDF URL") // PDF URL
                            ));
                        }

                        getActivity().runOnUiThread(() -> {
                            if (page == 1) {
                                itemList.clear(); // 새로고침 시 기존 데이터 삭제
                            }
                            itemList.addAll(newItems); // 서버에서 가져온 데이터 추가
                            reportAdapter.setItems(itemList);
                            swipeRefreshLayout.setRefreshing(false); // 로딩 UI 숨김
                            isLoading = false;
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false); // 로딩 UI 숨김
                            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                        });
                        isLoading = false;
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false); // 로딩 UI 숨김
                        Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    });
                    isLoading = false;
                }
            }
        });
    }
}
