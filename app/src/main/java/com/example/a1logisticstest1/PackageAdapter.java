package com.example.a1logisticstest1;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
    private static final String TAG = "PackageAdapter";
    private final List<Map<String, Object>> packages;
    private final OnPackageClickListener listener;

    public interface OnPackageClickListener {
        void onPackageClick(int position);
    }

    public PackageAdapter(List<Map<String, Object>> packages, OnPackageClickListener listener) {
        this.packages = packages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new PackageViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Map<String, Object> packageData = packages.get(position);

        try {
            holder.orderIdText.setText(getSafeString(packageData, "orderId"));
            holder.customerNameText.setText(getSafeString(packageData, "customerName"));
            holder.customerNumberText.setText(getSafeString(packageData, "customerNumber"));
            holder.deliveryLocationText.setText(getSafeString(packageData, "deliveryLocation"));
            holder.codPriceText.setText(getSafeString(packageData, "codPrice"));
            holder.statusText.setText(getSafeString(packageData, "status"));

            // Format and display dates
            Timestamp createdDate = (Timestamp) packageData.get("createdDate");
            Timestamp lastUpdate = (Timestamp) packageData.get("lastUpdate");

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

            if (createdDate != null) {
                holder.createdDateText.setText("Created: " + sdf.format(createdDate.toDate()));
                holder.createdDateText.setVisibility(View.VISIBLE);
            } else {
                holder.createdDateText.setVisibility(View.GONE);
            }

            if (lastUpdate != null) {
                holder.lastUpdateText.setText("Updated: " + sdf.format(lastUpdate.toDate()));
                holder.lastUpdateText.setVisibility(View.VISIBLE);
            } else {
                holder.lastUpdateText.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding package data", e);
        }
    }

    private String getSafeString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "N/A";
        }
        return map.get(key).toString();
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, customerNameText, customerNumberText,
                deliveryLocationText, codPriceText, statusText,
                createdDateText, lastUpdateText;

        public PackageViewHolder(@NonNull View itemView, OnPackageClickListener listener) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            customerNumberText = itemView.findViewById(R.id.customerNumberText);
            deliveryLocationText = itemView.findViewById(R.id.deliveryLocationText);
            codPriceText = itemView.findViewById(R.id.codPriceText);
            statusText = itemView.findViewById(R.id.statusText);
            createdDateText = itemView.findViewById(R.id.createdDateText);
            lastUpdateText = itemView.findViewById(R.id.lastUpdateText);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPackageClick(getAdapterPosition());
                }
            });
        }
    }
}