package com.fahmy.countapp.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.fahmy.countapp.Data.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ProductAutoCompleteAdapter extends ArrayAdapter<Product> {
    private final List<Product> originalList;
    private final List<Product> filteredList;

    public ProductAutoCompleteAdapter(Context context, int resource, List<Product> products) {
        super(context, resource, new ArrayList<>(products));
        this.originalList = new ArrayList<>(products);
        this.filteredList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Product getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Product> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(originalList);
            } else {
                String q = constraint.toString().toLowerCase(Locale.ROOT);
                for (Product p : originalList) {
                    // match against the display string (toString) or name if you prefer
                    if (p.toString().toLowerCase(Locale.ROOT).contains(q)) {
                        suggestions.add(p);
                    }
                }
            }

            // dedupe by display string while preserving order
            LinkedHashMap<String, Product> dedup = new LinkedHashMap<>();
            for (Product p : suggestions) {
                dedup.put(p.toString(), p);
            }

            List<Product> unique = new ArrayList<>(dedup.values());
            results.values = unique;
            results.count = unique.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            if (results != null && results.count > 0) {
                //noinspection unchecked
                filteredList.addAll((List<Product>) results.values);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}

