package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private ImageButton playButton;
    private int currentTrackIndex = 0; // 현재 재생 중인 트랙 인덱스
    private List<Item> itemList;

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

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.home_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 초기화
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(requireContext(), itemList);
        adapter.setItems(itemList);
        recyclerView.setAdapter(adapter);

        // TTSHelper 초기화
        ttsHelper = new TTSHelper(requireContext());

        // 서버에서 데이터 가져오기
        fetchDataFromServer();

        // 더미 데이터 추가 (서버가 없을 때만)
        addDummyData();

        // RecyclerView 아이템 클릭 이벤트
        adapter.setOnItemClickListener(item -> {
            currentTrackIndex = itemList.indexOf(item); // 클릭된 트랙의 인덱스 저장
            ttsHelper.performTextToSpeech(item.getContent(), BuildConfig.MY_KEY, playButton);
        });

        // Play 버튼 이벤트
        playButton.setOnClickListener(v -> ttsHelper.togglePlayPause(playButton));

        // 이전 트랙 버튼 이벤트
        prevButton.setOnClickListener(v -> {
            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                Item prevTrack = itemList.get(currentTrackIndex);
                ttsHelper.performTextToSpeech(prevTrack.getContent(), BuildConfig.MY_KEY, playButton);
            } else {
                Toast.makeText(requireContext(), "이전 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 다시 듣기 버튼 이벤트
        restartButton.setOnClickListener(v -> {
            Item currentTrack = itemList.get(currentTrackIndex);
            ttsHelper.performTextToSpeech(currentTrack.getContent(), BuildConfig.MY_KEY, playButton);
        });

        // 다음 트랙 버튼 이벤트
        nextButton.setOnClickListener(v -> {
            if (currentTrackIndex < itemList.size() - 1) {
                currentTrackIndex++;
                Item nextTrack = itemList.get(currentTrackIndex);
                ttsHelper.performTextToSpeech(nextTrack.getContent(), BuildConfig.MY_KEY, playButton);
            } else {
                Toast.makeText(requireContext(), "다음 트랙이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // 서버에서 데이터 가져오기
    private void fetchDataFromServer() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://your-server-url/api/reports"; // 서버 API URL

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                );
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
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "서버 응답이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // JSON 데이터를 파싱하고 RecyclerView에 설정
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

    // 더미 데이터 추가
    private void addDummyData() {
        itemList.add(new Item("경제", "더미 데이터 1", "ABC증권", "https://example.com/dummy1.pdf", "2025-01-13", "10", "이것은 더미 데이터 1입니다."));
        itemList.add(new Item("기술", "더미 데이터 2", "XYZ증권", "https://example.com/dummy2.pdf", "2025-01-14", "20", "이것은 더미 데이터 2입니다."));
        itemList.add(new Item("사회", "더미 데이터 3", "LMN증권", "https://example.com/dummy3.pdf", "2025-01-15", "30", "이것은 더미 데이터 3입니다."));
        adapter.notifyDataSetChanged();
    }
}
