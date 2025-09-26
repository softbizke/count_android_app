package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Adapters.ProductEntryAdapter;
import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.Product;
import com.fahmy.countapp.Data.ProductEntry;
import com.fahmy.countapp.Data.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    List<ProductEntry> productEntriesList;
    RecyclerView rv;
    ProductEntryAdapter adapter;
    DrawerLayout drawerLayout;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fab).setOnClickListener(v->{
//            startActivity(new Intent(MainActivity.this, AddProductEntryActivity.class));
            showCustomOverflowMenu();
        });

        setUpUiMainFeatures();
        rv = findViewById(R.id.productsEntryRv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        productEntriesList = new ArrayList<>();
        adapter = new ProductEntryAdapter(MainActivity.this, productEntriesList);
        rv.setAdapter(adapter);

        checkSignedIn();
        String token = getTokenFromPrefs();
        fetchManualProductCounts(token, 1, 1000, "" );


    }

    private void setUpUiMainFeatures() {
        drawerLayout = findViewById(R.id.main);
        NavigationView navigationView = findViewById(R.id.navigation_view);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // Show hamburger
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_menu_24); // Hamburger icon
        }
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                //
            }
            if (item.getItemId() == R.id.nav_mill_data) {
                startActivity(new Intent(MainActivity.this, MillDataActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        if (item.getItemId() == R.id.action_add_count){
            startActivity(new Intent(MainActivity.this, AddProductEntryActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_add_mill){
            startActivity(new Intent(MainActivity.this, AddMillDataActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkSignedIn() {
        String token = getTokenFromPrefs();

        // If token missing → go to Login
        if (token == null || token.isEmpty()) {

            redirectToLogin();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        // Build request with Authorization header
        Request request = new Request.Builder()
                .url(ApiBase.CURRENT.getUrl() + "/auth/signedin")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Network error – handle gracefully (e.g., show a Toast on UI thread)
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 401 or 403 → not signed in
                    runOnUiThread(() -> redirectToLogin());
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    boolean signedIn = json.optBoolean("signedIn", false);
                    if (!signedIn) {
                        runOnUiThread(() -> redirectToLogin());
                    } else {
                        JSONObject userJson = json.optJSONObject("user");
                        user = new User(
                            userJson.get("id").toString(),
                            userJson.get("phone").toString()
                        );

                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Invalid server response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void fetchManualProductCounts(String jwtToken, int page, int perPage, String search) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(ApiBase.DEV.getUrl()+"/manual-products-count")
                .newBuilder()
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("perPage", String.valueOf(perPage))
                .addQueryParameter("search", search)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Failed to fetch product counts: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String respBody = response.body().string();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject root = new JSONObject(respBody);

                            // Get the "data" object
                            JSONObject dataObj = root.getJSONObject("data");

                            // Get the array inside "data"
                            JSONArray arr = dataObj.getJSONArray("data");

                            productEntriesList.clear();
                            if(arr.length() > 0) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);

                                    String productTitle = obj.optString("product_name"); // or obj.optString("product_name") depending on API
                                    String openingCount = obj.optString("opening_count");
                                    String closingCount = obj.optString("closing_count");
                                    String totalCount = obj.optString("total_count", "0");
                                    String totalBalesStr = obj.optString("total_bales", "0");

                                    totalBalesStr = (totalBalesStr == null || totalBalesStr.equals("null") || totalBalesStr.isEmpty()) ? "0" : totalBalesStr;
                                    BigDecimal totalBales = new BigDecimal(totalBalesStr).setScale(2, RoundingMode.HALF_UP);

                                    double totalBalesLong = totalBales.doubleValue();

                                    productEntriesList.add(new ProductEntry(productTitle, openingCount, closingCount, totalCount, String.valueOf(totalBalesLong)));

                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,
                                    "Error fetching product counts: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        Log.i("ProductCounts", respBody);
                    });
                } else {
                    Log.e("Error ProductCounts ", respBody);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Error fetching product counts: " + response.code(),
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
    }



    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getTokenFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    private void showCustomOverflowMenu() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView addCount = bottomSheetView.findViewById(R.id.addCountData);
        TextView addMill = bottomSheetView.findViewById(R.id.addMillData);

        addCount.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, AddProductEntryActivity.class));
        });

        addMill.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, AddMillDataActivity.class));
        });

        bottomSheetDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkSignedIn();
        String token = getTokenFromPrefs();
        fetchManualProductCounts(token, 1, 1000, "" );
    }
}