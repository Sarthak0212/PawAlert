package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoginActivity — handles Firebase Email/Password authentication.
 * On success, navigates to HomeActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;

    private FirebaseAuth      mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth      = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // If user is already signed in, skip login
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchRoleAndNavigate(currentUser.getUid());
            return;
        }

        bindViews();
        setListeners();
    }

    private void bindViews() {
        etEmail       = findViewById(R.id.et_login_email);
        etPassword    = findViewById(R.id.et_login_password);
        btnLogin      = findViewById(R.id.btn_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- Validation ---
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!AppUtils.isValidEmail(email)) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // --- Firebase Auth ---
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchRoleAndNavigate(uid);
                })
                .addOnFailureListener(e -> {
                    AppUtils.showToast(this, "Login failed: " + e.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                });
    }

    /**
     * Reads the authenticated user's "role" from Firestore users/{uid}
     * and routes to the correct dashboard activity.
     */
    private void fetchRoleAndNavigate(String uid) {
        mFirestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String role = snapshot.getString("role");
                    navigateByRole(role);
                })
                .addOnFailureListener(e -> {
                    // Firestore read failed — safe default: go to reporter home
                    AppUtils.showToast(this, "Could not fetch role, defaulting.");
                    navigateByRole(null);
                });
    }

    private void navigateByRole(String role) {
        Class<?> destination;
        if ("Volunteer".equalsIgnoreCase(role)) {
            destination = VolunteerHomeActivity.class;
        } else if ("Admin".equalsIgnoreCase(role)) {
            mAuth.signOut();
            AppUtils.showToast(this, "Admins must use the Admin Portal.");
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            return;
        } else {
            destination = ReporterHomeActivity.class;
        }
        Intent intent = new Intent(LoginActivity.this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
