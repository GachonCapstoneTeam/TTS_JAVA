package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ReportAdapter;
import com.example.myapplication.entity.Item;

import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;



import android.os.Handler;
import android.os.Looper;

public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;

    private Handler handler; // 핸들러 선언
    private Runnable dataFetchRunnable; // 반복 실행할 작업 선언
    private static final int FETCH_INTERVAL = 100000; // 데이터 갱신 간격 (밀리초)

    private List<Item> parseJson(String jsonResponse) {
        Gson gson = new Gson();
        List<Item> items = new ArrayList<>();

        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray contentsArray = jsonObject.getAsJsonArray("contents");
            for (JsonElement element : contentsArray) {
                Item item = gson.fromJson(element, Item.class);
                items.add(item);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return items;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = view.findViewById(R.id.reportRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportAdapter = new ReportAdapter(getContext());
        recyclerView.setAdapter(reportAdapter);

        handler = new Handler(Looper.getMainLooper());
        startDataFetchLoop();

        return view;
    }

    private void fetchItemsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/textload/content/") // API 엔드포인트
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    List<Item> itemList = parseJson(jsonResponse);
                    getActivity().runOnUiThread(() -> reportAdapter.setItems(itemList));
                }
            }
        });
    }

    private void startDataFetchLoop() {
        dataFetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchItemsFromServer(); // 데이터 가져오기
                handler.postDelayed(this, FETCH_INTERVAL); // 일정 시간 후 다시 실행
            }
        };
        handler.post(dataFetchRunnable); // 즉시 실행
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopDataFetchLoop(); // Fragment 종료 시 루프 중단
    }

    private void stopDataFetchLoop() {
        if (handler != null && dataFetchRunnable != null) {
            handler.removeCallbacks(dataFetchRunnable); // 반복 작업 제거
        }
    }
}