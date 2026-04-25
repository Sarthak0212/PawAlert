package com.example.pawalert.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.R;
import com.example.pawalert.models.Report;

import java.util.List;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.AdminViewHolder> {

    private final List<Report> reports;
    private final OnAdminActionListener listener;

    public interface OnAdminActionListener {
        void onApprove(Report report);
        void onReject(Report report);
    }

    public AdminReportAdapter(List<Report> reports, OnAdminActionListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_report_card, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report, listener);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvAnimalType, tvPriority, tvStatus;
        Button btnApprove, btnReject;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_admin_thumbnail);
            tvAnimalType = itemView.findViewById(R.id.tv_admin_animal_type);
            tvPriority = itemView.findViewById(R.id.tv_admin_priority);
            tvStatus = itemView.findViewById(R.id.tv_admin_status);
            btnApprove = itemView.findViewById(R.id.btn_admin_approve);
            btnReject = itemView.findViewById(R.id.btn_admin_reject);
        }

        public void bind(Report report, OnAdminActionListener listener) {
            tvAnimalType.setText(report.getAnimalType());
            tvPriority.setText(report.getPriority());
            tvStatus.setText("Status: " + report.getStatus());

            if (report.getImageBase64() != null && !report.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(report.getImageBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivThumbnail.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    ivThumbnail.setImageResource(android.R.drawable.ic_menu_camera);
                }
            }

            btnApprove.setOnClickListener(v -> listener.onApprove(report));
            btnReject.setOnClickListener(v -> listener.onReject(report));
            
            // If it's a resolution request, change button text for clarity
            if (Report.STATUS_RESOLVED_PENDING.equals(report.getStatus())) {
                btnApprove.setText("Verify & Resolve");
                btnReject.setText("Send Back");
            } else {
                btnApprove.setText("Approve");
                btnReject.setText("Reject");
            }
        }
    }
}
