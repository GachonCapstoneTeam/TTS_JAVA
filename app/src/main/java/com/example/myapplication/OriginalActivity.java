package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private ImageButton skipBack, stop, play, skipForward, pdf;
    private Button handleButton, backButton;
    private View bottomMenuContainer;
    private boolean isMenuVisible = true;
    private boolean isPlaying = false;

    private MediaPlayer mediaPlayer;

    private final String API_KEY = BuildConfig.MY_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original);

        // View 초기화
        backButton = findViewById(R.id.backbutton_ori);
        oriScript = findViewById(R.id.oriScript);
        handleButton = findViewById(R.id.handle_shape);
        skipBack = findViewById(R.id.skipback);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        skipForward = findViewById(R.id.skipforward);
        pdf = findViewById(R.id.pdf);
        bottomMenuContainer = findViewById(R.id.bottom_menu_container);
        oriTitle = findViewById(R.id.oriTitle);
        oriName = findViewById(R.id.oriName);
        oriDate = findViewById(R.id.ori_date);

        // Intent에서 데이터 받아오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        String content = intent.getStringExtra("Content");
        String pdfUrl = intent.getStringExtra("PDF_URL");
        String company = intent.getStringExtra("Bank");
        String category = intent.getStringExtra("Category");
        String date = intent.getStringExtra("Date");


        // 제목과 내용 설정
        if (content != null) {
            oriScript.setText(content);
        }

        if (title != null) {
            oriTitle.setText(title);
        }

        if(date != null) {
            oriDate.setText(date);
        }


        // 서버에서 텍스트 가져오기
        // fetchTextFromServer(); // 기존 서버에서 텍스트를 가져오는 부분을 수정하려면 주석 해제하세요.

        // 손잡이 버튼 클릭 이벤트
        handleButton.setOnClickListener(v -> toggleBottomMenu());

        // 버튼 동작 처리
        backButton.setOnClickListener(v -> finish());
        skipBack.setOnClickListener(v -> Toast.makeText(this, "Skip Back", Toast.LENGTH_SHORT).show());
        stop.setOnClickListener(v -> stopAudio());
        play.setOnClickListener(v -> togglePlayPause());
        skipForward.setOnClickListener(v -> Toast.makeText(this, "Skip Forward", Toast.LENGTH_SHORT).show());

        pdf.setOnClickListener(v -> {
            String url_pdf = intent.getStringExtra("PDF_URL"); // 인텐트에서 URL 가져오기
            if (url_pdf != null && !url_pdf.isEmpty()) {
                Intent pdfintent = new Intent(Intent.ACTION_VIEW);
                pdfintent.setDataAndType(Uri.parse(url_pdf), "application/pdf");
                pdfintent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                // PDF를 열 수 있는 앱이 있는지 확인
                try {
                    startActivity(pdfintent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "PDF를 열 수 있는 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "PDF URL이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 메뉴 숨기기/보이기
    private void toggleBottomMenu() {
        if (isMenuVisible) {
            bottomMenuContainer.animate()
                    .translationY(bottomMenuContainer.getHeight() - 80)
                    .setDuration(300)
                    .start();
        } else {
            bottomMenuContainer.setVisibility(View.VISIBLE);
            bottomMenuContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
        isMenuVisible = !isMenuVisible;
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
            runOnUiThread(() -> play.setImageResource(R.drawable.pause));

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                runOnUiThread(() -> play.setImageResource(R.drawable.play));
                tempAudioFile.delete();
            });

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(OriginalActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show());
        }
    }

    // 재생/일시정지 버튼 처리
    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                play.setImageResource(R.drawable.play);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                play.setImageResource(R.drawable.pause);
            }
        } else {
            performTextToSpeech();
        }
    }

    // 오디오 중지
    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            play.setImageResource(R.drawable.play);
        }
    }
}
