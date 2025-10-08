package com.fahmy.countapp.Adapters;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.ProductEntry;
import com.fahmy.countapp.Data.Util;
import com.fahmy.countapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class ProductEntryAdapter extends RecyclerView.Adapter<ProductEntryAdapter.ViewHolder>{

    private final List<ProductEntry> items;
    private final Context context;

    public ProductEntryAdapter(Context context, List<ProductEntry> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ProductEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.products_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductEntryAdapter.ViewHolder holder, int position) {
        ProductEntry productEntry = items.get(position);
        holder.productTitleTv.setText(productEntry.getProductTitle() + " - " + Util.extractWeight(productEntry.getProductDescription() ) + " Kgs");
        holder.openingCountTv.setText("Opening: " + productEntry.getOpeningCount());
        holder.closingCountTv.setText("Closing: " + productEntry.getClosingCount());
        holder.totalCountTv.setText("Count: " + productEntry.getTotalCount());
        holder.totalBalesTv.setText(productEntry.getTotalBales()+" bales");


        if(productEntry.getComments().isEmpty() || productEntry.getComments() == null || Objects.equals(productEntry.getComments(), "null")) {
            holder.commentsTv.setVisibility(View.GONE);
        }else {
            holder.commentsTv.setVisibility(View.VISIBLE);
            holder.commentsTv.setText(productEntry.getComments());
        }

//        Log.i("Image path", ApiBase.ROOT.getUrl() + productEntry.getPhoto_path());
        if(!productEntry.getPhoto_path().isEmpty()) {

            Log.i("Image path", ApiBase.ROOT.getUrl() + productEntry.getPhoto_path());
            Picasso.get()
                .load(ApiBase.ROOT.getUrl() + productEntry.getPhoto_path())
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
        TextView productTitleTv, openingCountTv, closingCountTv, totalCountTv, totalBalesTv, commentsTv;
        ImageView photoIv;

        public ViewHolder(View view) {
            super(view);
            productTitleTv = view.findViewById(R.id.productTitleTv);
            openingCountTv = view.findViewById(R.id.openingCountTv);
            closingCountTv = view.findViewById(R.id.closingCountTv);
            totalCountTv = view.findViewById(R.id.totalCountTv);
            totalBalesTv = view.findViewById(R.id.totalBalesTv);
            photoIv = view.findViewById(R.id.photoIv);
            commentsTv = view.findViewById(R.id.commentsTv);
        }
    }
}
