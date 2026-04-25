package com.example.pawalert.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.R;
import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

/**
 * ReportAdapter — RecyclerView adapter for the Volunteer dashboard.
 *
 * Each card shows:
 *  • Full-width thumbnail decoded from Base64
 *  • Animal type, priority badge, location
 *  • Status Spinner (Pending / In Progress / Resolved) — updates Firestore on change
 *  • "Get Directions" button — opens Google Maps navigation to the incident
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    /** The allowed status values for volunteers. */
    private static final List<String> STATUS_OPTIONS =
            Arrays.asList(
                    Report.STATUS_ACCEPTED,
                    Report.STATUS_IN_PROGRESS,
                    Report.STATUS_COMPLETED
            );

    public interface OnReportActionListener {
        void onReportClick(Report report);
        void onAcceptClick(Report report, int position);
        void onRejectClick(Report report, int position);
    }

    private final List<Report>           reports;
    private final OnReportActionListener listener;
    private final FirebaseFirestore      firestore;

    public ReportAdapter(List<Report> reports, OnReportActionListener listener) {
        this.reports   = reports;
        this.listener  = listener;
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report  report  = reports.get(position);
        Context context = holder.itemView.getContext();

        // ── Animal type ────────────────────────────────────────────────────────
        holder.tvAnimalType.setText(report.getAnimalType() != null ? report.getAnimalType() : "—");

        // ── Priority badge ─────────────────────────────────────────────────────
        String priority = report.getPriority();
        holder.tvPriority.setText(priority != null ? priority : "");
        if ("HIGH".equalsIgnoreCase(priority)) {
            holder.tvPriority.setBackgroundResource(R.drawable.badge_high);
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            holder.tvPriority.setBackgroundResource(R.drawable.badge_medium);
        } else if ("LOW".equalsIgnoreCase(priority)) {
            holder.tvPriority.setBackgroundResource(R.drawable.badge_low);
        } else {
            holder.tvPriority.setBackgroundResource(R.drawable.badge_normal);
        }

        // ── Location ───────────────────────────────────────────────────────────
        holder.tvLocation.setText(
                String.format("📍 %.5f, %.5f", report.getLat(), report.getLng()));

        // ── Thumbnail ──────────────────────────────────────────────────────────
        decodeBase64IntoImageView(report.getImageBase64(), holder.ivThumbnail);

        // ── Toggle Visibility based on Status ──────────────────────────────────
        if (Report.STATUS_OPEN.equals(report.getStatus())) {
            holder.llStatusContainer.setVisibility(View.GONE);
            holder.llActionButtons.setVisibility(View.VISIBLE);
        } else {
            holder.llStatusContainer.setVisibility(View.VISIBLE);
            holder.llActionButtons.setVisibility(View.GONE);
            setupStatusSpinner(holder, report, context);
        }

        // ── Action Buttons ─────────────────────────────────────────────────────
        holder.btnAccept.setOnClickListener(v -> listener.onAcceptClick(report, holder.getAdapterPosition()));
        holder.btnReject.setOnClickListener(v -> listener.onRejectClick(report, holder.getAdapterPosition()));

        // ── Get Directions button ──────────────────────────────────────────────
        holder.btnDirections.setOnClickListener(v ->
                openGoogleMapsDirections(context, report.getLat(), report.getLng()));

        // ── Whole-card click → detail screen ──────────────────────────────────
        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() { return reports.size(); }

    // ── Status Spinner ─────────────────────────────────────────────────────────

    /**
     * Populates the status spinner and attaches a listener that writes the
     * selected value back to Firestore when the volunteer changes it.
     *
     * We disable the listener before setting the selection programmatically
     * (via a tag trick) to avoid spurious Firestore writes on bind.
     */
    private void setupStatusSpinner(@NonNull ReportViewHolder holder,
                                    Report report,
                                    Context context) {

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, STATUS_OPTIONS);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(spinnerAdapter);

        // Determine current selection index
        String currentStatus = report.getStatus();
        int selectedIndex = 0;  // default: Pending
        for (int i = 0; i < STATUS_OPTIONS.size(); i++) {
            if (STATUS_OPTIONS.get(i).equalsIgnoreCase(currentStatus)) {
                selectedIndex = i;
                break;
            }
        }

        // Tag = true means "programmatic set — skip Firestore write"
        holder.spinnerStatus.setTag(true);
        holder.spinnerStatus.setSelection(selectedIndex, false);

        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Skip the programmatic selection triggered during bind
                if (Boolean.TRUE.equals(holder.spinnerStatus.getTag())) {
                    holder.spinnerStatus.setTag(false);
                    return;
                }

                String newStatus = STATUS_OPTIONS.get(pos);

                // Optimistic UI update
                report.setStatus(newStatus);

                // Persist to Firestore
                firestore.collection("reports")
                        .document(report.getReportId())
                        .update("status", newStatus)
                        .addOnSuccessListener(unused ->
                                AppUtils.showToast(context,
                                        "Status updated to " + newStatus))
                        .addOnFailureListener(e -> {
                            AppUtils.showToast(context,
                                    "Failed to update status: " + e.getMessage());
                            // Revert the in-memory model so the UI stays consistent
                            report.setStatus(currentStatus);
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });
    }

    // ── Google Maps ────────────────────────────────────────────────────────────

    /**
     * Opens Google Maps navigation to the given latitude/longitude.
     * Uses the standard directions URI that works on all Android devices.
     */
    private void openGoogleMapsDirections(Context context, double lat, double lng) {
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");   // prefer Maps app
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Fall back to browser if Maps app is not installed
            intent.setPackage(null);
            context.startActivity(intent);
        }
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────────

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView  tvAnimalType;
        TextView  tvPriority;
        TextView  tvLocation;
        Spinner   spinnerStatus;
        Button    btnDirections;
        View      llStatusContainer;
        View      llActionButtons;
        Button    btnAccept;
        Button    btnReject;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail       = itemView.findViewById(R.id.iv_card_thumbnail);
            tvAnimalType      = itemView.findViewById(R.id.tv_card_animal_type);
            tvPriority        = itemView.findViewById(R.id.tv_card_priority);
            tvLocation        = itemView.findViewById(R.id.tv_card_location);
            spinnerStatus     = itemView.findViewById(R.id.spinner_status);
            btnDirections     = itemView.findViewById(R.id.btn_get_directions);
            llStatusContainer = itemView.findViewById(R.id.ll_status_container);
            llActionButtons   = itemView.findViewById(R.id.ll_action_buttons);
            btnAccept         = itemView.findViewById(R.id.btn_accept);
            btnReject         = itemView.findViewById(R.id.btn_reject);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Decodes a Base64 string and sets the resulting Bitmap on the ImageView.
     * No-ops safely if the string is null, empty, or corrupt.
     */
    private void decodeBase64IntoImageView(String base64, ImageView imageView) {
        if (base64 == null || base64.isEmpty()) return;
        try {
            byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
