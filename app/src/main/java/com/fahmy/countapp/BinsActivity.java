package com.fahmy.countapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Adapters.BinsReportAdapter;
import com.fahmy.countapp.Adapters.MillDataAdapter;
import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.BinReport;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.User;
import com.fahmy.countapp.Data.UserRoles;
import com.fahmy.countapp.Data.Util;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BinsActivity extends AppCompatActivity {
    List<BinReport> binsReportList;
    RecyclerView rv;
    BinsReportAdapter adapter;
    DrawerLayout drawerLayout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bins);
        findViewById(R.id.fab).setOnClickListener(v->{
            showCustomOverflowMenu();
        });

        setUpUiMainFeatures();
        rv = findViewById(R.id.millDataRv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        binsReportList = new ArrayList<>();
        adapter = new BinsReportAdapter(BinsActivity.this, binsReportList);
        rv.setAdapter(adapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Bins Report");
        }

        checkSignedIn();


    }



    private void checkSignedIn() {
        String token = getTokenFromPrefs();

        // If token missing → go to Login
        if (token == null || token.isEmpty()) {

            redirectToLogin();

        }else {

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
                            Toast.makeText(BinsActivity.this, "Network error: " + e.getMessage(),
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
                                    userJson.get("phone").toString(),
                                    userJson.get("role").toString()
                            );
                            getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putString("user", new Gson().toJson(user)).apply();

                            String token = getTokenFromPrefs();
                            setUpUiMainFeatures();

                            // Formatter for ISO 8601 in UTC
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                            // Start date → today at 12:00 AM
                            Calendar startCal = Calendar.getInstance();
                            startCal.set(Calendar.HOUR_OF_DAY, 0);
                            startCal.set(Calendar.MINUTE, 0);
                            startCal.set(Calendar.SECOND, 0);
                            startCal.set(Calendar.MILLISECOND, 0);
                            String startDate = sdf.format(startCal.getTime());

                            // End date → tomorrow at 12:00 AM
                            Calendar endCal = (Calendar) startCal.clone();
                            endCal.add(Calendar.DAY_OF_MONTH, 1);
                            String endDate = sdf.format(endCal.getTime());

                            fetchBinReportData(token, 1, 1000, "", startDate, endDate );

                        }
                    } catch (JSONException e) {
                        Log.e("Signed in", e.getMessage());
                        runOnUiThread(() -> Toast.makeText(BinsActivity.this,
                                "Invalid server response", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        if (item.getItemId() == R.id.logout){
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs
                .edit()
                .remove("jwt_token")
                .apply();
            prefs
                .edit()
                .remove("user")
                .apply();
            startActivity(new Intent(BinsActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void fetchBinReportData(String jwtToken, int page, int perPage, String search, String startDate, String endDate) {

        final AlertDialog[] progressDialog = new AlertDialog[1];

        runOnUiThread(() -> {
            progressDialog[0] = Util.showDialog(BinsActivity.this, "Fetching today's data...", R.color.blue);
            progressDialog[0].show();
        });
        
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(ApiBase.DEV.getUrl() + "/bins-report")
                .newBuilder()
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("perPage", String.valueOf(perPage))
                .addQueryParameter("search", search)
                .addQueryParameter("startDate", startDate)
                .addQueryParameter("endDate", endDate)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {

                    Util.hideDialog(progressDialog[0]);
                    Toast.makeText(BinsActivity.this,
                            "Failed to fetch mill data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String respBody = response.body().string();
                    Log.i("Bins report respBody", respBody);
                    runOnUiThread(() -> {
                        try {
                            JSONObject root = new JSONObject(respBody);

                            // Get the "data" object
                            JSONObject dataObj = root.getJSONObject("data");

                            // Get the array inside "data"
                            JSONArray arr = dataObj.getJSONArray("data");

                            binsReportList.clear();
                            if(arr.length() > 0) {
                                for (int i = 0; i < arr.length(); i++) {

                                    JSONObject obj = arr.getJSONObject(i);

                                    binsReportList.add(new BinReport(
                                        obj.optInt("ring_count", 0),
                                        obj.optString("bin_type", ""),
                                        obj.optString("bales", ""),
                                        obj.optString("ending_time", ""),
                                        obj.optString("comments", "")
                                    ));
                                }
                                adapter.notifyDataSetChanged();
                            }

                            Util.hideDialog(progressDialog[0]);


                        } catch (JSONException e) {

                            Util.hideDialog(progressDialog[0]);
                            e.printStackTrace();
                            Toast.makeText(BinsActivity.this,
                                    "Error fetching mill data: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        Log.d("MillData", respBody);
                    });
                } else {
                    runOnUiThread(() -> {

                        Util.hideDialog(progressDialog[0]);
                        Toast.makeText(BinsActivity.this,
                                "Error fetching mill data: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    private void setUpUiMainFeatures() {
        drawerLayout = findViewById(R.id.main);
        NavigationView navigationView = findViewById(R.id.navigation_view);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_menu_24);
        }



        User userDet = user!= null? user: getUserFromPrefs();

        Menu menu = navigationView.getMenu();
        MenuItem millDataItem = menu.findItem(R.id.nav_mill_data);
        MenuItem binsReportItem = menu.findItem(R.id.bins_report);
        MenuItem homeCountData = menu.findItem(R.id.nav_home);
        if(userDet != null && userDet.getRole().equals(UserRoles.OPERATOR.getValue())) {

            if (millDataItem != null) {
                millDataItem.setVisible(false);
            }
            if (binsReportItem != null) {
                binsReportItem.setVisible(false);
            }
        } else if(userDet != null && userDet.getRole().equals(UserRoles.MILLER.getValue())) {

            if (homeCountData != null) {
                homeCountData.setVisible(false);
            }
            if (binsReportItem != null) {
                binsReportItem.setVisible(false);
            }
        } else if(userDet != null && userDet.getRole().equals(UserRoles.CONTROLLER.getValue())) {

            if (millDataItem != null) {
                millDataItem.setVisible(false);
            }
            if (homeCountData != null) {
                homeCountData.setVisible(false);
            }
        }


        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(BinsActivity.this, MainActivity.class));
                finish();
            }
            if (item.getItemId() == R.id.nav_mill_data) {
                startActivity(new Intent(BinsActivity.this, MillDataActivity.class));
                finish();
            }

            if (item.getItemId() == R.id.bins_report) {
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }




    private void redirectToLogin() {
        Intent intent = new Intent(BinsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private String getTokenFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }

    private User getUserFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userJson = prefs.getString("user", null);
        return userJson == null?null:new Gson().fromJson(userJson, (Type) User.class);
    }

    private void showCustomOverflowMenu() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView addCount = bottomSheetView.findViewById(R.id.addCountData);
        TextView addMill = bottomSheetView.findViewById(R.id.addMillData);
        TextView addBin = bottomSheetView.findViewById(R.id.addBinReport);



        addCount.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(BinsActivity.this, AddProductEntryActivity.class));
        });

        addMill.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(BinsActivity.this, AddMillDataActivity.class));
        });

        addBin.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(BinsActivity.this, AddBinsActivity.class));
        });


        User userDet = user!= null? user: getUserFromPrefs();

        if(userDet != null && userDet.getRole().equals(UserRoles.OPERATOR.getValue())) {
            addMill.setVisibility(View.GONE);
            addBin.setVisibility(View.GONE);
        }

        if(userDet != null && userDet.getRole().equals(UserRoles.MILLER.getValue())) {
            addCount.setVisibility(View.GONE);
            addBin.setVisibility(View.GONE);
        }

        if(userDet != null && userDet.getRole().equals(UserRoles.CONTROLLER.getValue())) {
            addCount.setVisibility(View.GONE);
            addMill.setVisibility(View.GONE);
        }

        bottomSheetDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSignedIn();

    }
}