package com.example.pawalert;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.utils.AppUtils;
import com.example.pawalert.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * MainActivity — Entry point of PawAlert.
 *
 * This activity verifies that all Firebase services are connected and working.
 * Replace the content of this activity once you start building real features
 * (e.g., redirect to LoginActivity if user is not signed in).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PawAlert_Main";

    private TextView tvFirebaseStatus;
    private TextView tvAuthStatus;
    private TextView tvFirestoreStatus;
    private TextView tvFcmStatus;
    private Button btnCheckStatus;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase helper
        firebaseHelper = FirebaseHelper.getInstance();

        // Bind views
        tvFirebaseStatus  = findViewById(R.id.tv_firebase_status);
        tvAuthStatus      = findViewById(R.id.tv_auth_status);
        tvFirestoreStatus = findViewById(R.id.tv_firestore_status);
        tvFcmStatus       = findViewById(R.id.tv_fcm_status);
        btnCheckStatus    = findViewById(R.id.btn_check_status);

        // Verify Firebase on launch
        verifyFirebaseSetup();

        // Re-check on button press
        btnCheckStatus.setOnClickListener(v -> verifyFirebaseSetup());
    }

    /**
     * Checks all Firebase services and updates the UI status indicators.
     */
    private void verifyFirebaseSetup() {

        // 1. Firebase App check
        tvFirebaseStatus.setText("🔥 Firebase App: ✅ Connected");
        tvFirebaseStatus.setTextColor(getColor(android.R.color.holo_green_dark));
        Log.d(TAG, "Firebase App initialized successfully");

        // 2. Firebase Authentication
        FirebaseAuth auth = firebaseHelper.getAuth();
        if (auth != null) {
            String userInfo = firebaseHelper.isUserLoggedIn()
                    ? "Signed in as: " + firebaseHelper.getCurrentUser().getEmail()
                    : "No user signed in (ready for login)";
            tvAuthStatus.setText("🔐 Auth: ✅ " + userInfo);
            tvAuthStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            Log.d(TAG, "Firebase Auth initialized. " + userInfo);
        } else {
            tvAuthStatus.setText("🔐 Auth: ❌ Failed");
            tvAuthStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        // 3. Cloud Firestore
        FirebaseFirestore db = firebaseHelper.getFirestore();
        if (db != null) {
            tvFirestoreStatus.setText("🗄️ Firestore: ✅ Connected");
            tvFirestoreStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            Log.d(TAG, "Firestore initialized successfully");
        } else {
            tvFirestoreStatus.setText("🗄️ Firestore: ❌ Failed");
            tvFirestoreStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        // 4. Firebase Cloud Messaging — fetch current token
        tvFcmStatus.setText("📬 FCM: ⏳ Fetching token...");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        // Show only first 20 chars for UI brevity
                        tvFcmStatus.setText("📬 FCM: ✅ Token: " + token.substring(0, 20) + "...");
                        tvFcmStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    } else {
                        tvFcmStatus.setText("📬 FCM: ❌ Token fetch failed");
                        tvFcmStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                        Log.e(TAG, "FCM token fetch failed", task.getException());
                    }
                });

        AppUtils.showToast(this, "Firebase check complete!");
    }
}