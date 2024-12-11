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
import com.example.myapplication.view.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> allItems = new ArrayList<>(); // 전체 데이터를 저장
    private List<Item> currentItems = new ArrayList<>(); // 현재 페이지 데이터를 저장
    private int currentPage = 1; // 현재 페이지 번호
    private final int pageSize = 7; // 한 페이지에 표시할 데이터 개수

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportAdapter = new ReportAdapter(getContext());
        recyclerView.setAdapter(reportAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1; // 페이지를 초기화
            allItems.clear(); // 기존 데이터 초기화
            fetchItemsFromServer(); // 서버에서 새 데이터 가져오기
            swipeRefreshLayout.setRefreshing(false); // 리프레시 종료
        });

        // 데이터 가져오기
        fetchItemsFromServer();

        return view;
    }

    // 서버에서 데이터 가져오기
    private void fetchItemsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://40.82.148.190:8000/textload/content/") // 서버 API URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        parseJsonAndAddItems(jsonResponse);

                        getActivity().runOnUiThread(() -> {
                            loadPage(currentPage); // 첫 페이지 로드
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // JSON 데이터를 파싱하고 allItems 리스트에 추가
    private void parseJsonAndAddItems(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray contentsArray = jsonObject.getJSONArray("contents");

        for (int i = 0; i < contentsArray.length(); i++) {
            JSONObject itemObject = contentsArray.getJSONObject(i);

            Item item = new Item(
                    itemObject.getString("Category"),
                    itemObject.getString("Title"),
                    itemObject.getString("증권사"),
                    itemObject.getString("Content"),
                    Integer.parseInt(itemObject.getString("Views")),
                    itemObject.getString("작성일"),
                    itemObject.getString("PDF URL")
            );

            allItems.add(item);
        }
    }

    // 특정 페이지의 데이터 로드
    private void loadPage(int page) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allItems.size());

        if (startIndex < allItems.size()) {
            currentItems.clear();
            currentItems.addAll(allItems.subList(startIndex, endIndex));
            reportAdapter.setItems(currentItems);
        } else {
            Toast.makeText(getContext(), "No more data to load", Toast.LENGTH_SHORT).show();
        }
    }

    // 다음 페이지 로드
    public void loadNextPage() {
        if (currentPage * pageSize < allItems.size()) {
            currentPage++;
            loadPage(currentPage);
        } else {
            Toast.makeText(getContext(), "You are on the last page", Toast.LENGTH_SHORT).show();
        }
    }

    // 이전 페이지 로드
    public void loadPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
        } else {
            Toast.makeText(getContext(), "You are on the first page", Toast.LENGTH_SHORT).show();
        }
    }
}
