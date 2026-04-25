package com.example.pawalert;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.adapters.AdminReportAdapter;
import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AdminHomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvReports;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnLogout;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AdminReportAdapter adapter;
    private final List<Report> reportList = new ArrayList<>();

    private String currentStatusFilter = Report.STATUS_PENDING_APPROVAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bindViews();
        setupTabs();
        setupRecyclerView();
        setListeners();

        loadReports();
    }

    private void bindViews() {
        tabLayout = findViewById(R.id.tab_layout_admin);
        rvReports = findViewById(R.id.rv_admin_reports);
        progressBar = findViewById(R.id.progress_admin);
        tvEmpty = findViewById(R.id.tv_admin_empty);
        btnLogout = findViewById(R.id.btn_admin_logout);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("New Requests"));
        tabLayout.addTab(tabLayout.newTab().setText("Resolutions"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentStatusFilter = Report.STATUS_PENDING_APPROVAL;
                } else {
                    currentStatusFilter = Report.STATUS_RESOLVED_PENDING;
                }
                loadReports();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminReportAdapter(reportList, new AdminReportAdapter.OnAdminActionListener() {
            @Override
            public void onApprove(Report report) {
                handleApproval(report, true);
            }

            @Override
            public void onReject(Report report) {
                handleApproval(report, false);
            }
        });
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void setListeners() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("reports")
                .whereEqualTo("status", currentStatusFilter)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Report report = parseReport(doc);
                        if (report != null) {
                            reportList.add(report);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    if (reportList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    AppUtils.showToast(this, "Error: " + e.getMessage());
                });
    }

    private void handleApproval(Report report, boolean approved) {
        boolean isResolutionPhase = currentStatusFilter.equals(Report.STATUS_RESOLVED_PENDING);

        if (isResolutionPhase && !approved) {
            // "Send Back" logic - requires a reason
            showRejectionDialog(report);
        } else {
            // Normal Approval/Rejection logic
            performStatusUpdate(report, approved, null);
        }
    }

    private void showRejectionDialog(Report report) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rejection_reason, null);
        EditText etReason = dialogView.findViewById(R.id.et_rejection_reason);

        new AlertDialog.Builder(this)
                .setTitle("Rejection Reason")
                .setMessage("Please provide a reason for sending back this resolution.")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        AppUtils.showToast(this, "Reason is mandatory");
                    } else {
                        performStatusUpdate(report, false, reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performStatusUpdate(Report report, boolean approved, String rejectionReason) {
        String nextStatus;
        boolean isResolutionPhase = currentStatusFilter.equals(Report.STATUS_RESOLVED_PENDING);

        if (currentStatusFilter.equals(Report.STATUS_PENDING_APPROVAL)) {
            nextStatus = approved ? Report.STATUS_OPEN : Report.STATUS_REJECTED;
        } else {
            // Resolution phase
            // Requirement: If rejected (Send Back), status goes to IN_PROGRESS
            nextStatus = approved ? Report.STATUS_COMPLETED : Report.STATUS_IN_PROGRESS;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", nextStatus);
        
        if (rejectionReason != null) {
            updates.put("rejectionReason", rejectionReason);
            updates.put("rejectedAt", System.currentTimeMillis());
        }

        db.collection("reports").document(report.getReportId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    String msg = approved ? "Approved!" : "Sent back to volunteer";
                    AppUtils.showToast(this, msg);

                    if (currentStatusFilter.equals(Report.STATUS_PENDING_APPROVAL)) {
                        if (approved) {
                            notifyAllVolunteers("New Rescue Request", "An animal needs your help!");
                        } else {
                            sendNotification(report.getCreatedBy(), "Request Rejected",
                                    "Your rescue request was rejected.", "REPORT_REJECTED", report.getReportId());
                        }
                    } else if (isResolutionPhase) {
                        if (approved) {
                            sendNotification(report.getVolunteerId(), "Resolution Approved",
                                    "Your rescue work has been approved by admin.", "RESOLUTION_APPROVED", report.getReportId());
                            sendNotification(report.getCreatedBy(), "Animal Rescued",
                                    "Your reported animal has been successfully rescued.", "REPORT_RESOLVED", report.getReportId());
                        } else {
                            // Rejection Notification with reason
                            sendNotification(report.getVolunteerId(), "Resolution Rejected",
                                    "Your submission was rejected. Reason: " + rejectionReason,
                                    "RESOLUTION_REJECTED", report.getReportId());
                        }
                    }
                    loadReports();
                })
                .addOnFailureListener(e -> AppUtils.showToast(this, "Failed: " + e.getMessage()));
    }

    private void sendNotification(String userId, String title, String message, String type, String incidentId) {
        if (userId == null || userId.isEmpty() || incidentId == null) return;

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("incidentId", incidentId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnFailureListener(e -> android.util.Log.e("Notification", "Failed to send: " + e.getMessage()));
    }

    private void notifyAllVolunteers(String title, String message) {
        db.collection("users")
                .whereEqualTo("role", "VOLUNTEER")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            sendNotification(userId, title, message, "NEW_RESCUE", "global");
                        }
                    }
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private Report parseReport(com.google.firebase.firestore.QueryDocumentSnapshot doc) {
        try {
            String reportId    = doc.getId();
            String imageBase64 = doc.getString("imageBase64");
            String animalType  = doc.getString("animalType");
            String priority    = doc.getString("priority");
            String status      = doc.getString("status");
            String createdBy   = doc.getString("createdBy");
            String volunteerId = doc.getString("volunteerId");
            String resDescription = doc.getString("rescueDescription");
            String proofImage  = doc.getString("resolvedProofImage");
            String rejReason   = doc.getString("rejectionReason");

            double lat = 0, lng = 0;
            Object locObj = doc.get("location");
            if (locObj instanceof java.util.Map) {
                java.util.Map<String, Object> locMap = (java.util.Map<String, Object>) locObj;
                if (locMap.get("lat") instanceof Number) lat = ((Number) locMap.get("lat")).doubleValue();
                if (locMap.get("lng") instanceof Number) lng = ((Number) locMap.get("lng")).doubleValue();
            }

            long timestamp = 0;
            Object tsObj = doc.get("timestamp");
            if (tsObj instanceof Number) {
                timestamp = ((Number) tsObj).longValue();
            } else if (tsObj instanceof String) {
                try { timestamp = Long.parseLong((String) tsObj); } catch (Exception ignored) {}
            }

            return new Report(reportId, imageBase64, lat, lng, animalType, priority, 
                              status, createdBy, volunteerId, proofImage, resDescription, rejReason, timestamp);
        } catch (Exception e) {
            android.util.Log.e("AdminHome", "Parsing failed for: " + doc.getId(), e);
            return null;
        }
    }
}
