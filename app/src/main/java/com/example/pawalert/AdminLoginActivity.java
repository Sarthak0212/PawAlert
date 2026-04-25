package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_admin_email);
        etPassword = findViewById(R.id.et_admin_password);
        btnLogin = findViewById(R.id.btn_admin_login);
        progressBar = findViewById(R.id.progress_admin_login);

        btnLogin.setOnClickListener(v -> attemptAdminLogin());
    }

    private void attemptAdminLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            AppUtils.showToast(this, "Empty credentials");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> verifyAdminRole(authResult.getUser().getUid()))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    AppUtils.showToast(this, "Login failed: " + e.getMessage());
                });
    }

    private void verifyAdminRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if ("Admin".equalsIgnoreCase(role)) {
                        Intent intent = new Intent(this, AdminHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        mAuth.signOut();
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        AppUtils.showToast(this, "Access Denied: You are not an Admin");
                    }
                })
                .addOnFailureListener(e -> {
                    mAuth.signOut();
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    AppUtils.showToast(this, "Error verifying role");
                });
    }
}
