package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.Util;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAttaReportActivity extends AppCompatActivity {



    Button submitBtn;
    EditText totalBagsEt, confirmTotalBagsEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_atta_report);



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
            actionBar.setTitle(R.string.add_atta_report);
        }

        submitBtn = findViewById(R.id.submitBtn);
        totalBagsEt = findViewById(R.id.totalBagsEt);
        confirmTotalBagsEt = findViewById(R.id.confirmTotalBagsEt);

        setUpEditTextListeners();

        submitBtn.setOnClickListener(v-> {
            String totalBags = totalBagsEt.getText().toString();
            String confirmTotalBags = confirmTotalBagsEt.getText().toString();

            if(totalBags.isEmpty()) {
                totalBagsEt.setError("This field is required");
                totalBagsEt.requestFocus();
            } else if(confirmTotalBags.isEmpty()) {
                confirmTotalBagsEt.setError("This field is required");
                confirmTotalBagsEt.requestFocus();
            }else  {



                EditText commentsET = findViewById(R.id.commentsET);
                String comments = commentsET.getText().toString();
                submitAttaReport(
                    totalBags,
                    comments,
                    getTokenFromPrefs()
                );


            }
        });


    }



    private void setUpEditTextListeners() {

        confirmTotalBagsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                confirmTotalBagsEt.setError(!s.toString().equals(totalBagsEt.getText().toString())? "The total bags values do not match": null);

            }
        });

    }


    private void submitAttaReport(
            String totalBags,
            String comments,
            String jwtToken
    ) {


        Log.i("Atta Report Entry", "I am here");

        submitBtn.setEnabled(false);
        AlertDialog progressDialog = Util.showDialog(AddAttaReportActivity.this, "Submitting data...", R.color.blue);
        runOnUiThread(progressDialog::show);
        OkHttpClient client = new OkHttpClient();

        JSONObject attaDataObject = new JSONObject();
        try {
            attaDataObject.put("bags", totalBags);
            if (!comments.isEmpty()) {
                attaDataObject.put("comments", comments);
            }
        }catch (Exception e) {
            Log.e("Atta Report Error", e.getMessage());
            runOnUiThread(() -> {
                Util.hideDialog(progressDialog);
                Toast.makeText(AddAttaReportActivity.this, "Something went wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }



        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(attaDataObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/atta-data-report")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OnFailure Mill Report Entry", e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(
                            AddAttaReportActivity.this,
                            "Network error: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    Util.hideDialog(progressDialog);
                    submitBtn.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    Log.i("Atta Report Entry", resBody);
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog);
                        submitBtn.setEnabled(true);
                        Toast.makeText(AddAttaReportActivity.this,
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(AddAttaReportActivity.this, AttaReportActivity.class));
                        finish();
                    });
                } else {
                    Log.e("ServerError Mill Report Entry", resBody);
                    runOnUiThread(() -> {
                        Toast.makeText(AddAttaReportActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT
                        ).show();

                        Util.hideDialog(progressDialog);
                        submitBtn.setEnabled(true);
                    });
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}