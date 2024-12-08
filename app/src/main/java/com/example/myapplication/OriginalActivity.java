package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.media.MediaPlayer;

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript;
    private ImageButton skipBack, stop, play, skipForward, pdf;
    private Button handleButton, backButton;
    private View bottomMenuContainer;
    private boolean isMenuVisible = true; // 메뉴의 가시성 상태
    private final String API_KEY = BuildConfig.MY_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        // View 초기화
        backButton = findViewById(R.id.backbutton_ori);
        oriScript = findViewById(R.id.oriScript);
        handleButton = findViewById(R.id.handle_shape);
        skipBack = findViewById(R.id.skipback);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        skipForward = findViewById(R.id.skipforward);
        pdf = findViewById(R.id.pdf);
        bottomMenuContainer = findViewById(R.id.bottom_menu_container);

        // 서버에서 텍스트 가져오기
        fetchTextFromServer();

        // 둥근 손잡이 버튼 클릭 이벤트
        handleButton.setOnClickListener(v -> toggleBottomMenu());

        // 버튼 동작 처리
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        skipBack.setOnClickListener(v -> Toast.makeText(this, "Skip Back", Toast.LENGTH_SHORT).show());
        stop.setOnClickListener(v -> Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show());
        play.setOnClickListener(v -> performTextToSpeech());
        skipForward.setOnClickListener(v -> Toast.makeText(this, "Skip Forward", Toast.LENGTH_SHORT).show());
        pdf.setOnClickListener(v -> Toast.makeText(this, "Open PDF", Toast.LENGTH_SHORT).show());
    }

    // 메뉴 숨기기/보이기
    private void toggleBottomMenu() {
        if (isMenuVisible) {
            bottomMenuContainer.animate()
                    .translationY(bottomMenuContainer.getHeight() - 80)
                    .setDuration(300)
                    .start();
        } else {
            bottomMenuContainer.setVisibility(View.VISIBLE);
            bottomMenuContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
        isMenuVisible = !isMenuVisible;
    }

    // 서버에서 텍스트 가져오기
    private void fetchTextFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/textload/content/") // Django 서버 URL
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Failed to fetch text", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String originalText = jsonObject.getString("originaltext");
                        Log.d("ServerResponse", responseBody);

                        // UI 업데이트
                        runOnUiThread(() -> oriScript.setText(originalText));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Error fetching text", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // TTS 기능 수행
    private void performTextToSpeech() {
        String text = oriScript.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject data = new JSONObject();
        try {
            data.put("input", new JSONObject().put("text", text));
            data.put("voice", new JSONObject().put("languageCode", "ko-KR"));
            data.put("audioConfig", new JSONObject().put("audioEncoding", "MP3"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(data.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "TTS Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String audioContentEncoded = jsonObject.getString("audioContent");
                        playAudio(audioContentEncoded);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "TTS Request failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 오디오 재생
    private void playAudio(String base64Audio) {
        byte[] decodedAudio = Base64.decode(base64Audio, Base64.DEFAULT);

        try {
            File tempAudioFile = File.createTempFile("tts_audio", ".mp3", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(decodedAudio);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Playing Audio", Toast.LENGTH_SHORT).show());

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                tempAudioFile.delete();
            });

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show());
        }
    }
}