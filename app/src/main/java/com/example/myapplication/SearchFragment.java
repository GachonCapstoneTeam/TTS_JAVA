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
import com.example.myapplication.entity.Item;
import com.example.myapplication.service.ApiService;
import com.example.myapplication.Client.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private boolean isLoading = false; // 로딩 상태 확인
    private int currentPage = 1; // 현재 페이지 번호
    private final int totalPage = 5; // 총 페이지 수 (서버 데이터가 제한된 경우)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();

        // 초기 데이터 로드
        fetchDataFromServer(currentPage);

        //itemAdapter = new ItemAdapter(getContext());
        recyclerView.setAdapter(itemAdapter);

        itemAdapter.setItems(itemList);

        // 스크롤 이벤트 리스너 추가
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null &&
                        layoutManager.findLastCompletelyVisibleItemPosition() == itemList.size() - 1) {
                    // 추가 데이터 로드
                    if (currentPage < totalPage) {
                        currentPage++;
                        fetchDataFromServer(currentPage);
                    }
                }
            }
        });

        return view;
    }

    private void fetchDataFromServer(int page) {
        isLoading = true; // 데이터 로딩 상태로 변경

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Map<String, List<Map<String, String>>>> call = apiService.getContents(page);

        call.enqueue(new Callback<Map<String, List<Map<String, String>>>>() {
            @Override
            public void onResponse(Call<Map<String, List<Map<String, String>>>> call,
                                   Response<Map<String, List<Map<String, String>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, String>> contents = response.body().get("contents");
                    List<Item> newItems = new ArrayList<>();

                    for (Map<String, String> content : contents) {
                        newItems.add(new Item(
                                content.get("Category"),
                                content.get("Title"),
                                content.get("증권사"),
                                content.get("PDF URL"),
                                content.get("작성일"),
                                content.get("Views"),
                                content.get("Content")
                                ));
                    }

                    getActivity().runOnUiThread(() -> {
                        itemList.addAll(newItems);
                        itemAdapter.setItems(itemList); // 데이터 갱신
                        isLoading = false; // 로딩 상태 해제
                    });
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
                    );
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<Map<String, String>>>> call, Throwable t) {
                t.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                );
                isLoading = false;
            }
        });
    }
}
