package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ItemAdapter;
import com.example.myapplication.entity.Item;
import com.example.myapplication.view.TTSHelper;
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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TTSHelper ttsHelper;
    private ImageButton playButton, fullScreenButton;
    private int currentTrackIndex = 0; // 현재 재생 중인 트랙 인덱스
    private List<Item> itemList;
    private ProgressBar progressBar;
    private TextView currentTimeText, fullTimeText;
    private Handler progressHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 버튼 초기화
        ImageButton restartButton = view.findViewById(R.id.restart);
        ImageButton prevButton = view.findViewById(R.id.prev);
        playButton = view.findViewById(R.id.button_play);
        ImageButton nextButton = view.findViewById(R.id.next);
        ImageButton fullScreenButton = view.findViewById(R.id.full_screen);

        //재생바 초기화
        progressBar = view.findViewById(R.id.progress_bar);
        currentTimeText = view.findViewById(R.id.current_time);
        fullTimeText = view.findViewById(R.id.full_time);

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.home_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 초기화
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(requireContext(), itemList);
        recyclerView.setAdapter(adapter);

        // TTSHelper 초기화 및 PlaybackCallback 설정
        ttsHelper = new TTSHelper(requireContext());
        ttsHelper.setPlaybackCallback(() -> requireActivity().runOnUiThread(this::playNextTrack));

        // 서버에서 데이터 가져오기
        fetchDataFromServer();

        // RecyclerView 아이템 클릭 이벤트
        adapter.setOnItemClickListener(item -> {
            currentTrackIndex = itemList.indexOf(item); // 클릭된 트랙의 인덱스 저장
            playTrackAtIndex(currentTrackIndex);
        });

        // Play 버튼 이벤트
        playButton.setOnClickListener(v -> ttsHelper.togglePlayPause(playButton));

        // 이전 트랙 버튼 이벤트
        prevButton.setOnClickListener(v -> {
            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                playTrackAtIndex(currentTrackIndex);
            } else {
                Toast.makeText(requireContext(), "이전 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 다시 듣기 버튼 이벤트
        restartButton.setOnClickListener(v -> playTrackAtIndex(currentTrackIndex));

        // 다음 트랙 버튼 이벤트
        nextButton.setOnClickListener(v -> {
            if (currentTrackIndex < itemList.size() - 1) {
                currentTrackIndex++;
                playTrackAtIndex(currentTrackIndex);
            } else {
                Toast.makeText(requireContext(), "다음 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        fullScreenButton.setOnClickListener(v -> {
            if (currentTrackIndex >= 0 && currentTrackIndex < itemList.size()) {
                Item currentItem = itemList.get(currentTrackIndex);

                Intent intent = new Intent(getContext(), OriginalActivity.class);
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

        return view;
    }

    private void playTrackAtIndex(int index) {
        if (index >= 0 && index < itemList.size()) {
            Item track = itemList.get(index);

            requireActivity().runOnUiThread(() -> {
                TextView titleView = requireView().findViewById(R.id.home_display_title);
                TextView scriptView = requireView().findViewById(R.id.home_display_script);

                if (titleView != null && scriptView != null) {
                    titleView.setText(track.getTitle());
                    scriptView.setText(track.getContent());
                }

                // ProgressBar 초기화
                progressBar.setProgress(0);
                currentTimeText.setText("00:00");
                fullTimeText.setText("00:00");

                // 🔹 오디오가 준비된 후 ProgressBar 업데이트 실행
                ttsHelper.setPlaybackCallback(() -> {
                    requireActivity().runOnUiThread(() -> {
                        progressHandler.post(updateProgressRunnable);
                    });
                });

                playButton.setImageResource(R.drawable.pause);
            });

            ttsHelper.performTextToSpeech(track.getContent(), track.getTitle() + ".mp3", playButton);
        }
    }


    private void playNextTrack() {
        if (currentTrackIndex < itemList.size() - 1) {
            currentTrackIndex++;
            playTrackAtIndex(currentTrackIndex);
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "마지막 트랙입니다.", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // 서버에서 데이터 가져오기
    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://40.82.148.190:8000"; // 서버 API URL

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
                        parseAndSetData(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
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

    private void loadDummyData() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("경제", "더미 리포트 1", "삼성증권", "https://example.com/dummy1.pdf", "2025-02-01", "123", "더미 데이터 내용 1입니다."));
        dummyItems.add(new Item("기술", "더미 리포트 2", "LG증권", "https://example.com/dummy2.pdf", "2025-02-02", "234", "더미 데이터 내용 2입니다."));
        dummyItems.add(new Item("산업", "더미 리포트 3", "한화증권", "https://example.com/dummy3.pdf", "2025-02-03", "345", "더미 데이터 내용 3입니다."));

        requireActivity().runOnUiThread(() -> {
            itemList.clear();
            itemList.addAll(dummyItems);
            adapter.notifyDataSetChanged();
        });
    }


    private Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (ttsHelper.getMediaPlayer() != null) {
                MediaPlayer mediaPlayer = ttsHelper.getMediaPlayer();

                if (mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();

                    if (totalDuration > 0) {
                        int progress = (int) ((currentPosition / (float) totalDuration) * 100);
                        progressBar.setProgress(progress);
                    }

                    // 시간 표시 업데이트
                    currentTimeText.setText(formatTime(currentPosition));
                    fullTimeText.setText(formatTime(totalDuration));

                    // 1초마다 갱신
                    progressHandler.postDelayed(this, 1000);
                }
            }
        }
    };


    //시간 변환 함수
    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void parseAndSetData(String jsonResponse) throws JSONException {
        List<Item> fetchedItems = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Item item = new Item(
                    jsonObject.getString("category"),
                    jsonObject.getString("title"),
                    jsonObject.getString("stockName"),
                    jsonObject.getString("pdfUrl"),
                    jsonObject.getString("date"),
                    jsonObject.getString("views"),
                    jsonObject.getString("content")
            );
            fetchedItems.add(item);
        }

        requireActivity().runOnUiThread(() -> {
            itemList.clear();
            itemList.addAll(fetchedItems);
            adapter.notifyDataSetChanged();
        });
    }

}

