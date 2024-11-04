package com.example.myapplication;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextToSpeechTask extends AsyncTask<String, Void, String> {
    private Context context;

    public TextToSpeechTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String apiKey = "AIzaSyDIeqKKGDnDMqe2ykQt0fmYpUhUJNkmuZ4";
        String apiUrl = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey;
        String inputText = params[0];

        try {
            // JSON payload to send
            String jsonInputString = "{\n" +
                    "  \"voice\": {\n" +
                    "    \"languageCode\": \"ko-KR\"\n" +
                    "  },\n" +
                    "  \"input\": {\n" +
                    "    \"text\": \"" + inputText + "\"\n" +
                    "  },\n" +
                    "  \"audioConfig\": {\n" +
                    "    \"audioEncoding\": \"MP3\"\n" +
                    "  }\n" +
                    "}";

            // Set up HttpURLConnection
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            // Send JSON input
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Parse and return audio content from JSON response
            String audioContent = response.toString().replaceAll(".*\"audioContent\":\"", "").replaceAll("\"}", "");
            return audioContent;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String audioContent) {
        if (audioContent != null) {
            playAudioFromBase64(audioContent);
        } else {
            System.out.println("Error: Unable to get audio content");
        }
    }

    private void playAudioFromBase64(String base64Audio) {
        try {
            byte[] audioBytes = Base64.decode(base64Audio, Base64.DEFAULT);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(new ByteArrayInputStream(audioBytes).toString());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
