package com.fahmy.countapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.BinReport;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.Util;
import com.fahmy.countapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

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
        holder.endingTimeTv.setText(Util.formatDate(binReport.getEndingTime()));
        holder.totalBalesTv.setText("Bales: " + binReport.getTotalBales());


    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView binTypeTv, ringCountTv, endingTimeTv, totalBalesTv;
        ImageView photoIv;

        public ViewHolder(View view) {
            super(view);
            ringCountTv = view.findViewById(R.id.ringCountTv);
            binTypeTv = view.findViewById(R.id.binTypeTv);
            endingTimeTv = view.findViewById(R.id.endingTimeTv);
            totalBalesTv = view.findViewById(R.id.totalBalesTv);
            photoIv = view.findViewById(R.id.photoIv);
        }
    }
}
