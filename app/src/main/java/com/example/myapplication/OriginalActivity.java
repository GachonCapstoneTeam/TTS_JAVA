package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.view.TTSHelper;

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript, oriTitle, oriDate, oriName;
    private TextView oriCurrentTime, oriFullTime;
    private ImageButton playButton, skipBackButton, skipForwardButton, restartButton, pdfButton, backButton;
    private ProgressBar progressBar;
    private TTSHelper ttsHelper;
    private Handler progressHandler = new Handler();
    private boolean isPlaying = false;
    private String content, title;

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
}
