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
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.Data.ProductEntry;
import com.fahmy.countapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MillDataAdapter extends RecyclerView.Adapter<MillDataAdapter.ViewHolder>{

    private final List<MillData> items;
    private final Context context;

    public MillDataAdapter(Context context, List<MillData> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MillDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mill_data_layout, parent, false);
        return new MillDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MillDataAdapter.ViewHolder holder, int position) {
        MillData millData = items.get(position);
        holder.machineTv.setText(millData.getMachine());
        holder.millCapacityTv.setText("Mill Capacity: " + millData.getMillCapacity());
        holder.millExtractionTv.setText("Mill Extraction: " + millData.getMillExtraction());

        if(!millData.getPhoto_path().isEmpty()) {

            Picasso.get()
                .load(ApiBase.ROOT.getUrl() + millData.getPhoto_path())
                .placeholder(R.drawable.baseline_document_scanner_24)
                .error(R.drawable.baseline_document_scanner_24)
                .into(holder.photoIv);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView millCapacityTv, millExtractionTv, machineTv;
        ImageView photoIv;

        public ViewHolder(View view) {
            super(view);
            machineTv = view.findViewById(R.id.machineTv);
            millCapacityTv = view.findViewById(R.id.millCapacityTv);
            millExtractionTv = view.findViewById(R.id.millExtractionTv);
            photoIv = view.findViewById(R.id.photoIv);
        }
    }
}
