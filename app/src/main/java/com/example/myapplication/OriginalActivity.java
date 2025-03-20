package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.view.HomeAudioService;

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript, oriTitle, oriDate, oriName;
    private TextView oriCurrentTime, oriFullTime;
    private ImageButton playButton, skipBackButton, skipForwardButton, restartButton, pdfButton, backButton;
    private ProgressBar progressBar;
    private Handler progressHandler = new Handler();
    private boolean isPlaying = false;
    private int audioPosition = 0;
    private int trackIndex;
    private String title, content, category, date, pdfUrl;
    private HomeAudioService homeAudioService;
    private String audioFilePath;
    private boolean isServiceBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HomeAudioService.AudioBinder binder = (HomeAudioService.AudioBinder) service;
            homeAudioService = binder.getService();
            isServiceBound = true;

            homeAudioService.setProgressUpdateListener((currentPosition, duration) -> {
                runOnUiThread(() -> updateProgressBar(currentPosition, duration));
            });

            // 기존 재생 상태 유지
            if (homeAudioService.isPrepared()) {
                homeAudioService.seekTo(audioPosition);
                if (isPlaying) {
                    homeAudioService.resumeAudio();
                    progressHandler.post(updateProgressRunnable);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (homeAudioService != null && homeAudioService.isPlaying()) {
                int currentPosition = homeAudioService.getCurrentPosition();
                int totalDuration = homeAudioService.getDuration();

                Log.d("HomeAudioService", "Progress 업데이트: " + currentPosition + " / " + totalDuration);

                if (totalDuration > 0) {
                    int progress = (int) ((currentPosition / (float) totalDuration) * 100);
                    progressBar.setProgress(progress);
                } else {
                    Log.e("HomeAudioService", "progressUpdateListener가 null입니다!");
                }

                oriCurrentTime.setText(formatTime(currentPosition));
                oriFullTime.setText(formatTime(totalDuration));

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        this.audioFilePath = getIntent().getStringExtra("AudioFilePath");

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            Toast.makeText(this, "오디오 파일 경로가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // ForegroundService 바인딩
        Intent serviceIntent = new Intent(this, HomeAudioService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        // Intent 데이터 받기
        Intent intent = getIntent();
        trackIndex = intent.getIntExtra("track_index", 0);
        audioPosition = intent.getIntExtra("AudioPosition", 0);
        isPlaying = intent.getBooleanExtra("IsPlaying", false);

        title = intent.getStringExtra("Title");
        content = intent.getStringExtra("Content");
        category = intent.getStringExtra("Category");
        date = intent.getStringExtra("Date");
        pdfUrl = intent.getStringExtra("PDF_URL");

        oriTitle.setText(title);
        oriScript.setText(content);
        oriName.setText(category);
        oriDate.setText(date);

        backButton.setOnClickListener(v -> finishWithResult());
        playButton.setOnClickListener(v -> togglePlayPause());
        restartButton.setOnClickListener(v -> restartTrack());
        skipBackButton.setOnClickListener(v -> skipBack());
        skipForwardButton.setOnClickListener(v -> skipForward());
        pdfButton.setOnClickListener(v -> openPdf(pdfUrl));
    }

    private void togglePlayPause() {
        if (homeAudioService == null) return;

        if (homeAudioService.isPlaying()) {
            homeAudioService.pauseAudio();
            isPlaying = false;
            playButton.setImageResource(R.drawable.button_play);
        } else {
            homeAudioService.resumeAudio();
            isPlaying = true;
            playButton.setImageResource(R.drawable.button_pause);
            progressHandler.post(updateProgressRunnable);
        }
    }

    private void restartTrack() {
        if (homeAudioService != null) {
            homeAudioService.seekTo(0);
            homeAudioService.resumeAudio();
            progressHandler.post(updateProgressRunnable);
            isPlaying = true;
            playButton.setImageResource(R.drawable.button_pause);
        }
    }

    private void skipBack() {
        if (homeAudioService != null) {
            int currentPosition = homeAudioService.getCurrentPosition();
            homeAudioService.seekTo(Math.max(currentPosition - 5000, 0));
        }
    }

    private void skipForward() {
        if (homeAudioService != null) {
            int currentPosition = homeAudioService.getCurrentPosition();
            homeAudioService.seekTo(Math.min(currentPosition + 5000, homeAudioService.getDuration()));
        }
    }

    private void openPdf(String pdfUrl) {
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            try {
                startActivity(pdfIntent);
            } catch (Exception e) {
                Toast.makeText(this, "PDF를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("track_index", trackIndex);
        resultIntent.putExtra("AudioPosition", homeAudioService.getCurrentPosition());
        resultIntent.putExtra("IsPlaying", homeAudioService.isPlaying());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateProgressBar(int currentPosition, int duration) {
        if (duration > 0) {
            int progress = (int) ((currentPosition / (float) duration) * 100);
            progressBar.setProgress(progress);
        }

        oriCurrentTime.setText(formatTime(currentPosition));
        oriFullTime.setText(formatTime(duration));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        progressHandler.removeCallbacks(updateProgressRunnable);
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
