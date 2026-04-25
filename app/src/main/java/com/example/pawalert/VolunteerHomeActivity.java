package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
 
import com.example.pawalert.adapters.ReportAdapter;
import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * VolunteerHomeActivity — Dashboard for Volunteer-role users.
 *
 * Fetches all reports from the Firestore "reports" collection and
 * displays them in a RecyclerView of cards.  Tapping a card opens
 * ReportDetailActivity with the full report data.
 */
public class VolunteerHomeActivity extends AppCompatActivity {

    private RecyclerView  rvReports;
    private ProgressBar   progressBar;
    private TextView      tvEmpty;
    private Button        btnLogout;
    private android.widget.ImageButton btnInbox;
    private android.widget.ImageButton btnProfile;

    private FirebaseAuth      mAuth;
    private FirebaseFirestore mFirestore;

    private final List<Report>  reportList = new ArrayList<>();
    private final Set<String>   rejectedIds = new HashSet<>();
    private ReportAdapter       adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_home);

        mAuth      = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progress_volunteer);
        tvEmpty     = findViewById(R.id.tv_volunteer_empty);
        rvReports   = findViewById(R.id.rv_reports);
        btnLogout   = findViewById(R.id.btn_volunteer_logout);
        btnInbox    = findViewById(R.id.btn_inbox);
        btnProfile  = findViewById(R.id.btn_profile);

        // Set up RecyclerView
        adapter = new ReportAdapter(reportList, new ReportAdapter.OnReportActionListener() {
            @Override
            public void onReportClick(Report report) {
                openReportDetail(report);
            }

            @Override
            public void onAcceptClick(Report report, int position) {
                acceptReport(report);
            }

            @Override
            public void onRejectClick(Report report, int position) {
                rejectReport(report, position);
            }
        });
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logout());
        btnInbox.setOnClickListener(v -> startActivity(new Intent(this, InboxActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        loadReports();
    }

    // ── Firestore ──────────────────────────────────────────────────────────────

    /**
     * Listens to the Firestore "reports" collection in real-time.
     * Only shows OPEN reports, or reports ACCEPTED/IN_PROGRESS/COMPLETED by the current user.
     * Skips reports the user has rejected.
     */
    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        mFirestore.collection("reports")
                .whereIn("status", Arrays.asList(
                        Report.STATUS_OPEN, 
                        Report.STATUS_ACCEPTED, 
                        Report.STATUS_IN_PROGRESS, 
                        Report.STATUS_COMPLETED
                ))
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        AppUtils.showToast(this, "Listen failed: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        reportList.clear();
                        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

                        // Check document changes for notifications
                        for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                Report modifiedReport = parseReport(dc.getDocument());
                                if (modifiedReport != null && 
                                    Report.STATUS_ACCEPTED.equals(modifiedReport.getStatus()) &&
                                    !currentUserId.equals(modifiedReport.getVolunteerId())) {
                                    // It was OPEN, now ACCEPTED by someone else. 
                                    // Since it's no longer OPEN and not ours, it will be removed.
                                    AppUtils.showToast(VolunteerHomeActivity.this, "A rescue was accepted by another volunteer.");
                                }
                            }
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            Report report = parseReport(doc);
                            if (report == null) continue;
                            
                            if (rejectedIds.contains(report.getReportId())) {
                                continue;
                            }

                            if (Report.STATUS_OPEN.equals(report.getStatus())) {
                                reportList.add(report);
                            } else if (currentUserId.equals(report.getVolunteerId())) {
                                reportList.add(report);
                            }
                        }

                        // Sort descending by timestamp (since we can't orderBy easily with whereIn)
                        reportList.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                        if (reportList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void rejectReport(Report report, int position) {
        rejectedIds.add(report.getReportId());
        reportList.remove(position);
        adapter.notifyItemRemoved(position);
        if (reportList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void acceptReport(Report report) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        
        mFirestore.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference docRef = 
                mFirestore.collection("reports").document(report.getReportId());
            
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            String status = snapshot.getString("status");
            
            if (Report.STATUS_OPEN.equals(status)) {
                transaction.update(docRef, "status", Report.STATUS_ACCEPTED);
                transaction.update(docRef, "volunteerId", currentUserId);
                return true;
            } else {
                return false;
            }
        }).addOnSuccessListener(success -> {
            progressBar.setVisibility(View.GONE);
            if (success) {
                AppUtils.showToast(this, "Rescue request accepted!");
            } else {
                AppUtils.showToast(this, "Already accepted by another volunteer.");
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            AppUtils.showToast(this, "Transaction failed: " + e.getMessage());
        });
    }

    /**
     * Manually maps a Firestore document to a {@link Report}.
     * Extracts the nested "location" map for lat/lng.
     */
    @SuppressWarnings("unchecked")
    private Report parseReport(QueryDocumentSnapshot doc) {
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
            android.util.Log.e("VolunteerHome", "Parsing failed for: " + doc.getId(), e);
            return null;
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    private void openReportDetail(Report report) {
        Intent intent = new Intent(this, ReportDetailActivity.class);
        intent.putExtra("reportId",    report.getReportId());
        intent.putExtra("imageBase64", report.getImageBase64());
        intent.putExtra("animalType",  report.getAnimalType());
        intent.putExtra("priority",    report.getPriority());
        intent.putExtra("status",      report.getStatus());
        intent.putExtra("createdBy",   report.getCreatedBy());
        intent.putExtra("lat",         report.getLat());
        intent.putExtra("lng",         report.getLng());
        intent.putExtra("rejectionReason", report.getRejectionReason());
        intent.putExtra("volunteerId",   report.getVolunteerId());
        startActivity(intent);
    }

    private void logout() {
        mAuth.signOut();
        AppUtils.showToast(this, "Logged out");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent back-navigation to login
    }
}
