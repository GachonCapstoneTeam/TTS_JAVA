// MainFragment.java
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
import com.google.android.material.tabs.TabLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> allItems = new ArrayList<>(); // 전체 데이터를 저장
    private List<Item> filteredItems = new ArrayList<>(); // 필터링된 데이터를 저장
    private boolean isLoading = false; // 로딩 상태를 추적
    private int currentPage = 1; // 현재 페이지 번호
    private final int pageSize = 7; // 한 페이지에 표시할 데이터 개수
    private TabLayout tabLayout;

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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                // 스크롤의 끝에 가까워졌을 때만 로드 (현재 위치 + 여유 항목 >= 총 항목)
                int visibleThreshold = 1; // 트리거를 위한 남은 항목 수
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    loadNextPage();
                }
            }
        });

        tabLayout = view.findViewById(R.id.tab_layout); // TabLayout 초기화
        setupTabListener(); // Tab 클릭 리스너 설정

        // 데이터 가져오기
        fetchItemsFromServer();

        return view;
    }

    // 서버에서 데이터 가져오기
    private void fetchItemsFromServer() {
        isLoading = true; // 로딩 중 상태로 설정

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8000/textload/content/?page=" + currentPage + "&size=" + pageSize; // 페이지 번호와 크기 전달

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false; // 로딩 상태 해제
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch data, loading dummy data", Toast.LENGTH_SHORT).show();
                    loadDummyData(); // 더미 데이터 로드
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        parseJsonAndAddItems(jsonResponse);

                        getActivity().runOnUiThread(() -> {
                            reportAdapter.setItems(allItems); // 어댑터에 전체 데이터 갱신
                            isLoading = false; // 로딩 상태 해제
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        isLoading = false; // 로딩 상태 해제
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                            loadDummyData(); // 파싱 오류 시 더미 데이터 로드
                        });
                    }
                } else {
                    isLoading = false; // 로딩 상태 해제
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error fetching data, loading dummy data", Toast.LENGTH_SHORT).show();
                        loadDummyData(); // 오류 시 더미 데이터 로드
                    });
                }
            }
        });
    }

    // 더미 데이터를 추가하는 메서드
    private void loadDummyData() {
        allItems.clear(); // 기존 데이터 초기화

        allItems.add(new Item("기업", "더미 데이터 1", "삼성증권", "내용1", 100, "2025-02-01", "http://dummy.pdf", "PDF 내용 1"));
        allItems.add(new Item("산업", "더미 데이터 2", "LG증권", "내용2", 200, "2025-02-02", "http://dummy.pdf", "PDF 내용 2"));
        allItems.add(new Item("정기", "더미 데이터 3", "한화증권", "내용3", 300, "2025-02-03", "http://dummy.pdf", "PDF 내용 3"));

        getActivity().runOnUiThread(() -> reportAdapter.setItems(allItems)); // 더미 데이터 어댑터에 설정
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
                    itemObject.getString("PDF URL"),
                    itemObject.optString("PDF Content","")
            );

            allItems.add(item);
        }
    }

    // 다음 페이지 로드
    private void loadNextPage() {
        currentPage++; // 다음 페이지 번호 증가
        fetchItemsFromServer(); // 서버에서 데이터 가져오기
    }

    // Tab 클릭 리스너 설정
    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterItemsByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // 탭에 따라 아이템 필터링
    private void filterItemsByTab(int tabPosition) {
        filteredItems.clear();

        switch (tabPosition) {
            case 0: // "전체"
                filteredItems.addAll(allItems);
                break;
            case 1: // "종목"
                for (Item item : allItems) {
                    if (item.getCategory().equals("종목분석 리포트")) {
                        filteredItems.add(item);
                    }
                }
                break;
            case 2: // "산업"
                for (Item item : allItems) {
                    if (item.getCategory().equals("산업분석 리포트")) {
                        filteredItems.add(item);
                    }
                }
                break;
            case 3: // "시황"
                for (Item item : allItems) {
                    if (item.getCategory().equals("시황정보 리포트")) {
                        filteredItems.add(item);
                    }
                }

            case 4: // "투자"
                for (Item item : allItems) {
                    if (item.getCategory().equals("투자정보 리포트")) {
                        filteredItems.add(item);
                    }
                }

            case 5: // "경제"
                for (Item item : allItems) {
                    if (item.getCategory().equals("경제분석 리포트")) {
                        filteredItems.add(item);
                    }
                }

            case 6: // "채권"
                for (Item item : allItems) {
                    if (item.getCategory().equals("채권분석 리포트")) {
                        filteredItems.add(item);
                    }
                }
                break;
        }

        reportAdapter.setItems(filteredItems); // 필터링된 데이터를 어댑터에 설정
    }
}
