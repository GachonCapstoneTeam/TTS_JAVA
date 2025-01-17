package com.example.myapplication.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TTSHelper {
    private MediaPlayer mediaPlayer;
    private Context context;
    private boolean isPlaying = false; // 재생 상태를 추적

    public TTSHelper(Context context) {
        this.context = context;
    }

    // 텍스트를 TTS로 변환하고 재생
    public void performTextToSpeech(String text, String apiKey, ImageButton playButton) {
        // 여기에서 TTS API 호출 및 음성 데이터 가져오기 로직을 구현합니다.
        // 이 예제에서는 받은 base64 오디오 데이터를 사용합니다.
        String audioContentEncoded = "base64_encoded_audio"; // 예제 데이터

        playAudio(audioContentEncoded, playButton);
    }

    // 오디오 재생
    private void playAudio(String base64Audio, ImageButton playButton) {
        byte[] decodedAudio = Base64.decode(base64Audio, Base64.DEFAULT);

        try {
            File tempAudioFile = File.createTempFile("tts_audio", ".mp3", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(decodedAudio);
            fos.close();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            updatePlayButton(playButton);

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                updatePlayButton(playButton);
                tempAudioFile.delete();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "오디오 재생 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    // 재생/일시정지 토글
    public void togglePlayPause(ImageButton playButton) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
            } else {
                mediaPlayer.start();
                isPlaying = true;
            }
            updatePlayButton(playButton);
        } else {
            Toast.makeText(context, "오디오가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 버튼 상태 업데이트
    private void updatePlayButton(ImageButton playButton) {
        if (isPlaying) {
            playButton.setImageResource(R.drawable.button_pause);
        } else {
            playButton.setImageResource(R.drawable.button_play);
        }
    }
}
