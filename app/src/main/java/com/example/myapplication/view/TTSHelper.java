package com.example.myapplication.view;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;

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

public class TTSHelper {
    private final Context context;
    private final String API_KEY =  BuildConfig.MY_KEY;

    public TTSHelper(Context context) {
        this.context = context;
    }

    public interface OnAudioFileReadyListener {
        void onAudioFileReady(File audioFile);
    }

    public void performTextToSpeech(String text, String fileName, OnAudioFileReadyListener listener) {
        File audioFile = new File(context.getCacheDir(), fileName);

        // 파일이 이미 존재하면 변환 없이 바로 실행
        if (audioFile.exists()) {
            Log.d("TTSHelper", "이미 저장된 오디오 파일 사용: " + audioFile.getAbsolutePath());
            listener.onAudioFileReady(audioFile);
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
                e.printStackTrace();
                showToast("TTS 요청 실패");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String audioContentEncoded = jsonObject.getString("audioContent");

                        // 파일을 저장한 후 getAbsolutePath() 적용
                        saveAudioFile(audioContentEncoded, audioFile, listener);
                        Log.d("TTSHelper", "오디오 파일 저장됨: " + audioFile.getAbsolutePath());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast("TTS 응답 파싱 실패");
                    }
                } else {
                    showToast("TTS 응답 실패");
                }
            }
        });
    }


    private void saveAudioFile(String base64Audio, File audioFile, OnAudioFileReadyListener listener) {
        byte[] decodedAudio = Base64.decode(base64Audio, Base64.DEFAULT);
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(decodedAudio);
            Log.d("TTSHelper", "오디오 파일 저장 완료: " + audioFile.getAbsolutePath());

            // 변환 완료 후 listener 호출
            listener.onAudioFileReady(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            showToast("오디오 파일 저장 실패");
        }
    }


    private void playAudio(File audioFile) {
        Intent intent = new Intent(context, AudioService.class);
        intent.putExtra("audioFilePath", audioFile.getAbsolutePath());
        context.startService(intent);
        Log.d("TTSHelper", "AudioService에서 재생 요청: " + audioFile.getAbsolutePath());
    }

    private void playDefaultAudio() {
        File defaultAudio = new File(context.getCacheDir(), "default.mp3");

        // 기본 오디오 파일이 있으면 재생
        if (defaultAudio.exists()) {
            playAudio(defaultAudio);
        } else {
            showToast("기본 오디오 파일이 없습니다.");
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
