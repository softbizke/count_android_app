package com.fahmy.countapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.Product;
import com.fahmy.countapp.Data.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class AddProductEntryActivity extends AppCompatActivity {

    String selectedProdId = null;
    AutoCompleteTextView autoText;
    Button submitBtn;
    EditText openingCountEt, closingCountEt;
    ImageView selectImgIv;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private Uri photoUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product_entry);

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
                        selectImgIv.setImageURI(photoUri);

                        // If you need to refresh (sometimes needed for newly saved images):
                        selectImgIv.invalidate();
//                        Toast.makeText(AddProductEntryActivity.this,
//                                "Photo saved: " + photoUri.toString(),
//                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        );


        autoText = findViewById(R.id.productsAutoText);
        submitBtn = findViewById(R.id.submitBtn);
        openingCountEt = findViewById(R.id.openingCountEt);
        closingCountEt = findViewById(R.id.closingCountEt);


        fetchProducts(getTokenFromPrefs());

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
            String openingCount = openingCountEt.getText().toString();
            String closingCount = closingCountEt.getText().toString();
            EditText confirmOpeningCountEt = findViewById(R.id.confirmOpeningCountEt);
            EditText confirmClosingCountEt = findViewById(R.id.confirmClosingCountEt);
            String confirmOpeningCount = confirmOpeningCountEt.getText().toString();
            String confirmClosingCount = confirmClosingCountEt.getText().toString();
            if(selectedProdId == null || selectedProdId.isEmpty()) {
                autoText.setError("This field is required");
                autoText.requestFocus();
            } else if(openingCount.isEmpty()) {
                openingCountEt.setError("This field is required");
                openingCountEt.requestFocus();
            } else if(closingCount.isEmpty()) {
                closingCountEt.setError("This field is required");
                closingCountEt.requestFocus();
            } else if(!confirmOpeningCount.equals(openingCount)) {
                confirmOpeningCountEt.setError("The opening counts do not match, please double check");
                confirmOpeningCountEt.requestFocus();
            }else if(!confirmClosingCount.equals(closingCount)) {
                confirmClosingCountEt.setError("The closing counts do not match, please double check");
                confirmClosingCountEt.requestFocus();
            }else if(photoUri == null) {

                Toast.makeText(AddProductEntryActivity.this, "Please take a picture of the machine recordings to continue", Toast.LENGTH_LONG).show();
            }else  {

                File imageFile = new Util().getFileFromUri(photoUri, AddProductEntryActivity.this);
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this, "Something went wrong. Please capture the Image again.", Toast.LENGTH_SHORT).show();
                } else {
                    sendManualProductCount(
                        selectedProdId,
                        Long.parseLong(openingCount),
                        Long.parseLong(closingCount),
                        getTokenFromPrefs(),
                        imageFile
                    );
                }


            }
        });

    }

    private void fetchProducts(String jwtToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/products")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddProductEntryActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String respBody = response.body().string();
                    Log.i("respBody11", respBody);

                    try {

                        JSONObject root = new JSONObject(respBody);

                        // Get the "data" object
                        JSONObject dataObj = root.getJSONObject("data");

                        // Get the array inside "data"
                        JSONArray arr = dataObj.getJSONArray("data");

                        List<Product> productList = new ArrayList<>();

                        if(arr.length() > 0) {
                            productList.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);

                                // Assuming Product has a constructor Product(String id, String name)
                                productList.add(new Product(
                                    obj.getString("id"),
                                    obj.getString("name"),
                                    obj.getString("barcode"),
                                    obj.getString("description")
                                ));
                            }
                        }


                        runOnUiThread(() -> setupAutoComplete(productList));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupAutoComplete(List<Product> products) {

        ArrayAdapter<Product> adapter =
            new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                products);

        autoText.setAdapter(adapter);

        autoText.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = (Product) parent.getItemAtPosition(position);
            selectedProdId = selectedProduct.getId();
            Log.d("Selected Product ID", selectedProdId);
        });
    }


    private void sendManualProductCount(
            String productId,
            long openingCount,
            long closingCount,
            String jwtToken,
            File imageFile
    ) {

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("product_id", productId)
                .addFormDataPart("opening_count", String.valueOf(openingCount))
                .addFormDataPart("closing_count", String.valueOf(closingCount));

        if (imageFile != null && imageFile.exists()) {
            builder.addFormDataPart(
                    "image",                                // field name in Node route
                    imageFile.getName(),                    // file name to send
                    RequestBody.create(
                            imageFile,
                            MediaType.parse("image/*")       // let server accept any image type
                    )
            );
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/manual-products-count")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                AddProductEntryActivity.this,
                                "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String resBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    Log.i("Product Entry", resBody);
                    runOnUiThread(() -> {
                        Toast.makeText(
                                AddProductEntryActivity.this,
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    });
                } else {
                    Log.e("Error inserting manual prod data", resBody);
                    runOnUiThread(() ->
                            Toast.makeText(
                                    AddProductEntryActivity.this,
                                    "Server error: " + resBody,
                                    Toast.LENGTH_SHORT
                            ).show()
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