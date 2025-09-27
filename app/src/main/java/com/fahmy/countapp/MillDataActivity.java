package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.fahmy.countapp.Adapters.MillDataAdapter;
import com.fahmy.countapp.Adapters.ProductEntryAdapter;
import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.ProductEntry;
import com.fahmy.countapp.Data.User;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class MillDataActivity extends AppCompatActivity {
    List<MillData> millReportEntryList;
    RecyclerView rv;
    MillDataAdapter adapter;
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mill_data);

        setUpUiMainFeatures();
        rv = findViewById(R.id.millDataRv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        millReportEntryList = new ArrayList<>();
        adapter = new MillDataAdapter(MillDataActivity.this, millReportEntryList);
        rv.setAdapter(adapter);

//        checkSignedIn();
        String token = getTokenFromPrefs();
        fetchManualMillData(token, 1, 1000, "" );
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
                startActivity(new Intent(MillDataActivity.this, MainActivity.class));
                finish();
            }
            if (item.getItemId() == R.id.nav_mill_data) {
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

        if (item.getItemId() == R.id.logout){
            getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    .edit()
                    .remove("jwt_token")
                    .apply();
            startActivity(new Intent(MillDataActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void fetchManualMillData(String jwtToken, int page, int perPage, String search) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(ApiBase.DEV.getUrl() + "/manual-mill-data")
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
                runOnUiThread(() -> Toast.makeText(MillDataActivity.this,
                        "Failed to fetch mill data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String respBody = response.body().string();
                    Log.i("mill data report respBody", respBody);
                    runOnUiThread(() -> {
                        try {
                            JSONObject root = new JSONObject(respBody);

                            // Get the "data" object
                            JSONObject dataObj = root.getJSONObject("data");

                            // Get the array inside "data"
                            JSONArray arr = dataObj.getJSONArray("data");

                            millReportEntryList.clear();
                            if(arr.length() > 0) {
                                for (int i = 0; i < arr.length(); i++) {

                                    JSONObject obj = arr.getJSONObject(i);

                                    String millCapacity   = obj.optString("mill_capacity");
                                    String millExtraction = obj.optString("mill_extraction");
                                    String filePath = obj.optString("photo_path", "");

                                    millReportEntryList.add(new MillData(millCapacity, millExtraction, filePath));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MillDataActivity.this,
                                    "Error fetching mill data: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        Log.d("MillData", respBody);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MillDataActivity.this,
                            "Error fetching mill data: " + response.code(),
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getTokenFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);

        return true;
    }
}