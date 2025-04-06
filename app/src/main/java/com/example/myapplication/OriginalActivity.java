package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.entity.Item;
import com.example.myapplication.view.AudioService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.List;

public class OriginalActivity extends AppCompatActivity {

    private TextView oriScript, oriTitle, oriDate, oriName;
    private TextView oriCurrentTime, oriFullTime;
    private ImageButton playButton, skipBackButton, skipForwardButton, lastButton, nextButton, pdfButton, backButton;
    private SeekBar seekBar;
    private Handler progressHandler = new Handler();
    private boolean isPlaying = false;
    private int audioPosition = 0;
    private int trackIndex;
    private String title, content, category, date, pdfUrl;
    private AudioService audioService;
    private String audioFilePath;
    private boolean isServiceBound = false;
    private List<Item> itemList;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();
            isServiceBound = true;

            int currentPosition = audioService.getCurrentPosition();
            int totalDuration = audioService.getDuration();

            runOnUiThread(() -> updateProgressBar(currentPosition, totalDuration));

            audioService.playAudio(audioFilePath, trackIndex, audioPosition);

            audioService.setProgressUpdateListener((currentPosition2, duration) -> {
                runOnUiThread(() -> updateProgressBar(currentPosition2, duration));
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (audioService != null) {
                int currentPosition = audioService.getCurrentPosition();
                int totalDuration = audioService.getDuration();

                Log.d("HomeAudioService", "Progress 업데이트: " + currentPosition + " / " + totalDuration);

                if (totalDuration > 0) {
                    int progress = (int) ((currentPosition / (float) totalDuration) * 100);
                    seekBar.setProgress(progress);
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


        // 리스트 받기
        String jsonList = getIntent().getStringExtra("ItemList");
        itemList = new Gson().fromJson(jsonList, new TypeToken<List<Item>>(){}.getType());

        // 인덱스 받기
        trackIndex = getIntent().getIntExtra("track_index", 0);
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
        lastButton = findViewById(R.id.ori_last_track);
        nextButton = findViewById(R.id.ori_next_track);
        pdfButton = findViewById(R.id.ori_pdf);
        seekBar = findViewById(R.id.ori_progress_bar);
        oriCurrentTime = findViewById(R.id.ori_current_time);
        oriFullTime = findViewById(R.id.ori_full_time);

        // ForegroundService 바인딩
        Intent serviceIntent = new Intent(this, AudioService.class);
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
        skipBackButton.setOnClickListener(v -> skipBack());
        skipForwardButton.setOnClickListener(v -> skipForward());
        lastButton.setOnClickListener(v -> skipLast());
        nextButton.setOnClickListener(v -> skipNext());
        pdfButton.setOnClickListener(v -> openPdf(pdfUrl));

        if (isPlaying) {
            playButton.setImageResource(R.drawable.button_pause);
        } else {
            playButton.setImageResource(R.drawable.button_play);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && audioService != null) {
                    int newPosition = (int) ((progress / 100.0) * audioService.getDuration());
                    oriCurrentTime.setText(formatTime(newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용자가 SeekBar 조작 디버깅 가능
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (audioService != null) {
                    int newPosition = (int) ((seekBar.getProgress() / 100.0) * audioService.getDuration());
                    audioService.seekTo(newPosition);
                    updateProgressBar(newPosition, audioService.getDuration());
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (audioService != null) {
            int currentPosition = audioService.getCurrentPosition();
            int totalDuration = audioService.getDuration();

            updateProgressBar(currentPosition, totalDuration);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioService != null) {
            audioService.setProgressUpdateListener((currentPosition, duration) -> {
                runOnUiThread(() -> updateProgressBar(currentPosition, duration));
            });
            audioService.startProgressUpdates();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (audioService != null) {
            audioService.setProgressUpdateListener(null);
        }
    }
    private void togglePlayPause() {
        if (audioService == null) return;

        if (audioService.isPlaying()) {
            audioService.pauseAudio();
            isPlaying = false;
            playButton.setImageResource(R.drawable.button_play);
        } else {
            audioService.resumeAudio();
            isPlaying = true;
            playButton.setImageResource(R.drawable.button_pause);
            progressHandler.post(updateProgressRunnable);
        }
    }

    private String getAudioFilePath(String fileName) {
        File audioFile = new File(getCacheDir(), fileName + ".mp3");
        return audioFile.getAbsolutePath();
    }

    private void playTrack(Item item) {
        String audioPath = getAudioFilePath(item.getTitle());
        oriTitle.setText(item.getTitle());
        oriScript.setText(item.getContent());
        oriName.setText(item.getCategory());
        oriDate.setText(item.getDate());

        // 기존처럼 audioService.prepareAudio() 호출
        audioService.prepareAudio(audioPath, 0, true);
    }

    private void skipNext() {
        if (trackIndex < itemList.size() - 1) {
            trackIndex++;
            playTrack(itemList.get(trackIndex));
        } else {
            Toast.makeText(this, "마지막 트랙입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipLast() {
        if (trackIndex > 0) {
            trackIndex--;
            playTrack(itemList.get(trackIndex));
        } else {
            Toast.makeText(this, "첫 번째 트랙입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private void skipBack() {
        if (audioService != null) {
            int currentPosition = audioService.getCurrentPosition();
            audioService.seekTo(Math.max(currentPosition - 5000, 0));
        }
    }

    private void skipForward() {
        if (audioService != null) {
            int currentPosition = audioService.getCurrentPosition();
            audioService.seekTo(Math.min(currentPosition + 5000, audioService.getDuration()));
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
        resultIntent.putExtra("AudioPosition", audioService.getCurrentPosition());
        resultIntent.putExtra("IsPlaying", audioService.isPlaying());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateProgressBar(int currentPosition, int duration) {
        if (duration > 0) {
            int progress = (int) ((currentPosition / (float) duration) * 100);
            seekBar.setProgress(progress);
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
