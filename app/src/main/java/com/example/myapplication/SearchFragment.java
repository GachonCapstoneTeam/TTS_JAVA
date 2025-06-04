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
import com.example.myapplication.entity.Item;
import com.facebook.shimmer.ShimmerFrameLayout;
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

    private ShimmerFrameLayout shimmerFrameLayout;


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
        shimmerFrameLayout = view.findViewById(R.id.search_shimmer_container);
        shimmerFrameLayout.startShimmer();

        LinearLayout shimmerLayout = view.findViewById(R.id.shimmer_linear);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        for (int i = 0; i < 5; i++) {
            View skeletonView = layoutInflater.inflate(R.layout.itembox_skeleton, shimmerLayout, false);
            shimmerLayout.addView(skeletonView);
        }

        swipeRefreshLayout = view.findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            allItems.clear();
            currentQuery = null; // 검색어 초기화

            showShimmer();

            fetchItemsFromServer("all");
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


        fetchItemsFromServer("all");
        return view;
    }


    private void fetchItemsFromServer(String category) {
        isLoading = true;
        OkHttpClient client = new OkHttpClient();
        String url;

        if (currentQuery != null && !currentQuery.isEmpty()) {
            url = "http://10.0.2.2:8000/textload/search/?q=" + currentQuery;
        } else {
            if ("stock".equals(category)) {
                url = "http://10.0.2.2:8000/textload/stock/";
            } else if ("industry".equals(category)) {
                url = "http://10.0.2.2:8000/textload/industry/";
            } else {
                url = "http://10.0.2.2:8000/textload/content";
            }
        }

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isLoading = false;
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
                    loadDummyData();

                    hideShimmer();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        List<Item> items = parseJsonAndAddItems(jsonResponse);

                        requireActivity().runOnUiThread(() -> {
                            reportAdapter.setItems(items);
                            hideShimmer();
                            isLoading = false;
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    isLoading = false;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "API 요청 실패", Toast.LENGTH_SHORT).show();
                        loadDummyData();
                        hideShimmer();
                    });
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
                    itemObject.getString("Views"),
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
        fetchItemsFromServer("all");
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
        currentQuery = query;
        fetchItemsFromServer("all");
    }



    private void filterItemsByTab(int tabPosition) {
        switch (tabPosition) {
            case 0: // 전체
                fetchItemsFromServer("all");
                break;
            case 1: // 종목 (기업)
                fetchItemsFromServer("stock");
                break;
            case 2: // 산업
                fetchItemsFromServer("industry");
                break;

        }
    }

    private void showShimmer() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        recyclerView.setVisibility(View.GONE);
    }
    private void hideShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);

        recyclerView.setVisibility(View.VISIBLE);
    }


    private void loadDummyData() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("정기", "Mirae Asset Minutes (2025년 10호)", "에스엠", "엔터테인먼트 섹터는 하반기에 실적 모멘텀이 뒷받침 되면서 주가 상승세가 이어질 것으로 기대. 북미 모멘텀이 재개되고, 구조적 성장 요인으로서 MD 매출 기반 강화 긍정적. 중국 공연시장 개방 기대감도 높아지고 있음. 탑픽 에스엠. 목표가 16만원. 2사분기에는 디어유 연결 편입, 성장성 높은 에스파, RIIZE, NCT WISH 컴백 등으로 역대 최대 실적 및 밸류에이션 상향이 기대됨. 중장기적으로도 에스파의 미국 가능성 등 주력 아이돌 그룹의 실적 기여도 상향 가능성 잠재.", "123", "2025-05-27", "https://example.com/sample1.pdf", "PDF 내용 예시 1"));
        dummyItems.add(new Item("기업", "가동률 반등, 실적 반영은 2분기부터", "DB하이텍", "동사의 1Q25 실적은 매출액 2,974억 원(QoQ +4.9%), 영업이익 525억 원(QoQ +48.8%)으로 전 분기 대비 개선되었으며, 이는 전방 산업 전반적으로 지속되던 재고 조정 국면이 바닥을 지난 영향으로 보인다. 한편 웨이퍼 투입 기준으로 2개월 시차가 존재하기에 3월 가동률 반등 효과는 2Q24 실적에 본격적으로 반영될 것으로 예상되며, 이에 따라 2분기 실적은 보다 안정적인 회복세가 기대된다. 다만, ASP는 전력 반도체의 구조적 특성상 가격 인상이 제한적이고, 과거 모델 비중이 여전히 높은 점을 고려할 때 단기적 상승 여력은 제한적일 것으로 보인다. 자동차향 비중은 4Q24 일시적 하락(8%) 이후 1Q25 10% 수준으로 회복되었으며, 이는 4Q24 TI 물량 공세로 인해 축적되었던 시장 재고가 정상화된 영향으로 보인다.동사는 2027년 기준 생산능력 부족 가능성에 대비해 클린룸 및 유틸리티 인프라 사전확보 차원의 중장기 설비투자 계획을 갖고 있다. 해당 투자는 총 2,500억 원 규모로 웨이퍼 기준 약 30K 수준의 생산능력 확보가 기대된다. 최근 Si Capacitor, GaN 등 신사업이 차질 없이 진행 중이며, Si Capacitor는 6월 초도 생산, 4분기 본격 양산 예정, GaN은 글로벌 IDM, 기존 고객사 등으로 생플 공급 중인 것으로 파악된다. 동사는 2023년 말 기준 자사주를 7.2% 보유하고 있으며, 5개년 내 이를 15%까지 확대할 계획이다. 향후 자사주 소각 법제화 가능성에 따른 내부적인 대응책 또한 준비하는 등 정치적 변수에 따른 리스크 보완책 강구 중에 있다.", "21", "2025-05-30", "",""));
        dummyItems.add(new Item("정기", "3월 경제 정기 보고서", "미래에셋", "2024년 3월 경제 보고서 개요", "321", "2024-03-03", "https://example.com/sample3.pdf", "PDF 내용 예시 3"));

        allItems.clear();
        allItems.addAll(dummyItems);

        requireActivity().runOnUiThread(() -> reportAdapter.setItems(allItems));
    }

}