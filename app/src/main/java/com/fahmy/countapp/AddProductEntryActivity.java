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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fahmy.countapp.Adapters.ProductAutoCompleteAdapter;
import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.Product;
import com.fahmy.countapp.Data.User;
import com.fahmy.countapp.Data.UserRoles;
import com.fahmy.countapp.Data.Util;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    EditText openingCountEt, closingCountEt, confirmOpeningCountEt, confirmClosingCountEt, totalBagsEt, confirmTotalBagsEt;
    ImageView selectImgIv;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private Uri photoUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    User user;

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
            actionBar.setTitle(R.string.add_product_report);
        }


        String jsonUser = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("user", null);
        if(jsonUser != null) {

            user = new Gson().fromJson(jsonUser, User.class);
        } else {
            startActivity(new Intent(AddProductEntryActivity.this, LoginActivity.class));
            finish();
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

        if (user.getRole().equals(UserRoles.OPERATOR.getValue())) {

            findViewById(R.id.openingClosingContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.branPollardContainer).setVisibility(View.GONE);

        } else if (user.getRole().equals(UserRoles.BRAN_POLLARD_OPERATOR.getValue())) {

            findViewById(R.id.openingClosingContainer).setVisibility(View.GONE);
            findViewById(R.id.branPollardContainer).setVisibility(View.VISIBLE);
        }

        openingCountEt = findViewById(R.id.openingCountEt);
        closingCountEt = findViewById(R.id.closingCountEt);
        confirmOpeningCountEt = findViewById(R.id.confirmOpeningCountEt);
        confirmClosingCountEt = findViewById(R.id.confirmClosingCountEt);
        totalBagsEt = findViewById(R.id.totalBagsEt);
        confirmTotalBagsEt = findViewById(R.id.confirmTotalBagsEt);

        setUpEditTextListeners();


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
            String totalBags = totalBagsEt.getText().toString();
            String confirmOpeningCount = confirmOpeningCountEt.getText().toString();
            String confirmClosingCount = confirmClosingCountEt.getText().toString();
            String confirmTotalBags = confirmTotalBagsEt.getText().toString();
            boolean isBranPollardOperator = user.getRole().equals(UserRoles.BRAN_POLLARD_OPERATOR.getValue());
            if(selectedProdId == null || selectedProdId.isEmpty()) {
                autoText.setError("This field is required");
                autoText.requestFocus();
            } else if(!isBranPollardOperator && openingCount.isEmpty()) {
                openingCountEt.setError("This field is required");
                openingCountEt.requestFocus();
            } else if(!isBranPollardOperator && closingCount.isEmpty()) {
                closingCountEt.setError("This field is required");
                closingCountEt.requestFocus();
            } else if(!isBranPollardOperator && !confirmOpeningCount.equals(openingCount)) {
                confirmOpeningCountEt.setError("The opening counts do not match, please double check");
                confirmOpeningCountEt.requestFocus();
            }else if(!isBranPollardOperator && !confirmClosingCount.equals(closingCount)) {
                confirmClosingCountEt.setError("The closing counts do not match, please double check");
                confirmClosingCountEt.requestFocus();
            }else if(isBranPollardOperator && totalBags.isEmpty()) {
                totalBagsEt.setError("This field is required");
                totalBagsEt.requestFocus();
            }else if(isBranPollardOperator && !confirmTotalBags.equals(totalBags)) {
                confirmTotalBagsEt.setError("The total bags do not match, please double check");
                confirmTotalBagsEt.requestFocus();
            }else if(photoUri == null) {

                Toast.makeText(AddProductEntryActivity.this, "Please take a picture of the machine recordings to continue", Toast.LENGTH_LONG).show();
            }else  {

                File imageFile = new Util().getFileFromUri(photoUri, AddProductEntryActivity.this);
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this, "Something went wrong. Please capture the Image again.", Toast.LENGTH_SHORT).show();
                } else {


                    EditText commentsET = findViewById(R.id.commentsET);
                    String comments = commentsET.getText().toString();
                    sendManualProductCount(
                        selectedProdId,
                        isBranPollardOperator?0:Long.parseLong(openingCount),
                        isBranPollardOperator?0:Long.parseLong(closingCount),
                        isBranPollardOperator?Long.parseLong(totalBags):0,
                        comments,
                        getTokenFromPrefs(),
                        imageFile,
                        isBranPollardOperator
                    );
                }


            }
        });

    }

    private void setUpEditTextListeners() {

        confirmOpeningCountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().equals(openingCountEt.getText().toString())) {
                    //show error
                    confirmOpeningCountEt.setError("The opening count values do not match");

                } else {
                    //remove error
                    confirmOpeningCountEt.setError(null);
                }

            }
        });

        confirmClosingCountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().equals(closingCountEt.getText().toString())) {
                    //show error
                    confirmClosingCountEt.setError("The closing count values do not match");

                } else {
                    //remove error
                    confirmClosingCountEt.setError(null);
                }

            }
        });

        confirmTotalBagsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!s.toString().equals(totalBagsEt.getText().toString())) {
                    //show error
                    confirmTotalBagsEt.setError("The total bags values do not match");

                } else {
                    //remove error
                    confirmTotalBagsEt.setError(null);
                }

            }
        });


    }

    private void fetchProducts(String jwtToken) {
        Log.e("Fetching", "I am here");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/products")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Fetching products", e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(AddProductEntryActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String respBody = response.body().string();
                if (response.isSuccessful()) {
                    Log.i("respBody11", respBody);

                    try {

                        JSONObject root = new JSONObject(respBody);

                        // Get the "data" object
                        JSONObject dataObj = root.getJSONObject("data");

                        // Get the array inside "data"
                        JSONArray arr = dataObj.getJSONArray("data");

                        List<Product> productList = new ArrayList<>();
                        Set<String> seenProductIds = new HashSet<>();

                        if(arr.length() > 0) {
                            productList.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);


                                String productId = obj.getString("id");
                                if (seenProductIds.contains(productId)) continue;
                                String productName = obj.getString("name").toLowerCase();
//                                Log.i("prod-name", productName);
                                boolean isBranPollardProduct = productName.contains("bran") || productName.contains("pollard");

                                //only add bran/pollard products for bran pollard operator and other products for the operator
                                if (user.getRole().equals(UserRoles.OPERATOR.getValue())) {

                                    if(!isBranPollardProduct) {

                                        productList.add(new Product(
                                            obj.getString("id"),
                                            obj.getString("name"),
                                            obj.getString("barcode"),
                                            obj.getString("description")
                                        ));
                                        seenProductIds.add(productId);
                                    }

                                } else if (user.getRole().equals(UserRoles.BRAN_POLLARD_OPERATOR.getValue())) {
                                    if(isBranPollardProduct) {

                                        productList.add(new Product(
                                            obj.getString("id"),
                                            obj.getString("name"),
                                            obj.getString("barcode"),
                                            obj.getString("description")
                                        ));
                                        seenProductIds.add(productId);
                                    }
                                }
                            }
                        }


                        runOnUiThread(() -> setupAutoComplete(productList));

                    } catch (JSONException e) {
                        Log.e("Fetching products", e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Fetching products", respBody);
                }
            }
        });
    }

    private void setupAutoComplete(List<Product> products) {
        ProductAutoCompleteAdapter adapter =
                new ProductAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line, products);

        autoText.setAdapter(adapter);
        autoText.setThreshold(1);

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
        long totalBags,
        String comments,
        String jwtToken,
        File imageFile,
        boolean isBranPollardOperator
    ) {



        submitBtn.setEnabled(false);
        AlertDialog progressDialog = Util.showDialog(AddProductEntryActivity.this, "Submitting data...", R.color.blue);
        runOnUiThread(progressDialog::show);
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("product_id", productId);

        if(!isBranPollardOperator) {

            builder
                .addFormDataPart("opening_count", String.valueOf(openingCount))
                .addFormDataPart("closing_count", String.valueOf(closingCount));

        }else {

            builder
                    .addFormDataPart("bags", String.valueOf(totalBags));
        }

        if (!comments.isEmpty()) {
            builder.addFormDataPart("comments", comments);
        }

        if (imageFile != null && imageFile.exists()) {
            builder.addFormDataPart(
                "image",
                imageFile.getName(),
                RequestBody.create(
                    imageFile,
                    MediaType.parse("image/*")
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
                runOnUiThread(() -> {
                    Toast.makeText(
                            AddProductEntryActivity.this,
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
                    Log.i("Product Entry", resBody);
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog);
                        submitBtn.setEnabled(true);
                        Toast.makeText(
                                AddProductEntryActivity.this,
                                "Data added successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    });
                } else {
                    Log.e("Error inserting manual prod data", resBody);
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog);
                        submitBtn.setEnabled(true);
                        Toast.makeText(
                                AddProductEntryActivity.this,
                                "Server error: " + resBody,
                                Toast.LENGTH_SHORT
                        ).show();
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