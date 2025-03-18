package com.example.myapplication.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

import java.io.IOException;

public class OriginalAudioService extends Service {
    private static final String CHANNEL_ID = "OriginalAudioServiceChannel";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new AudioBinder();
    private boolean isPrepared = false;
    private Handler progressHandler = new Handler();
    private ProgressUpdateListener progressUpdateListener;
    private NextTrackListener nextTrackListener;

    private int trackIndex = 0; // 현재 트랙 인덱스 저장
    private String audioFilePath; // 현재 재생 중인 오디오 파일 경로

    public interface ProgressUpdateListener {
        void onProgressUpdate(int currentPosition, int duration);
    }
    public interface NextTrackListener {
        void onTrackCompleted();
    }

    public class AudioBinder extends Binder {
        public OriginalAudioService getService() {
            return OriginalAudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification("오디오 서비스 실행 중..."));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        progressHandler.removeCallbacks(updateProgressRunnable);
    }

    public void setNextTrackListener(NextTrackListener listener) {
        this.nextTrackListener = listener;
    }
    public void setProgressUpdateListener(ProgressUpdateListener listener) {
        this.progressUpdateListener = listener;
    }

    public void playAudio(String audioFilePath, int trackIndex, int audioPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.seekTo(audioPosition);
                mp.start();
                startProgressUpdates(); // Progress 업데이트 시작

                Log.d("OriginalAudioService", "오디오 재생 시작, Progress 업데이트 시작");
            });

            mediaPlayer.setOnCompletionListener(mp -> stopProgressUpdates());

        } catch (IOException e) {
            Log.e("OriginalAudioService", "오디오 재생 실패: " + e.getMessage());
        }
    }

    public void prepareAudio(String audioFilePath, int position, boolean play) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                Log.d("AudioService", "MediaPlayer 준비 완료");

                // 기존 위치로 이동
                if (position > 0) {
                    Log.d("AudioService", "기존 위치로 이동: " + position);
                    mp.seekTo(position);
                }

                // Home에서 미리 prepare해놓고, Original에서 start()만 실행
                if (play) {
                    mp.start();
                    startForeground(1, getNotification("오디오 재생 중..."));
                    startProgressUpdates();
                }
            });
        } catch (IOException e) {
            Log.e("AudioService", "오디오 준비 실패: " + e.getMessage());
        }
    }


    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressUpdates();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            startProgressUpdates();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    private void startProgressUpdates() {
        progressHandler.post(updateProgressRunnable);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(updateProgressRunnable);
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                if (progressUpdateListener != null) {
                    progressUpdateListener.onProgressUpdate(getCurrentPosition(), getDuration());
                }
                progressHandler.postDelayed(this, 1000);
            }
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "오디오 서비스 채널",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification getNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TalkStock 오디오 서비스")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPrepared() {
        return mediaPlayer != null && isPrepared;
    }

}
