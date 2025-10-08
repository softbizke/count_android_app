package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private LinearLayout binsContainer;
    private List<EditText> ringInputs = new ArrayList<>();

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
            actionBar.setTitle(R.string.add_bin_report);
        }

        submitBtn = findViewById(R.id.submitBtn);
        binsContainer = findViewById(R.id.binsContainer);

        int numberOfBins = 9;
        for (int i = 0; i < numberOfBins; i++) {
            addBinRow(i + 1);
        }



        submitBtn.setOnClickListener(v-> {

            JSONArray binsArray = new JSONArray();
            EditText commentsTv = findViewById(R.id.commentsET);
            String comments = commentsTv.getText().toString().trim();

            // Validate and build JSON array
            for (int i = 0; i < ringInputs.size(); i++) {
                EditText ringInput = ringInputs.get(i);
                String ringValue = ringInput.getText().toString().trim();

                if (ringValue.isEmpty()) {
                    ringInput.setError("Please enter a value for Bin " + (i + 1));
                    ringInput.requestFocus();
                    return;
                }

                try {
                    JSONObject binObject = new JSONObject();
                    binObject.put("bin_type", "Bin " + (i + 1));
                    binObject.put("ring_count", ringValue);
                    if (!comments.isEmpty()) {
                        binObject.put("comments", comments);
                    }
                    binsArray.put(binObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AddBinsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }

            // Now wrap everything in a single object
            JSONObject mainJson = new JSONObject();
            try {
                mainJson.put("bins", binsArray);

                sendManualMillReport(
                    mainJson,
                    getTokenFromPrefs()
                );
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AddBinsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        });




    }

    private void addBinRow(int binNumber) {
        // Create horizontal layout for each bin
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(16, 16, 16, 16);

        // TextView for bin label
        TextView binLabel = new TextView(this);
        binLabel.setText("Bin " + binNumber + ": ");
        binLabel.setTextSize(16);
        binLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        ));

        EditText ringInput = new EditText(this);
        ringInput.setHint("Enter ring value");
        ringInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ringInput.setBackgroundResource(R.drawable.card_bg);
        ringInput.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 3
        ));

        rowLayout.addView(binLabel);
        rowLayout.addView(ringInput);
        binsContainer.addView(rowLayout);
        ringInputs.add(ringInput);
    }


    private void sendManualMillReport(
        JSONObject mainJson,
        String jwtToken
    ) {


        AlertDialog progressDialog = Util.showDialog(AddBinsActivity.this, "Submitting data...", R.color.blue);
        runOnUiThread(progressDialog::show);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(mainJson.toString(), JSON);

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