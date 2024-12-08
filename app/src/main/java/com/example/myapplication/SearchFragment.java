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

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPage = 5;

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

        itemAdapter = new ItemAdapter(getContext());
        recyclerView.setAdapter(itemAdapter);

        itemAdapter.setItems(itemList);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 스크롤 끝 감지
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

        String url = "http://10.0.2.2:8000/api/items?page=" + page;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                );
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
                                    jsonObject.getString("stockName"),
                                    jsonObject.getString("stockTitle"),
                                    jsonObject.getString("bank"),
                                    jsonObject.getString("script"),
                                    jsonObject.getInt("id")
                            ));
                        }

                        getActivity().runOnUiThread(() -> {
                            itemList.addAll(newItems);
                            itemAdapter.setItems(itemList);
                            isLoading = false;
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                        );
                        isLoading = false;
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
                    );
                    isLoading = false;
                }
            }
        });
    }
}
