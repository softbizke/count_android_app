package com.fahmy.countapp.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.AddBinsActivity;
import com.fahmy.countapp.AddProductEntryActivity;
import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.BinReport;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.Util;
import com.fahmy.countapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BinsReportAdapter extends RecyclerView.Adapter<BinsReportAdapter.ViewHolder>{

    private final List<BinReport> items;
    private final Context context;

    public BinsReportAdapter(Context context, List<BinReport> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public BinsReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bin_report_layout, parent, false);
        return new BinsReportAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BinsReportAdapter.ViewHolder holder, int position) {
        BinReport binReport = items.get(position);
        holder.ringCountTv.setText("Rings: " + binReport.getRingCount());
        holder.binTypeTv.setText( binReport.getBinType());
        holder.totalBalesTv.setText("Bales: " + binReport.getTotalBales());

        if (binReport.getEndingTime().isEmpty() || binReport.getEndingTime().equals("null")) {
            holder.endingTimeTv.setVisibility(View.GONE);
        }else {
            holder.endingTimeTv.setVisibility(View.VISIBLE);
            holder.endingTimeTv.setText("Ended At: " + Util.formatDate(binReport.getEndingTime()));
        }

        if(binReport.getComments().isEmpty() || binReport.getComments() == null || Objects.equals(binReport.getComments(), "null")) {
            holder.commentsTv.setVisibility(View.GONE);
        }else {
            holder.commentsTv.setVisibility(View.VISIBLE);
            holder.commentsTv.setText(binReport.getComments());
        }


    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView binTypeTv, ringCountTv, endingTimeTv, totalBalesTv, commentsTv;
        ImageView photoIv;

        public ViewHolder(View view) {
            super(view);
            ringCountTv = view.findViewById(R.id.ringCountTv);
            binTypeTv = view.findViewById(R.id.binTypeTv);
            endingTimeTv = view.findViewById(R.id.endingTimeTv);
            totalBalesTv = view.findViewById(R.id.totalBalesTv);
            photoIv = view.findViewById(R.id.photoIv);
            commentsTv = view.findViewById(R.id.commentsTv);
        }
    }


    private String getTokenFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("jwt_token", null);
    }
}
