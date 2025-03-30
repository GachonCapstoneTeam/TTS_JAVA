package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private boolean isLoading = false;
    private int currentPage = 1;
    private final int pageSize = 7;
    private TabLayout tabLayout;
    private EditText searchInput;
    private ImageButton searchButton;
    private boolean isSearchExpanded = false;
    private LinearLayout mainLayout;
    private String currentQuery = null; // 현재 검색어 저장

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportAdapter = new ReportAdapter(getContext());
        recyclerView.setAdapter(reportAdapter);

        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        mainLayout = view.findViewById(R.id.main_layout);

        swipeRefreshLayout = view.findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            allItems.clear();
            currentQuery = null; // 검색어 초기화
            fetchItemsFromServer();
            swipeRefreshLayout.setRefreshing(false);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int visibleThreshold = 1;
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    loadNextPage();
                }
            }
        });

        tabLayout = view.findViewById(R.id.tab_layout);
        setupTabListener();



        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });


        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString().trim();
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                if (!query.isEmpty()) {
                    performSearch(query);

                    // 키보드 내리기
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                    }
                    return true;
                }
            }
            return false;
        });


        fetchItemsFromServer();
        return view;
    }
    // ! 검색 관련 필독 사항 !
    //현재 검색어를 서버로 쿼리로 보내서 필터링하게끔 되어있음. 이 점은 서버에서 처리할지 논의해보아야 함.
    // 서버에서 처리하는게 아니라 프론트 딴에서 처리할 수도 있지만 실시간 데이터 반영이 불가능하고 속도가 매우 느려지는 큰 단점 존재.
    private void fetchItemsFromServer() {
        isLoading = true;
        OkHttpClient client = new OkHttpClient();
        String url = "https://40.82.148.190:8000/textload/content/";
        if (currentQuery != null) {
            url += "?=" + currentQuery;
        }

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()

                );
                loadDummyData();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        parseJsonAndAddItems(jsonResponse);

                        getActivity().runOnUiThread(() -> {
                            reportAdapter.setItems(allItems);
                            isLoading = false;
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        isLoading = false;
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "데이터 파싱 오류", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    isLoading = false;
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "API 요청 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private List<Item> parseJsonAndAddItems(String jsonResponse) throws JSONException {
        List<Item> items = new ArrayList<>();
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
                    itemObject.getString("PDF Content")
            );

            items.add(item);
        }

        return items;
    }

    private void loadNextPage() {
        currentPage++;
        fetchItemsFromServer();
    }

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
    //검색창 닫기
    private void closeSearchBar() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(searchInput, "translationX", 0f, 300f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        searchInput.setVisibility(View.GONE);
        isSearchExpanded = false;
    }

    //검색 요청을 서버에 보내는 부분
    private void performSearch(String query) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://40.82.148.190:8000/textload/content/?search=" + query;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "검색 실패", Toast.LENGTH_SHORT).show();
                    loadDummyData();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        List<Item> searchResults = parseJsonAndAddItems(jsonResponse);

                        requireActivity().runOnUiThread(() -> {
                            reportAdapter.setItems(searchResults);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void filterItemsByTab(int tabPosition) {
        filteredItems.clear();

        switch (tabPosition) {
            case 0: // "전체"
                filteredItems.addAll(allItems);
                break;
            case 1: // "기업"
                for (Item item : allItems) {
                    if (item.getCategory().equals("기업")) {
                        filteredItems.add(item);
                    }
                }
                break;
            case 2: // "산업"
                for (Item item : allItems) {
                    if (item.getCategory().equals("산업")) {
                        filteredItems.add(item);
                    }
                }
                break;
            case 3: // "정기"
                for (Item item : allItems) {
                    if (item.getCategory().equals("정기")) {
                        filteredItems.add(item);
                    }
                }
                break;
        }

        reportAdapter.setItems(filteredItems);
    }

    private void loadDummyData() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("산업", "LG에너지솔루션 2차전지", "하나증권", "배터리 산업의 성장과 기술 분석", 123, "2024-03-01", "https://example.com/sample1.pdf", "PDF 내용 예시 1"));
        dummyItems.add(new Item("기업", "삼성전자 반도체 전략", "삼성증권", "반도체 산업 전망 분석", 234, "2024-03-02", "https://example.com/sample2.pdf", "PDF 내용 예시 2"));
        dummyItems.add(new Item("정기", "3월 경제 정기 보고서", "미래에셋", "2024년 3월 경제 보고서 개요", 321, "2024-03-03", "https://example.com/sample3.pdf", "PDF 내용 예시 3"));

        allItems.clear();
        allItems.addAll(dummyItems);

        requireActivity().runOnUiThread(() -> reportAdapter.setItems(allItems));
    }

}
