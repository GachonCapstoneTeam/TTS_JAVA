package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText idInput, passwordInput, checkPasswordInput, nicknameInput;
    private Button submitButton;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String REGISTER_URL = "http://10.0.2.2:8000/api/register/";  //이거 백엔드 맞춰서 수정 해야함.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        idInput = findViewById(R.id.su_id);
        passwordInput = findViewById(R.id.su_password);
        checkPasswordInput = findViewById(R.id.su_check);
        nicknameInput = findViewById(R.id.su_nickname);
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptSignUp() {
        String id = idInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String passwordCheck = checkPasswordInput.getText().toString().trim();
        String nickname = nicknameInput.getText().toString().trim();

        if (id.isEmpty() || password.isEmpty() || passwordCheck.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, "모든 입력을 채워주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordCheck)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        sendSignUpRequest(id, password, nickname);
    }

    private void sendSignUpRequest(String id, String password, String nickname) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", id);
            jsonObject.put("password", password);
            jsonObject.put("nickname", nickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SignUpActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resBody = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        finish(); // 가입 완료 후 로그인 화면으로 돌아감
                    } else {
                        Toast.makeText(SignUpActivity.this, "회원가입 실패: " + resBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
