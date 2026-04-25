package com.example.pawalert.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.R;
import com.example.pawalert.models.Alert;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of stray animal alerts.
 *
 * This is a scaffold — wire it to a real layout (item_alert.xml) when
 * you build the alert list screen.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final Context context;
    private List<Alert> alertList;
    private OnAlertClickListener listener;

    /** Callback interface for item click events. */
    public interface OnAlertClickListener {
        void onAlertClick(Alert alert, int position);
    }

    public AlertAdapter(Context context, List<Alert> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    /** Attach a click listener to handle alert item taps. */
    public void setOnAlertClickListener(OnAlertClickListener listener) {
        this.listener = listener;
    }

    /** Replace the dataset and refresh the list. */
    public void updateData(List<Alert> newAlerts) {
        this.alertList = newAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: Replace R.layout.item_alert with your actual item layout once created
        // For now this uses a simple built-in Android list item layout as a placeholder
        View view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);
        holder.tvTitle.setText(alert.getAnimalType() + " — " + alert.getStatus());
        holder.tvSubtitle.setText(alert.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlertClick(alert, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }

    /** ViewHolder that holds references to item views. */
    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs match android.R.layout.simple_list_item_2
            tvTitle = itemView.findViewById(android.R.id.text1);
            tvSubtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
