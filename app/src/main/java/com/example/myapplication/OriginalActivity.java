package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.view.TTSHelper;

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

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript, oriTitle, oriDate, oriName;
    private TextView oriCurrentTime, oriFullTime;
    private ImageButton playButton, skipBackButton, skipForwardButton, restartButton, pdfButton, backButton;
    private ProgressBar progressBar;
    private TTSHelper ttsHelper;
    private Handler progressHandler = new Handler();
    private boolean isPlaying = false;
    private String content, title;
    private MediaPlayer mediaPlayer;
    private final String API_KEY = BuildConfig.MY_KEY;



    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (ttsHelper.getMediaPlayer() != null && ttsHelper.getMediaPlayer().isPlaying()) {
                int currentPosition = ttsHelper.getMediaPlayer().getCurrentPosition();
                int totalDuration = ttsHelper.getMediaPlayer().getDuration();

                if (totalDuration > 0) {
                    int progress = (int) ((currentPosition / (float) totalDuration) * 100);
                    progressBar.setProgress(progress);
                }

                oriCurrentTime.setText(formatTime(currentPosition));
                oriFullTime.setText(formatTime(totalDuration));

                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        backButton = findViewById(R.id.ori_back);
        oriScript = findViewById(R.id.oriScript);
        oriTitle = findViewById(R.id.oriTitle);
        oriName = findViewById(R.id.oriName);
        oriDate = findViewById(R.id.ori_date);

        playButton = findViewById(R.id.ori_button_play);
        skipBackButton = findViewById(R.id.ori_prev);
        skipForwardButton = findViewById(R.id.ori_next);
        restartButton = findViewById(R.id.ori_restart);
        pdfButton = findViewById(R.id.ori_pdf);
        progressBar = findViewById(R.id.ori_progress_bar);
        oriCurrentTime = findViewById(R.id.ori_current_time);
        oriFullTime = findViewById(R.id.ori_full_time);

        ttsHelper = new TTSHelper(this);
        ttsHelper.setPlaybackCallback(this::onTrackCompleted);

        Intent intent = getIntent();
        title = intent.getStringExtra("Title");
        content = intent.getStringExtra("Content");

        Log.d("OriginalActivity", "Received Content: " + content);

        if (title != null) oriTitle.setText(title);
        if (content != null) {
            runOnUiThread(() -> oriScript.setText(content));
        } else {
            Toast.makeText(this, "스크립트 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        backButton.setOnClickListener(v -> finish());
        playButton.setOnClickListener(v -> togglePlayPause());
        restartButton.setOnClickListener(v -> restartTrack());
        skipBackButton.setOnClickListener(v -> skipBack());
        skipForwardButton.setOnClickListener(v -> skipForward());
        pdfButton.setOnClickListener(v -> openPdf(intent.getStringExtra("PDF_URL")));

        progressBar.setProgress(0);
    }

    private void togglePlayPause() {
        if (ttsHelper.getMediaPlayer() == null) {
            playTrack();
            return;
        }

        if (ttsHelper.getMediaPlayer().isPlaying()) {
            ttsHelper.getMediaPlayer().pause();
            isPlaying = false;
            playButton.setImageResource(R.drawable.button_play);
        } else {
            ttsHelper.getMediaPlayer().start();
            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);
            progressHandler.post(updateProgressRunnable);
        }
    }

    private void playTrack() {
        if (content == null || title == null) {
            Toast.makeText(this, "오디오 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        isPlaying = true;
        playButton.setImageResource(R.drawable.pause);

        ttsHelper.performTextToSpeech(content, title + ".mp3", playButton, new TTSHelper.OnPlaybackReadyListener() {
            @Override
            public void onReady() {
                if (ttsHelper.getMediaPlayer() != null) {
                    ttsHelper.getMediaPlayer().start();
                    progressHandler.post(updateProgressRunnable);
                }
            }
        });
    }

    private void restartTrack() {
        if (content != null && title != null) {
            ttsHelper.performTextToSpeech(content, title + ".mp3", playButton, new TTSHelper.OnPlaybackReadyListener() {
                @Override
                public void onReady() {
                    if (ttsHelper.getMediaPlayer() != null) {
                        ttsHelper.getMediaPlayer().start();
                        progressHandler.post(updateProgressRunnable);
                    }
                }
            });

            isPlaying = true;
            playButton.setImageResource(R.drawable.pause);
        }
    }

    private void onTrackCompleted() {
        runOnUiThread(() -> {
            playButton.setImageResource(R.drawable.button_play);
            progressBar.setProgress(0);
            oriCurrentTime.setText("00:00");
            isPlaying = false;
        });
    }

    private void skipBack() {
        if (ttsHelper.getMediaPlayer() != null) {
            int currentPosition = ttsHelper.getMediaPlayer().getCurrentPosition();
            ttsHelper.getMediaPlayer().seekTo(Math.max(currentPosition - 5000, 0));
        }
    }

    private void skipForward() {
        if (ttsHelper.getMediaPlayer() != null) {
            int currentPosition = ttsHelper.getMediaPlayer().getCurrentPosition();
            ttsHelper.getMediaPlayer().seekTo(Math.min(currentPosition + 5000, ttsHelper.getMediaPlayer().getDuration()));
        }
    }

    private void openPdf(String pdfUrl) {
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "PDF를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper.getMediaPlayer() != null) {
            ttsHelper.getMediaPlayer().release();
        }
        progressHandler.removeCallbacks(updateProgressRunnable);
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
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
            File tempAudioFile = File.createTempFile("tts_audio_" + oriTitle.getText().toString(), ".mp3", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(decodedAudio);
            fos.close();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            runOnUiThread(() -> playButton.setImageResource(R.drawable.pause));

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                runOnUiThread(() -> playButton.setImageResource(R.drawable.play));
                tempAudioFile.delete();
            });

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show());
        }
    }


    // 오디오 중지
    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            playButton.setImageResource(R.drawable.play);
        }
    }
}
