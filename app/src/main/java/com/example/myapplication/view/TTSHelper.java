package com.example.myapplication.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;

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
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private PlaybackCallback playbackCallback;
    private final String API_KEY = BuildConfig.MY_KEY;

    // 인터페이스 정의
    public interface PlaybackCallback {
        void onTrackCompleted(); // 트랙 완료 시 호출
    }

    // PlaybackCallback 설정 메서드
    public void setPlaybackCallback(PlaybackCallback callback) {
        this.playbackCallback = callback;
    }

    public TTSHelper(Context context) {
        this.context = context;
    }

    public void performTextToSpeech(String text, String fileName, ImageButton playButton) {
        File audioFile = new File(context.getCacheDir(), fileName);
        if (audioFile.exists()) {
            playAudio(audioFile, playButton);
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
                Toast.makeText(context, "TTS 요청 실패", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String audioContentEncoded = jsonObject.getString("audioContent");
                        saveAudioFile(audioContentEncoded, fileName, playButton);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "TTS 응답 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveAudioFile(String base64Audio, String fileName, ImageButton playButton) {
        byte[] decodedAudio = Base64.decode(base64Audio, Base64.DEFAULT);
        File audioFile = new File(context.getCacheDir(), fileName);

        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(decodedAudio);
            playAudio(audioFile, playButton);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "오디오 파일 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio(File audioFile, ImageButton playButton) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playButton.setImageResource(R.drawable.play);

                // PlaybackCallback 호출
                if (playbackCallback != null) {
                    playbackCallback.onTrackCompleted();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "오디오 재생 실패", Toast.LENGTH_SHORT).show();
        }
    }

    public void togglePlayPause(ImageButton playButton) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                playButton.setImageResource(R.drawable.play);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                playButton.setImageResource(R.drawable.pause);
            }
        }
    }
}
