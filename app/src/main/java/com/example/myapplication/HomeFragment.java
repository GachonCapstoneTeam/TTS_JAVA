package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
import com.example.myapplication.view.HomeAudioService;
import com.example.myapplication.view.OriginalAudioService;
import com.example.myapplication.view.TTSHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
    private ImageButton playButton, prevButton, nextButton, restartButton, fullScreenButton;
    private ProgressBar progressBar;
    private TextView currentTimeText, fullTimeText;
    private List<Item> itemList;
    private int currentTrackIndex = 0;
    private MediaPlayer mediaPlayer;
    private HomeAudioService homeAudioService;
    private OriginalAudioService originalAudioService;
    private boolean isHomeServiceBound = false;
    private boolean isOriginalServiceBound = false;
    private Handler progressHandler = new Handler();
    private Context mContext;

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
        restartButton = view.findViewById(R.id.restart);
        fullScreenButton = view.findViewById(R.id.full_screen);
        progressBar = view.findViewById(R.id.progress_bar);
        currentTimeText = view.findViewById(R.id.current_time);
        fullTimeText = view.findViewById(R.id.full_time);

        recyclerView = view.findViewById(R.id.home_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(requireContext(), itemList);
        recyclerView.setAdapter(adapter);

        ttsHelper = new TTSHelper(requireContext());

        // 데이터 가져오기
        fetchDataFromServer();

        // 아이템 클릭 시 오디오 재생
        adapter.setOnItemClickListener(item -> {
            currentTrackIndex = itemList.indexOf(item);
            playTrackAtIndex(currentTrackIndex, true);
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

        restartButton.setOnClickListener(v -> playTrackAtIndex(currentTrackIndex, true));

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
                intent.putExtra("track_index", currentTrackIndex);
                intent.putExtra("AudioFilePath", getAudioFilePath(currentItem.getTitle()));

                if (originalAudioService != null) {
                    intent.putExtra("AudioPosition", originalAudioService.getCurrentPosition());
                    intent.putExtra("IsPlaying", originalAudioService.isPlaying());
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
                TextView scriptView = requireView().findViewById(R.id.home_display_script);
                if (titleView != null && scriptView != null) {
                    titleView.setText(track.getTitle());
                    scriptView.setText(track.getContent());
                }

                progressBar.setProgress(0);
                currentTimeText.setText("00:00");
                fullTimeText.setText("00:00");

                ttsHelper.performTextToSpeech(track.getContent(), track.getTitle() + ".mp3", audioFile -> {
                    if (audioFile.exists()) {
                        Log.d("HomeFragment", "TTS 변환 완료 → 오디오 파일 저장됨: " + audioFile.getAbsolutePath());

                        requireActivity().runOnUiThread(() -> {
                            if (useHomeService && homeAudioService != null) {
                                Log.d("HomeFragment", "HomeAudioService에서 오디오 준비");
                                homeAudioService.prepareAudio(audioFile.getAbsolutePath(), 0, true);
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
        if (homeAudioService != null && homeAudioService.isPlaying()) {
            homeAudioService.pauseAudio();
            playButton.setImageResource(R.drawable.button_play);
        } else if (originalAudioService != null && originalAudioService.isPlaying()) {
            originalAudioService.pauseAudio();
            playButton.setImageResource(R.drawable.button_play);
        } else {
            homeAudioService.resumeAudio();
            playButton.setImageResource(R.drawable.button_pause);
        }
    }

    private final ServiceConnection homeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HomeAudioService.AudioBinder binder = (HomeAudioService.AudioBinder) service;
            homeAudioService = binder.getService();
            isHomeServiceBound = true;

            homeAudioService.setProgressUpdateListener((currentPosition, duration) ->
                    requireActivity().runOnUiThread(() -> updateProgressBar(currentPosition, duration))
            );

            homeAudioService.setNextTrackListener(() ->
                    requireActivity().runOnUiThread(() -> playNextTrack())
            );

            homeAudioService.startProgressUpdates();
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            isHomeServiceBound = false;
        }
    };

    private final ServiceConnection originalServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OriginalAudioService.AudioBinder binder = (OriginalAudioService.AudioBinder) service;
            originalAudioService = binder.getService();
            isOriginalServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isOriginalServiceBound = false;
        }
    };

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
            Intent serviceIntent = new Intent(mContext, HomeAudioService.class);

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
    }


    @Override
    public void onStop() {
        super.onStop();
        if (isHomeServiceBound) mContext.unbindService(homeServiceConnection);
        if (isOriginalServiceBound) mContext.unbindService(originalServiceConnection);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // 프래그먼트가 백그라운드로 가면 재생 중지
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

    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://40.82.148.190:8000/textload/home";

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

    private void updateProgressBar(int currentPosition, int duration) {
        if (duration > 0) {
            int progress = (int) ((currentPosition / (float) duration) * 100);
            progressBar.setProgress(progress);
        }

        currentTimeText.setText(formatTime(currentPosition));
        fullTimeText.setText(formatTime(duration));
    }

    private void loadDummyData() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("IT", "삼성전자5", "삼성증권", "https://example.com/sample1.pdf", "2024-02-15", "120", "반도체 시장의 향후 전망을 분석한 리포트입니다."));
        dummyItems.add(new Item("기술", "더미 리포트 2", "LG증권", "https://example.com/dummy2.pdf", "2025-02-02", "234", "더미 데이터 내용 2입니다."));
        dummyItems.add(new Item("산업", "더미 리포트 3", "한화증권", "https://example.com/dummy3.pdf", "2025-02-03", "345", "더미 데이터 내용 3입니다."));

        requireActivity().runOnUiThread(() -> {
            itemList.clear();
            itemList.addAll(dummyItems);
            adapter.notifyDataSetChanged();
        });
    }


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
