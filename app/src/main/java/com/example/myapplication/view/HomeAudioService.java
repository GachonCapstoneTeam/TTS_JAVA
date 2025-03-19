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

public class HomeAudioService extends Service {
    private static final String CHANNEL_ID = "HomeAudioServiceChannel";
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
        public HomeAudioService getService() {
            return HomeAudioService.this;
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
                if (audioPosition > 0) {
                    mp.seekTo(audioPosition); // 기존 진행 위치로 이동
                    Log.d("HomeAudioService", "기존 위치로 이동: " + audioPosition);
                }
                mp.start();
                startForeground(1, getNotification("오디오 재생 중..."));
                startProgressUpdates();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("HomeAudioService", " MediaPlayer 완료 감지! 현재 위치: " + mp.getCurrentPosition() + " / 전체 길이: " + mp.getDuration());

                stopProgressUpdates();
                if (nextTrackListener != null) {
                    nextTrackListener.onTrackCompleted();
                    Log.d("HomeAudioService", "현재 트랙 완료 -> 다음 트랙으로 이동");
                }else {
                    Log.e("HomeAudioService", "nextTrackListener가 설정되지 않음!");
                }
            });

            // 강제 트랙 변경 (백업용)
            new Handler().postDelayed(() -> {
                if (mediaPlayer != null && !mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() >= mediaPlayer.getDuration() - 1000) {
                    Log.d("HomeAudioService", "onCompletionListener 강제 실행!");
                    if (nextTrackListener != null) {
                        nextTrackListener.onTrackCompleted();
                    }
                }
            }, 1000);

        } catch (IOException e) {
            Log.e("HomeAudioService", "오디오 재생 실패: " + e.getMessage());
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
                Log.d("HomeAudioService", "MediaPlayer 준비 완료: "+ audioFilePath);

                // 기존 위치로 이동
                if (position > 0) {
                    Log.d("HomeAudioService", "기존 위치로 이동: " + position);
                    mp.seekTo(position);
                }

                // Home에서 미리 prepare해놓고, Original에서 start()만 실행
                if (play) {
                    mp.start();
                    Log.d("HomeAudioService", " MediaPlayer 재생 시작! isPlaying: " + mp.isPlaying());
                    startForeground(1, getNotification("오디오 재생 중..."));
                    startProgressUpdates();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("HomeAudioService", " onCompletionListener 실행됨!"); // 이 로그 확인 필수

                stopProgressUpdates();
                if (nextTrackListener != null) {
                    nextTrackListener.onTrackCompleted();
                    Log.d("HomeAudioService", "현재 트랙 완료 → 다음 트랙으로 이동");
                } else {
                    Log.e("HomeAudioService", "nextTrackListener가 설정되지 않음!");
                }
            });
        } catch (IOException e) {
            Log.e("HomeAudioService", "오디오 준비 실패: " + e.getMessage());
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressUpdates();
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.start();
            startForeground(1, getNotification("오디오 재생 중..."));
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

    public void startProgressUpdates() {
        progressHandler.post(updateProgressRunnable);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(updateProgressRunnable);
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int totalDuration = mediaPlayer.getDuration();

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
