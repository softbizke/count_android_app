package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fahmy.countapp.Data.ApiBase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    EditText phoneEt, codeEt;
    TextView codeTv;
    Button submitBtn;

    Boolean isCodeSent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        phoneEt = findViewById(R.id.phoneEt);
        codeEt = findViewById(R.id.codeEt);
        codeTv = findViewById(R.id.codeTv);
        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v->{
            String phone = phoneEt.getText().toString();
            String code = codeEt.getText().toString();
            if(phone.isEmpty()) {
                phoneEt.setError("Phone number is required");
                phoneEt.requestFocus();
            } else if(code.isEmpty() && isCodeSent) {
                codeEt.setError("Enter the code sent on your phone");
                codeEt.requestFocus();
            } else {

                JSONObject json = new JSONObject();
                try {
                    json.put("phone_no", phone);

                    if (isCodeSent) {
                        json.put("code", code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send request
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(ApiBase.CURRENT.getUrl() + "/auth/login-via-phone")
                        .post(body)
                        .build();

                // Network call must be off the UI thread
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e("onFailure", e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this,
                                        "Request failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        final String resp = response.body().string();
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                // Handle JSON result here
                                Toast.makeText(LoginActivity.this,
                                        resp,
                                        Toast.LENGTH_LONG).show();
                                if(isCodeSent) {

                                    try {

                                        JSONObject json = new JSONObject(resp);
                                        String token = json.getString("token");
                                        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putString("jwt_token", token).apply();

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }else {
                                    isCodeSent = true;
                                    codeEt.setVisibility(TextView.VISIBLE);
                                    codeTv.setVisibility(TextView.VISIBLE);
                                    submitBtn.setText(R.string.verify_code);
                                }
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Error: " + resp,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

            }

        });

    }
}