package com.fahmy.countapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Data.ProductEntry;
import com.fahmy.countapp.R;

import java.util.List;

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
        holder.productTitleTv.setText(productEntry.getProductTitle());
        holder.openingCountTv.setText("Opening: " + productEntry.getOpeningCount());
        holder.closingCountTv.setText("Closing: " + productEntry.getClosingCount());
        holder.totalCountTv.setText(productEntry.getTotalCount() + " kgs");
        holder.totalBalesTv.setText(productEntry.getTotalBales()+" bales");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitleTv, openingCountTv, closingCountTv, totalCountTv, totalBalesTv;

        public ViewHolder(View view) {
            super(view);
            productTitleTv = view.findViewById(R.id.productTitleTv);
            openingCountTv = view.findViewById(R.id.openingCountTv);
            closingCountTv = view.findViewById(R.id.closingCountTv);
            totalCountTv = view.findViewById(R.id.totalCountTv);
            totalBalesTv = view.findViewById(R.id.totalBalesTv);
        }
    }
}
