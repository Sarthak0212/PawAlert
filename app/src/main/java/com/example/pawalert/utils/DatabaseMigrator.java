package com.example.pawalert.utils;

import android.content.Context;
import android.util.Log;
import com.example.pawalert.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

/**
 * DatabaseMigrator — One-time utility to ensure the database follows the new structure.
 * 1. Seeds the Super Admin account.
 * 2. Migrates legacy String timestamps to Long (int64).
 */
public class DatabaseMigrator {
    private static final String TAG = "DatabaseMigrator";

    public static void runMigration(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 1. Seed Admin Account
        String adminEmail = "admin@pawalert.com";
        String adminPass = "Admin@123";

        mAuth.createUserWithEmailAndPassword(adminEmail, adminPass)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                Map<String, Object> admin = new HashMap<>();
                admin.put("email", adminEmail);
                admin.put("name", "Super Admin");
                admin.put("role", "ADMIN");
                admin.put("userId", uid);
                admin.put("createdAt", 177523556087L);
                
                db.collection("users").document(uid).set(admin)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Admin seeded successfully in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Firestore admin creation failed", e));
            })
            .addOnFailureListener(e -> Log.d(TAG, "Admin account likely already exists in Auth: " + e.getMessage()));

        // 2. Fix Legacy Report Timestamps & Fields
        db.collection("reports").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, Object> updates = new HashMap<>();
                
                // Migrate Timestamp
                Object ts = doc.get("timestamp");
                if (ts != null && !(ts instanceof Number)) {
                    try {
                        long newTs = Long.parseLong(ts.toString());
                        updates.put("timestamp", newTs);
                    } catch (Exception ignored) {}
                }

                // Add missing rescueDescription field to legacy documents
                if (!doc.contains("rescueDescription")) {
                    updates.put("rescueDescription", "");
                }

                if (!updates.isEmpty()) {
                    db.collection("reports").document(doc.getId()).update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Migrated document: " + doc.getId()));
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Report migration failed", e));
    }
}
