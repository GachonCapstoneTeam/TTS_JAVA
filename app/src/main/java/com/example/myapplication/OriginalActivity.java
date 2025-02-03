package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.io.IOException;

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript, oriTitle, oriDate, oriName;
    private TextView oriCurrentTime, oriFullTime;
    private ImageButton playButton, skipBackButton, skipForwardButton, restartButton, nextButton, pdfButton, backButton;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        // View 초기화
        backButton = findViewById(R.id.ori_back);
        oriScript = findViewById(R.id.oriScript);
        oriTitle = findViewById(R.id.oriTitle);
        oriName = findViewById(R.id.oriName);
        oriDate = findViewById(R.id.ori_date);

        // Audio 관련 View 초기화
        playButton = findViewById(R.id.ori_button_play);
        skipBackButton = findViewById(R.id.ori_prev);
        skipForwardButton = findViewById(R.id.ori_next);
        restartButton = findViewById(R.id.ori_restart);
        pdfButton = findViewById(R.id.ori_pdf);
        progressBar = findViewById(R.id.ori_progress_bar);
        oriCurrentTime = findViewById(R.id.ori_current_time);
        oriFullTime = findViewById(R.id.ori_full_time);

        // Intent 데이터 가져오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        String content = intent.getStringExtra("Content");
        String category = intent.getStringExtra("Category");
        String date = intent.getStringExtra("Date");
        String pdfUrl = intent.getStringExtra("PDF_URL");

        if (title != null) oriTitle.setText(title);
        if (content != null) oriScript.setText(content);
        if (category != null) oriName.setText(category);
        if (date != null) oriDate.setText(date);

        // 버튼 이벤트 설정
        backButton.setOnClickListener(v -> finish());
        playButton.setOnClickListener(v -> togglePlayPause());
        restartButton.setOnClickListener(v -> restartAudio());
        skipBackButton.setOnClickListener(v -> skipBack());
        skipForwardButton.setOnClickListener(v -> skipForward());
        pdfButton.setOnClickListener(v -> openPdf(pdfUrl));

        // ProgressBar 초기화
        progressBar.setProgress(0);
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                playButton.setBackgroundResource(R.drawable.button_play); // play 아이콘으로 변경
            } else {
                mediaPlayer.start();
                isPlaying = true;
                playButton.setBackgroundResource(R.drawable.button_pause); // pause 아이콘으로 변경
            }
        } else {
            Toast.makeText(this, "재생할 오디오가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void restartAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            isPlaying = true;
            playButton.setBackgroundResource(R.drawable.button_pause);
        }
    }

    private void skipBack() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int skipTime = 5000; // 5초 이전으로 이동
            mediaPlayer.seekTo(Math.max(currentPosition - skipTime, 0));
        }
    }

    private void skipForward() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int skipTime = 5000; // 5초 이후로 이동
            mediaPlayer.seekTo(Math.min(currentPosition + skipTime, mediaPlayer.getDuration()));
        }
    }

    private void openPdf(String pdfUrl) {
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "PDF를 열 수 있는 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "PDF URL이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
