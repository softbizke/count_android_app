package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import org.json.JSONException;
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

public class AddBinsActivity extends AppCompatActivity {


    Button submitBtn;
    Spinner binTypeSpinner, ringTypeSpinner;

    private String selectedBinType, selectedRingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_bins);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        submitBtn = findViewById(R.id.submitBtn);
        binTypeSpinner = findViewById(R.id.binTypeSpinner);
        ringTypeSpinner = findViewById(R.id.ringTypeSpinner);

        String[] binTypes = new String[9];
        for (int i = 0; i < 9; i++) {
            binTypes[i] = "Bin " + (i + 1);
        }

        selectedBinType = binTypes[0];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, binTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binTypeSpinner.setAdapter(adapter);

        binTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBinType = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });



        String[] ringCounts = new String[13];
        for (int i = 0; i < 13; i++) {
            ringCounts[i] = String.valueOf(i + 1);
        }
        selectedRingCount = ringCounts[0];
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ringCounts);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringTypeSpinner.setAdapter(adapter1);

        ringTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRingCount = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });



        submitBtn.setOnClickListener(v-> {

            if(selectedRingCount.isEmpty()) {
                Toast.makeText(this, "Please select the ring count to continue.", Toast.LENGTH_SHORT).show();
                ringTypeSpinner.requestFocus();
            }else if(selectedBinType.isEmpty()) {
                Toast.makeText(this, "Please select the bin type to continue.", Toast.LENGTH_SHORT).show();
                binTypeSpinner.requestFocus();
            }else  {

                EditText commentsET = findViewById(R.id.commentsET);
                String comments = commentsET.getText().toString();
                sendManualMillReport(
                    selectedBinType,
                    selectedRingCount,
                    comments,
                    getTokenFromPrefs()
                );


            }
        });




    }




    private void sendManualMillReport(
        String binType,
        String ringCount,
        String comments,
        String jwtToken
    ) {


        AlertDialog progressDialog = Util.showDialog(AddBinsActivity.this, "Submitting data...", R.color.blue);
        runOnUiThread(progressDialog::show);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("bin_type", binType);
            json.put("ring_count", ringCount);
            if (!comments.isEmpty()) {

                json.put("comments", comments);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Json error", e.getMessage());
            runOnUiThread(() -> Util.hideDialog(progressDialog));

        }

        RequestBody requestBody = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
            .url(ApiBase.DEV.getUrl() + "/bins-report")
            .addHeader("Authorization", "Bearer " + jwtToken)
            .post(requestBody)
            .build();

        Log.i("Bin Report", "Uploading bin report data…");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OnFailure Bin Report", e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(
                            AddBinsActivity.this,
                            "Network error: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    Util.hideDialog(progressDialog);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    Log.i("Bin Report Entry", resBody);
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog);
                        Toast.makeText(AddBinsActivity.this,
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                        ).show();

//                        startActivity(new Intent(AddBinsActivity.this, MillDataActivity.class));
                        finish();
                    });
                } else {
                    Log.e("ServerError Bin Report Entry", resBody);
                    runOnUiThread(() -> {
                        Toast.makeText(AddBinsActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT
                        ).show();

                        Util.hideDialog(progressDialog);
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