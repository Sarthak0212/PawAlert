package com.example.pawalert;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.adapters.NotificationAdapter;
import com.example.pawalert.models.Notification;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private final List<Notification> notificationList = new ArrayList<>();
    
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rv_notifications);
        progressBar = findViewById(R.id.pb_inbox);
        tvEmpty = findViewById(R.id.tv_inbox_empty);
        btnBack = findViewById(R.id.btn_inbox_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, this::markAsRead);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (currentUserId.isEmpty()) {
            AppUtils.showToast(this, "User not authenticated");
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        notification.setNotificationId(doc.getId());
                        notificationList.add(notification);
                    }

                    // Client-side sort: Latest first (Descending)
                    // This serves as a fallback while the Firestore composite index is building
                    java.util.Collections.sort(notificationList, (n1, n2) ->
                            Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                    
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (notificationList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    AppUtils.showToast(this, "Error: " + e.getMessage());
                });
    }

    private void markAsRead(Notification notification) {
        if (notification.isRead()) return; // Already read

        db.collection("notifications").document(notification.getNotificationId())
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> android.util.Log.e("Inbox", "Failed to update mark as read"));
    }
}
