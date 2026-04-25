package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole, tvTotal, tvActive, tvCompleted;
    private ImageButton btnBack;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvRole = findViewById(R.id.tv_profile_role);
        tvTotal = findViewById(R.id.tv_stat_total);
        tvActive = findViewById(R.id.tv_stat_active);
        tvCompleted = findViewById(R.id.tv_stat_completed);
        btnBack = findViewById(R.id.btn_profile_back);
        btnLogout = findViewById(R.id.btn_profile_logout);

        loadUserData();

        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        tvEmail.setText(user.getEmail());
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");

        // Fetch User role from "users" collection
        mFirestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        tvRole.setText(role != null ? role : "REPORTER");
                        fetchStats(user.getUid(), role);
                    }
                });
    }

    private void fetchStats(String uid, String role) {
        String field = "REPORTER".equals(role) ? "createdBy" : "volunteerId";
        
        mFirestore.collection("reports")
                .whereEqualTo(field, uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int active = 0;
                    int completed = 0;

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if (Report.STATUS_RESOLVED.equals(status)) {
                            completed++;
                        } else {
                            active++;
                        }
                    }

                    tvTotal.setText(String.valueOf(total));
                    tvActive.setText(String.valueOf(active));
                    tvCompleted.setText(String.valueOf(completed));
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
