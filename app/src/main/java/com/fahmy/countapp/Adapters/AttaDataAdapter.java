package com.fahmy.countapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fahmy.countapp.Data.ApiBase;
import com.fahmy.countapp.Data.AttaData;
import com.fahmy.countapp.Data.MillData;
import com.fahmy.countapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class AttaDataAdapter extends RecyclerView.Adapter<AttaDataAdapter.ViewHolder>{

    private final List<AttaData> items;
    private final Context context;

    public AttaDataAdapter(Context context, List<AttaData> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AttaDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.atta_data_layout, parent, false);
        return new AttaDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttaDataAdapter.ViewHolder holder, int position) {
        AttaData attaData = items.get(position);
        holder.titleTv.setText(R.string.atta_flour);
        holder.totalBagsTv.setText("Bags: " + attaData.getBags());
        holder.totalBalesTv.setText(attaData.getTotalBales() + "Kgs");

        if(attaData.getComments().isEmpty() || attaData.getComments() == null || Objects.equals(attaData.getComments(), "null")) {
            holder.commentsTv.setVisibility(View.GONE);
        }else {
            holder.commentsTv.setText(attaData.getComments());
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, totalBagsTv, totalBalesTv, commentsTv;
        ImageView photoIv;

        public ViewHolder(View view) {
            super(view);
            titleTv = view.findViewById(R.id.titleTv);
            totalBagsTv = view.findViewById(R.id.totalBagsTv);
            photoIv = view.findViewById(R.id.photoIv);
            totalBalesTv = view.findViewById(R.id.totalBalesTv);
            commentsTv = view.findViewById(R.id.commentsTv);
        }
    }


    private void showImageDialog(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
        builder.setView(dialogView);

        ImageView fullImageView = dialogView.findViewById(R.id.fullImageView);
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.baseline_document_scanner_24)
                .error(R.drawable.baseline_document_scanner_24)
                .into(fullImageView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Optional: close when user taps the image
        fullImageView.setOnClickListener(v -> dialog.dismiss());
    }
}
