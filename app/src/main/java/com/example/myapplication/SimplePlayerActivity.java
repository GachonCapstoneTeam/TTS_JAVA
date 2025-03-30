package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class SimplePlayerActivity extends AppCompatActivity {

    private TextView smScript, smTitle, smName, smDate;
    private TextView smCurrentTime, smFullTime;
    private ImageButton playButton, prevButton, nextButton, pdfButton, backButton;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable progressRunnable;
    private boolean isPlaying = false;
    private String audioFilePath, title, content, category, date, pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_player);

        smScript = findViewById(R.id.smScript);
        smTitle = findViewById(R.id.smTitle);
        smName = findViewById(R.id.smName);
        smDate = findViewById(R.id.sm_date);
        smCurrentTime = findViewById(R.id.sm_current_time);
        smFullTime = findViewById(R.id.sm_full_time);

        playButton = findViewById(R.id.sm_button_play);
        prevButton = findViewById(R.id.sm_prev);
        nextButton = findViewById(R.id.sm_next);
        pdfButton = findViewById(R.id.sm_pdf);
        backButton = findViewById(R.id.sm_back);
        seekBar = findViewById(R.id.sm_progress_bar);

        Intent intent = getIntent();
        audioFilePath = intent.getStringExtra("AudioFilePath");
        title = intent.getStringExtra("Title");
        content = intent.getStringExtra("Content");
        category = intent.getStringExtra("Category");
        date = intent.getStringExtra("Date");
        pdfUrl = intent.getStringExtra("PDF_URL");

        smTitle.setText(title);
        smScript.setText(content);
        smName.setText(category);
        smDate.setText(date);

        if (audioFilePath == null || audioFilePath.isEmpty()) {
            Toast.makeText(this, "오디오 파일 경로가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initMediaPlayer();

        playButton.setOnClickListener(v -> togglePlayPause());
        prevButton.setOnClickListener(v -> skip(-5000));
        nextButton.setOnClickListener(v -> skip(5000));
        pdfButton.setOnClickListener(v -> openPdf(pdfUrl));
        backButton.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int newPosition = (int) ((progress / 100.0) * mediaPlayer.getDuration());
                    smCurrentTime.setText(formatTime(newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    int newPosition = (int) ((seekBar.getProgress() / 100.0) * mediaPlayer.getDuration());
                    mediaPlayer.seekTo(newPosition);
                }
            }
        });
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            smFullTime.setText(formatTime(mediaPlayer.getDuration()));

            mediaPlayer.setOnCompletionListener(mp -> {
                stopProgressUpdates();
                isPlaying = false;
                playButton.setImageResource(R.drawable.button_play);
            });

        } catch (IOException e) {
            Log.e("SimplePlayer", "오디오 로드 실패: " + e.getMessage());
            Toast.makeText(this, "오디오 파일을 재생할 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressUpdates();
            playButton.setImageResource(R.drawable.button_play);
        } else {
            mediaPlayer.start();
            startProgressUpdates();
            playButton.setImageResource(R.drawable.button_pause);
        }
    }

    private void restartTrack() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
        startProgressUpdates();
        playButton.setImageResource(R.drawable.button_pause);
    }

    private void skip(int ms) {
        int pos = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();
        mediaPlayer.seekTo(Math.min(Math.max(pos + ms, 0), duration));
    }

    private void startProgressUpdates() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int current = mediaPlayer.getCurrentPosition();
                    int total = mediaPlayer.getDuration();
                    int progress = (int) ((current / (float) total) * 100);
                    seekBar.setProgress(progress);
                    smCurrentTime.setText(formatTime(current));
                    smFullTime.setText(formatTime(total));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable);
    }

    private void openPdf(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setDataAndType(Uri.parse(url), "application/pdf");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "PDF를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopProgressUpdates();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}