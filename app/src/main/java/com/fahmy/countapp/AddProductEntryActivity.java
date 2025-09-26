package com.fahmy.countapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.fahmy.countapp.Data.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddProductEntryActivity extends AppCompatActivity {

    String selectedProdId = null;
    AutoCompleteTextView autoText;
    Button submitBtn;
    EditText openingCountEt, closingCountEt;
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

        autoText = findViewById(R.id.productsAutoText);
        submitBtn = findViewById(R.id.submitBtn);
        openingCountEt = findViewById(R.id.openingCountEt);
        closingCountEt = findViewById(R.id.closingCountEt);
        fetchProducts(getTokenFromPrefs());
        submitBtn.setOnClickListener(v-> {
            String openingCount = openingCountEt.getText().toString();
            String closingCount = closingCountEt.getText().toString();
            if(selectedProdId == null || selectedProdId.isEmpty()) {
                autoText.setError("This field is required");
                autoText.requestFocus();
            } else if(openingCount.isEmpty()) {
                openingCountEt.setError("This field is required");
                openingCountEt.requestFocus();
            } else if(closingCount.isEmpty()) {
                closingCountEt.setError("This field is required");
                closingCountEt.requestFocus();
            } else  {

                sendManualProductCount(
                    selectedProdId,
                    Long.parseLong(openingCount),
                    Long.parseLong(openingCount),
                    getTokenFromPrefs()
                );
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

                    try {
                        JSONArray arr = new JSONArray(respBody);
                        List<Product> productList = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            productList.add(new Product(
                                    obj.getString("id"),
                                    obj.getString("name")
                            ));
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


        ArrayAdapter<Product> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                products
        );

        autoText.setAdapter(adapter);

        autoText.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = (Product) parent.getItemAtPosition(position);
            selectedProdId = selectedProduct.getId();
            Log.d("Selected Product ID", selectedProdId);
        });
    }


    private void sendManualProductCount(String productId, long openingCount, long closingCount, String jwtToken) {

        OkHttpClient client = new OkHttpClient();

        // JSON body
        JSONObject json = new JSONObject();
        try {
            json.put("product_id", productId);
            json.put("opening_count", openingCount);
            json.put("closing_count", closingCount);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(AddProductEntryActivity.this, "Something went wrong: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(ApiBase.DEV.getUrl() + "/manual-products-count")
                .addHeader("Authorization", "Bearer " + jwtToken) // <-- pass your JWT
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddProductEntryActivity.this, "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String resBody = response.body().string();
                    Log.i("Product Entry", resBody);
                    runOnUiThread(() ->
                            Toast.makeText(AddProductEntryActivity.this, "Data added successfully",
                                    Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(AddProductEntryActivity.this,
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