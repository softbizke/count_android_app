package com.fahmy.countapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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

public class AddMillDataActivity extends AppCompatActivity {

    Button submitBtn;
    EditText millCapacityEt, millExtractionEt, confirmMillCapacityEt, confirmMillExtractionEt;
    ImageView selectImgIv;
    Spinner machineSpinner;

    private static final int CAMERA_PERMISSION_CODE = 102;
    private Uri photoUri;
    private String selectedMachine;
    private ActivityResultLauncher<Uri> takePictureLauncher;

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

        selectImgIv = findViewById(R.id.selectImgIv);
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean success) {
                        if (success != null && success) {
//                            Toast.makeText(AddMillDataActivity.this,
//                                    "Photo saved: " + photoUri.toString(),
//                                    Toast.LENGTH_LONG).show();
                            selectImgIv.setImageURI(photoUri);

                            // If you need to refresh (sometimes needed for newly saved images):
                            selectImgIv.invalidate();
                        }
                    }
                }
        );

        submitBtn = findViewById(R.id.submitBtn);
        millCapacityEt = findViewById(R.id.millCapacityEt);
        millExtractionEt = findViewById(R.id.millExtractionEt);
        confirmMillCapacityEt = findViewById(R.id.confirmMillCapacityEt);
        confirmMillExtractionEt = findViewById(R.id.confirmMillExtractionEt);
        machineSpinner = findViewById(R.id.machineSpinner);

        String[] items = {"Machine A", "Machine B"};
        selectedMachine = items[0];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        machineSpinner.setAdapter(adapter);

        machineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMachine = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        setUpEditTextListeners();

        selectImgIv.setOnClickListener(v->{

            //ask for permissions
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE);
            }

        });
        submitBtn.setOnClickListener(v-> {
            String millCapacity = millCapacityEt.getText().toString();
            String millExtraction = millExtractionEt.getText().toString();
            String confirmMillCapacity = confirmMillCapacityEt.getText().toString();
            String confirmMillExtraction = confirmMillExtractionEt.getText().toString();

            if(selectedMachine.isEmpty()) {
                Toast.makeText(this, "Please select the milling machine.", Toast.LENGTH_SHORT).show();
                millCapacityEt.requestFocus();
            }else if(millCapacity.isEmpty()) {
                millCapacityEt.setError("This field is required");
                millCapacityEt.requestFocus();
            } else if(millExtraction.isEmpty()) {
                millExtractionEt.setError("This field is required");
                millExtractionEt.requestFocus();
            }else if(!confirmMillCapacity.equals(millCapacity)) {
                confirmMillCapacityEt.setError("The mill capacity values do not match, please double check");
                confirmMillCapacityEt.requestFocus();
            }else if(!confirmMillExtraction.equals(millExtraction)) {
                confirmMillExtractionEt.setError("The mill extraction values do not match, please double check");
                confirmMillExtractionEt.requestFocus();
            }  else if (photoUri == null) {
                    Toast.makeText(this, "Please capture the machine reading photo before submitting.", Toast.LENGTH_SHORT).show();
            }else  {



                File imageFile = new Util().getFileFromUri(photoUri, AddMillDataActivity.this);
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this, "Something went wrong. Please capture the Image again.", Toast.LENGTH_SHORT).show();
                } else {



                    //validate mill extraction value is a percentage value eg 40.20, 77.21 70 etc
                    try {
                        double value = Double.parseDouble(millExtraction);
                        if (value < 0 || value > 100) {
                            millExtractionEt.setError("Enter percentage value between 0 and 100");
                            confirmMillExtractionEt.setError("Enter value between 0 and 100");
                            millExtractionEt.requestFocus();
                        } else if (!millExtraction.matches("^\\d{0,3}(\\.\\d{0,2})?$")) {
                            millExtractionEt.setError("Please enter a value with up to 2 decimal places");
                            confirmMillExtractionEt.setError("Please enter a value with up to 2 decimal places");
                            millExtractionEt.requestFocus();
                        } else {

                            sendManualMillReport(
                                millCapacity,
                                millExtraction,
                                getTokenFromPrefs(),
                                imageFile
                            );
                        }
                    } catch (NumberFormatException e) {
                        millExtractionEt.setError("Enter a valid number");
                        confirmMillExtractionEt.setError("Enter a valid number");
                        millExtractionEt.requestFocus();
                    }

                }


            }
        });
    }


    private void setUpEditTextListeners() {

        confirmMillCapacityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                confirmMillCapacityEt.setError(!s.toString().equals(millCapacityEt.getText().toString())? "The mill capacity values do not match": null);

            }
        });

        confirmMillExtractionEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmMillExtractionEt.setError(!s.toString().equals(millExtractionEt.getText().toString())? "The mill extraction count values do not match": null);

            }
        });


    }


    private void sendManualMillReport(
        String millCapacity,
        String millExtraction,
        String jwtToken,
        File imageFile
    ) {



        AlertDialog progressDialog = Util.showDialog(AddMillDataActivity.this, "Submitting data...", R.color.blue);
        runOnUiThread(progressDialog::show);
        OkHttpClient client = new OkHttpClient();

        // --- Build multipart body ---
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("mill_capacity", millCapacity)
                .addFormDataPart("mill_extraction", millExtraction)
                .addFormDataPart("machine", selectedMachine);

        builder.addFormDataPart(
                "image",                                // field name expected by Node route
                imageFile.getName(),
                RequestBody.create(imageFile, MediaType.parse("image/*"))
        );

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/manual-mill-data") // <-- match your Node route
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(requestBody)
                .build();

        Log.i("Mill Report Entry", "Uploading mill data…");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OnFailure Mill Report Entry", e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(
                            AddMillDataActivity.this,
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
                    Log.i("Mill Report Entry", resBody);
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog);
                        Toast.makeText(AddMillDataActivity.this,
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(AddMillDataActivity.this, MillDataActivity.class));
                        finish();
                    });
                } else {
                    Log.e("ServerError Mill Report Entry", resBody);
                    runOnUiThread(() -> {
                        Toast.makeText(AddMillDataActivity.this,
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
            // This will behave like the back button
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchCamera() {
        try {
            File imageFile = File.createTempFile("photo_", ".jpg", getCacheDir());
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}