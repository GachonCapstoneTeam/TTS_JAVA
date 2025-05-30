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
                    Toast.makeText(requireContext(), "서버 연결 실패. 더미 데이터를 로드합니다.", Toast.LENGTH_SHORT).show();
                    loadDummyData(); // 실패 시 더미 데이터 로드
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
                                // ✅ 현재 최신목록이 아닌 경우 → RecyclerView를 덮어씌우지 않음
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
                            Toast.makeText(requireContext(), "데이터 파싱 오류. 더미 데이터를 로드합니다.", Toast.LENGTH_SHORT).show();
                            loadDummyData();
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "서버 응답 오류. 더미 데이터를 로드합니다.", Toast.LENGTH_SHORT).show();
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
        dummyItems.add(new Item("IT", "삼성전자6", "삼성증권", "https://example.com/sample1.pdf", "2024-02-15", "120", "반도체 시장의 향후 전망을 분석한 리포트입니다.반도체 시장의 향후 전망을 분석한 리포트입니다.반도체 시장의 향후 전망을 분석한 리포트입니다.반도체 시장의 향후 전망을 분석한 리포트입니다.",""));
        dummyItems.add(new Item("기술", "더미 리포트 2", "LG증권", "https://example.com/dummy2.pdf", "2025-02-02", "234", "더미 데이터 내용 2입니다.",""));
        dummyItems.add(new Item("산업", "더미 리포트 3", "한화증권", "https://example.com/dummy3.pdf", "2025-02-03", "345", "더미 데이터 내용 3입니다.",""));

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

