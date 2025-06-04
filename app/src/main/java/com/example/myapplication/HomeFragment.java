package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ItemAdapter;
import com.example.myapplication.adapter.ShimmerAdapter;
import com.example.myapplication.entity.Item;
import com.example.myapplication.view.AudioService;
import com.example.myapplication.view.TTSHelper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.myapplication.util.PreferenceUtil;
import com.facebook.shimmer.ShimmerFrameLayout;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TTSHelper ttsHelper;
    private ImageButton playButton, prevButton, nextButton;
    private Button myWishButton, top10Button, currentListButton, fullScreenButton;
    private SeekBar seekBar;
    private TextView currentTimeText, fullTimeText, emptyView;
    private List<Item> itemList;
    private int currentTrackIndex = 0;
    private MediaPlayer mediaPlayer;
    private AudioService audioService;

    private boolean isHomeServiceBound = false;
    private Handler progressHandler = new Handler();
    private Context mContext;
    private int currentPlayingIndex = -1;

    private ShimmerFrameLayout shimmerLayout;
    private boolean isDataLoaded = false;

    private enum ViewMode {
        CURRENT_LIST,  // 최신목록
        MY_WISH_LIST,  // 내 목록
        TOP10_LIST     // Top10
    }

    private ViewMode currentViewMode = ViewMode.CURRENT_LIST;

    final ViewMode requestViewMode = currentViewMode;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // UI 요소 초기화
        playButton = view.findViewById(R.id.button_play);
        prevButton = view.findViewById(R.id.prev);
        nextButton = view.findViewById(R.id.next);
        fullScreenButton = view.findViewById(R.id.full_screen);
        seekBar = view.findViewById(R.id.progress_bar);
        currentTimeText = view.findViewById(R.id.current_time);
        fullTimeText = view.findViewById(R.id.full_time);
        myWishButton = view.findViewById(R.id.myWish);
        top10Button = view.findViewById(R.id.top10);
        currentListButton = view.findViewById(R.id.current_list);
        emptyView = view.findViewById(R.id.home_empty_view);

        recyclerView = view.findViewById(R.id.home_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(requireContext(), itemList);
        recyclerView.setAdapter(adapter);

        ttsHelper = new TTSHelper(requireContext());

        // 데이터 가져오기
        fetchDataFromServer();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && audioService != null) {
                    int newPosition = (int) ((progress / 100.0) * audioService.getDuration());
                    currentTimeText.setText(formatTime(newPosition)); // 현재 시간 표시
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용자가 ProgressBar를 조작하기 시작할 때 (재생 중이라면 일시정지 가능)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (audioService != null) {
                    int newPosition = (int) ((seekBar.getProgress() / 100.0) * audioService.getDuration());
                    audioService.seekTo(newPosition); // 선택한 위치로 이동
                    updateProgressBar(newPosition, audioService.getDuration()); // UI 업데이트
                }
            }
        });


        // 아이템 클릭 시 오디오 재생
        adapter.setOnItemClickListener(item -> {
            currentTrackIndex = itemList.indexOf(item);
            playTrackAtIndex(currentTrackIndex, true);

            adapter.setPlayingIndex(currentTrackIndex); // 클릭한 인덱스로 갱신
        });

        playButton.setOnClickListener(v -> togglePlayPause());

        prevButton.setOnClickListener(v -> {
            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                playTrackAtIndex(currentTrackIndex, true);
            } else {
                Toast.makeText(requireContext(), "이전 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentTrackIndex < itemList.size() - 1) {
                currentTrackIndex++;
                playTrackAtIndex(currentTrackIndex, true);
            } else {
                Toast.makeText(requireContext(), "다음 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });


        fullScreenButton.setOnClickListener(v -> {
            if (currentTrackIndex >= 0 && currentTrackIndex < itemList.size()) {
                Item currentItem = itemList.get(currentTrackIndex);
                Intent intent = new Intent(getContext(), OriginalActivity.class);
                intent.putExtra("ItemList", new Gson().toJson(itemList));
                intent.putExtra("track_index", currentTrackIndex);
                intent.putExtra("AudioFilePath", getAudioFilePath(currentItem.getTitle()));

                if (audioService != null) {
                    intent.putExtra("AudioPosition", audioService.getCurrentPosition());
                    intent.putExtra("IsPlaying", audioService.isPlaying());
                } else {
                    intent.putExtra("AudioPosition", 0);
                    intent.putExtra("IsPlaying", false);
                }

                intent.putExtra("Title", currentItem.getTitle());
                intent.putExtra("Content", currentItem.getContent());
                intent.putExtra("Category", currentItem.getCategory());
                intent.putExtra("Date", currentItem.getDate());
                intent.putExtra("PDF_URL", currentItem.getPdfUrl());

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "선택된 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        myWishButton.setOnClickListener(v -> {
            updateButtonStyles(myWishButton);
            currentViewMode = ViewMode.MY_WISH_LIST;

            List<Item> likedItems = loadLikedItemsFromPrefs();

            if (likedItems.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setItems(likedItems);

                itemList.clear();
                itemList.addAll(likedItems);
                adapter.setItems(itemList);
            }
        });



        top10Button.setOnClickListener(v -> {
            updateButtonStyles(top10Button);

            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();

            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // TOP 10 필터링 로직 추가 예정
            //fetchDataFromServer(String category) 처럼 분기를 추가하는 방식으로 할지 고민 중.
            //추천 페이지 api수정하면서 같이 수정 요함.
        });

        currentListButton.setOnClickListener(v -> {
            updateButtonStyles(currentListButton);
            currentViewMode = ViewMode.CURRENT_LIST;

            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();

            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            fetchDataFromServer();
        });


        shimmerLayout = view.findViewById(R.id.player_shimmer_container); // itembox_skeleton 포함하는 layout

// 데이터 로딩 전 Shimmer 어댑터로 설정
        ShimmerAdapter shimmerAdapter = new ShimmerAdapter(5); // 5개 정도 보여줄 수 있음
        recyclerView.setAdapter(shimmerAdapter);
        shimmerLayout.startShimmer(); // 애니메이션 시작

        return view;
    }

    private String getAudioFilePath(String fileName) {
        File audioFile = new File(requireContext().getCacheDir(), fileName + ".mp3");
        return audioFile.getAbsolutePath();
    }

    private void playTrackAtIndex(int index, boolean useHomeService) {
        if (index >= 0 && index < itemList.size()) {
            Item track = itemList.get(index);
            Log.d("HomeFragment", "재생할 트랙: " + track.getTitle());

            requireActivity().runOnUiThread(() -> {
                // UI 업데이트
                TextView titleView = requireView().findViewById(R.id.home_display_title);
                TextView nameView = requireView().findViewById(R.id.home_display_name);


                if (titleView != null && nameView != null) {
                    titleView.setText(track.getTitle());
                    nameView.setText(track.getStockName());
                }

                seekBar.setProgress(0);
                currentTimeText.setText("00:00");
                fullTimeText.setText("00:00");

                ttsHelper.performTextToSpeech(track.getContent(), track.getTitle() + ".mp3", audioFile -> {
                    if (audioFile.exists()) {
                        Log.d("HomeFragment", "TTS 변환 완료 → 오디오 파일 저장됨: " + audioFile.getAbsolutePath());

                        requireActivity().runOnUiThread(() -> {
                            if (useHomeService && audioService != null) {
                                Log.d("HomeFragment", "HomeAudioService에서 오디오 준비");
                                audioService.prepareAudio(audioFile.getAbsolutePath(), 0, true);
                            } else {
                                Log.e("HomeFragment", "서비스가 바인딩되지 않음! 재생 불가");
                            }
                        });
                    } else {
                        Log.e("HomeFragment", "TTS 변환 후 오디오 파일이 존재하지 않음!");
                    }
                });

                playButton.setImageResource(R.drawable.button_pause);
            });
        } else {
            Log.e("HomeFragment", "잘못된 인덱스! index: " + index);
        }
    }

    private void togglePlayPause() {
        if (audioService != null && audioService.isPlaying()) {
            audioService.pauseAudio();
            playButton.setImageResource(R.drawable.button_play);
        } else {
            audioService.resumeAudio();
            playButton.setImageResource(R.drawable.button_pause);
        }
    }

    private final ServiceConnection homeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();
            isHomeServiceBound = true;

            audioService.setProgressUpdateListener((currentPosition, duration) ->
                    requireActivity().runOnUiThread(() -> updateProgressBar(currentPosition, duration))
            );

            audioService.setNextTrackListener(() ->
                    requireActivity().runOnUiThread(() -> {
                        playNextTrack();
                        adapter.setPlayingIndex(currentTrackIndex);

                    })
            );

            audioService.startProgressUpdates();
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            isHomeServiceBound = false;
        }
    };
    private List<Item> loadLikedItemsFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        List<Item> likedItems = new ArrayList<>();
        Gson gson = new Gson();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("liked_")) {
                String json = (String) entry.getValue();
                Item item = gson.fromJson(json, Item.class);
                likedItems.add(item);
            }
        }

        return likedItems;
    }


    private void playNextTrack() {
        if (currentTrackIndex < itemList.size() - 1) {
            currentTrackIndex++;
            playTrackAtIndex(currentTrackIndex, true);
        } else {
            Toast.makeText(requireContext(), "마지막 트랙입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mContext != null) {
            Intent serviceIntent = new Intent(mContext, AudioService.class);

            // 서비스가 실행되지 않았다면 먼저 실행
            mContext.startService(serviceIntent);

            // 실행 후 바인딩
            boolean result = mContext.bindService(serviceIntent, homeServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d("HomeFragment", "HomeAudioService 바인딩 시도 결과: " + result);
            if (result) {
                Log.d("HomeFragment", "HomeAudioService 바인딩 성공");
            } else {
                Log.e("HomeFragment", "HomeAudioService 바인딩 실패");
            }
        } else {
            Log.e("HomeFragment", "onStart()에서 mContext가 null임");
        }

        if (audioService != null) {
            int currentPosition = audioService.getCurrentPosition();
            int totalDuration = audioService.getDuration();

            currentPlayingIndex = audioService.getCurrentIndex();
            adapter.setPlayingIndex(currentPlayingIndex);
            updateProgressBar(currentPosition, totalDuration);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver,
                new IntentFilter("com.example.myapplication.LIKED_UPDATED"));
        if (audioService != null) {
            Log.d("HomeFragment", "onResume에서 리스너 재설정");

            if (audioService.isPlaying()) {
                playButton.setImageResource(R.drawable.button_pause);
            } else {
                playButton.setImageResource(R.drawable.button_play);
            }

            audioService.setProgressUpdateListener((currentPosition, duration) ->
                    requireActivity().runOnUiThread(() -> updateProgressBar(currentPosition, duration))
            );

            audioService.setNextTrackListener(() ->
                    requireActivity().runOnUiThread(this::playNextTrack)
            );

            audioService.startProgressUpdates();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (isHomeServiceBound) mContext.unbindService(homeServiceConnection);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        if (audioService != null) {
            audioService.setProgressUpdateListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // 메모리 해제
            mediaPlayer = null;
        }
    }
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLikedList();
        }
    };

    private void updateLikedList() {
        List<Item> likedItems = new ArrayList<>();
        Map<String, ?> allPrefs = PreferenceUtil.getAll();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().startsWith("liked_")) {
                String json = entry.getValue().toString();
                Item item = new Gson().fromJson(json, Item.class);
                likedItems.add(item);
            }
        }

        adapter.setItems(likedItems);
        recyclerView.setAdapter(adapter);
    }

    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8000/textload/content";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {

                    requireActivity().runOnUiThread(() -> {
                        if (currentViewMode != requestViewMode) return;
                        shimmerLayout.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        loadDummyData();
                    });
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        List<Item> items = parseAndSetData(jsonResponse);

                        requireActivity().runOnUiThread(() -> {

                            if (currentViewMode != ViewMode.CURRENT_LIST) {
                                shimmerLayout.stopShimmer();
                                shimmerLayout.setVisibility(View.GONE);
                                return;
                            }

                            syncLikedStateWithPrefs(items);

                            itemList.clear();
                            itemList.addAll(items);
                            adapter.setItems(itemList);  // 어댑터에 전달
                            adapter.notifyDataSetChanged(); // 혹시 모르니 안전하게 호출

                            // Shimmer 중단 및 실제 리스트 보여주기
                            shimmerLayout.stopShimmer();
                            shimmerLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "데이터 파싱 오류", Toast.LENGTH_SHORT).show();
                            loadDummyData();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "서버 응답 오류", Toast.LENGTH_SHORT).show();
                        loadDummyData(); // 응답 오류 시 더미 데이터 로드
                    });
                }
            }
        });
    }

    private void syncLikedStateWithPrefs(List<Item> items) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        for (Item item : items) {
            String key = "liked_" + item.getTitle();
            if (prefs.contains(key)) {
                item.setLiked(true);
            } else {
                item.setLiked(false);
            }
        }
    }


    private void updateProgressBar(int currentPosition, int duration) {
        if (duration > 0) {
            int progress = (int) ((currentPosition / (float) duration) * 100);

            if (currentPosition >= duration - 1000) {  // 1초 이내 남았을 때 100%로 설정
                progress = 100;
            }

            seekBar.setProgress(progress);
        }

        currentTimeText.setText(formatTime(currentPosition));
        fullTimeText.setText(formatTime(duration));
    }

    private void updateButtonStyles(Button selectedButton) {
        Button[] buttons = {myWishButton, top10Button, currentListButton};
        for (Button button : buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.list_button_green); // 선택된 버튼
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                button.setBackgroundResource(R.drawable.list_button); // 기본 버튼
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }
        }
    }

    private void loadDummyData() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("기업", "가동률 반등, 실적 반영은 2분기부터", "DB하이텍", "동사의 1Q25 실적은 매출액 2,974억 원(QoQ +4.9%), 영업이익 525억 원(QoQ +48.8%)으로 전 분기 대비 개선되었으며, 이는 전방 산업 전반적으로 지속되던 재고 조정 국면이 바닥을 지난 영향으로 보인다. 한편 웨이퍼 투입 기준으로 2개월 시차가 존재하기에 3월 가동률 반등 효과는 2Q24 실적에 본격적으로 반영될 것으로 예상되며, 이에 따라 2분기 실적은 보다 안정적인 회복세가 기대된다. 다만, ASP는 전력 반도체의 구조적 특성상 가격 인상이 제한적이고, 과거 모델 비중이 여전히 높은 점을 고려할 때 단기적 상승 여력은 제한적일 것으로 보인다. 자동차향 비중은 4Q24 일시적 하락(8%) 이후 1Q25 10% 수준으로 회복되었으며, 이는 4Q24 TI 물량 공세로 인해 축적되었던 시장 재고가 정상화된 영향으로 보인다.동사는 2027년 기준 생산능력 부족 가능성에 대비해 클린룸 및 유틸리티 인프라 사전확보 차원의 중장기 설비투자 계획을 갖고 있다. 해당 투자는 총 2,500억 원 규모로 웨이퍼 기준 약 30K 수준의 생산능력 확보가 기대된다. 최근 Si Capacitor, GaN 등 신사업이 차질 없이 진행 중이며, Si Capacitor는 6월 초도 생산, 4분기 본격 양산 예정, GaN은 글로벌 IDM, 기존 고객사 등으로 생플 공급 중인 것으로 파악된다. 동사는 2023년 말 기준 자사주를 7.2% 보유하고 있으며, 5개년 내 이를 15%까지 확대할 계획이다. 향후 자사주 소각 법제화 가능성에 따른 내부적인 대응책 또한 준비하는 등 정치적 변수에 따른 리스크 보완책 강구 중에 있다.", "21", "2025-05-30", "",""));
        dummyItems.add(new Item("기업", "FY1Q26 Review: AI 수요 급증에도 영업이익 부진", "DELL US", "AI 통합 시계열 분석 결과, Dell Technologies의 주요 지표들은 AI 모델의 예측치를 대체로 하회했다. 특히 영업이익과 인프라솔루션그룹(ISG) 매출액이 의미 있게 하회한 반면, 고객솔루션그룹(CSG) 매출액은 상회했다. 전체 매출액은 컨센서스 추정치에 부합했으나 영업이익은 하회했다. 경영진은 모든 핵심 사업이 성장하여 첫 분기 매출이 234억 달러에 도달했다고 밝혔다. AI 최적화 서버에 대한 수요가 급증하고 있다고 언급했다. AI 주문은 이번 분기만으로 121억 달러에 달해 FY25 전체 출하량을 초과했다고 덧붙였다.\n" +
                "\n" +
                "Dell Technologies 주가는 지난 3개월간 11.3% 상승하며 나스닥과 S&P500을 초과했다. 현재 주가는 PER 11.8배로 과거 평균보다 낮으며, 최근 지수와의 상관관계는 0.81로 강해졌다.", "33", "2025-05-30", "",""));
        dummyItems.add(new Item("산업", "[2025년 하반기 전망] 에너지/정유화학 (비중확대/유지)", "한화솔루션/S-OIL", "https://example.com/dummy3.pdf", "71", "2025-05-27", "",""));
        dummyItems.add(new Item("산업", "아직도 불안한 가다실", "MRK US", "https://example.com/dummy3.pdf", "71", "2025-05-27", "",""));

        requireActivity().runOnUiThread(() -> {
            itemList.clear();
            itemList.addAll(dummyItems);

            recyclerView.setAdapter(adapter); // ⭐ 중요!
            adapter.setItems(itemList);
            adapter.notifyDataSetChanged();

            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });
    }


    //시간 변환 함수
    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private List<Item> parseAndSetData(String jsonResponse) throws JSONException {
        List<Item> fetchedItems = new ArrayList<>();

        // 먼저 전체 JSON 응답을 JSONObject로 파싱
        JSONObject rootObject = new JSONObject(jsonResponse);

        // "contents" 키에서 JSONArray 추출
        JSONArray jsonArray = rootObject.getJSONArray("contents");

        // JSONArray 순회하며 Item 생성
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Item item = new Item(
                    jsonObject.getString("Category"),  // 대소문자 정확히 일치시켜야 함
                    jsonObject.getString("Title"),
                    jsonObject.getString("증권사"),      // "stockName"이 아닌 JSON에는 "증권사"로 들어 있음
                    jsonObject.getString("PDF URL"),
                    jsonObject.getString("작성일"),
                    jsonObject.getString("Views"),
                    jsonObject.getString("Content"),
                    jsonObject.getString("PDF Content")
                    );

            fetchedItems.add(item);
        }

        return fetchedItems;
    }
}

