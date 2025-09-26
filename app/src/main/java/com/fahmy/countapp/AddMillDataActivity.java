package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fahmy.countapp.Data.ApiBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddMillDataActivity extends AppCompatActivity {

    Button submitBtn;
    EditText millCapacityEt, millExtractionEt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_mill_data);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        submitBtn = findViewById(R.id.submitBtn);
        millCapacityEt = findViewById(R.id.millCapacityEt);
        millExtractionEt = findViewById(R.id.millExtractionEt);
        submitBtn.setOnClickListener(v-> {
            String millCapacity = millCapacityEt.getText().toString();
            String millExtraction = millExtractionEt.getText().toString();
            if(millCapacity.isEmpty()) {
                millCapacityEt.setError("This field is required");
                millCapacityEt.requestFocus();
            } else if(millExtraction.isEmpty()) {
                millExtractionEt.setError("This field is required");
                millExtractionEt.requestFocus();
            } else  {

                sendManualMillReport(
                    millCapacity,
                    millExtraction,
                    getTokenFromPrefs()
                );
            }
        });
    }


    private void sendManualMillReport( String millCapacity, String millExtraction, String jwtToken) {

        OkHttpClient client = new OkHttpClient();

        // JSON body
        JSONObject json = new JSONObject();
        try {
            json.put("mill_capacity", millCapacity);
            json.put("mill_extraction", millExtraction);
        } catch (JSONException e) {
            Log.i("Mill Report Entry", "Am here12");
            e.printStackTrace();
            Toast.makeText(AddMillDataActivity.this, "Something went wrong: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/manual-mill-data")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();

        Log.i("Mill Report Entry", "Am here");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                Log.e("OnFailure Mill Report Entry", e.getMessage().toString());
                runOnUiThread(() ->
                        Toast.makeText(AddMillDataActivity.this, "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                final String resBody = response.body().string();
                if (response.isSuccessful()) {
                    Log.i("Mill Report Entry", resBody);
                    runOnUiThread(() ->{
                        Toast.makeText(AddMillDataActivity.this, "Data added successfully",
                                Toast.LENGTH_SHORT).show();
                        AddMillDataActivity.this.startActivity(new Intent(AddMillDataActivity.this, MillDataActivity.class));
                        finish();
                    });
                } else {
                    Log.e("sERVERError Mill Report Entry", resBody);
                    runOnUiThread(() ->
                            Toast.makeText(AddMillDataActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private String getTokenFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This will behave like the back button
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}