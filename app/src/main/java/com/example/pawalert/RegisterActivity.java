package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.models.User;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * RegisterActivity — handles Firebase account creation and Firestore profile storage.
 * On success, navigates to HomeActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner  spinnerRole;
    private Button   btnRegister;
    private TextView tvGoToLogin;

    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // Role options shown in the Spinner
    private static final String[] ROLES = {"Volunteer", "Reporter"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        bindViews();
        setupRoleSpinner();
        setListeners();
    }

    private void bindViews() {
        etName       = findViewById(R.id.et_register_name);
        etEmail      = findViewById(R.id.et_register_email);
        etPassword   = findViewById(R.id.et_register_password);
        spinnerRole  = findViewById(R.id.spinner_role);
        btnRegister  = findViewById(R.id.btn_register);
        tvGoToLogin  = findViewById(R.id.tv_go_to_login);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                ROLES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name     = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role     = spinnerRole.getSelectedItem().toString();

        // --- Validation ---
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
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

        // --- Firebase Registration ---
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, name, email, role);
                })
                .addOnFailureListener(e -> {
                    AppUtils.showToast(this, "Registration failed: " + e.getMessage());
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create Account");
                });
    }

    /**
     * Saves the newly registered user's profile to Firestore under "users/{uid}".
     */
    private void saveUserToFirestore(String uid, String name, String email, String role) {
        User user = new User(
                uid,
                name,
                email,
                "",                          // phone — not collected at registration
                role,
                "",                          // fcmToken — will be set later
                System.currentTimeMillis()   // createdAt
        );

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> goToHome())
                .addOnFailureListener(e -> {
                    // Auth account was created but Firestore write failed — still navigate
                    AppUtils.showLongToast(this,
                            "Account created, but profile save failed: " + e.getMessage());
                    goToHome();
                });
    }

    private void goToHome() {
        // Route newly registered user to the correct dashboard by their role
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String role = snapshot.getString("role");
                    Class<?> dest = "Volunteer".equalsIgnoreCase(role)
                            ? VolunteerHomeActivity.class
                            : ReporterHomeActivity.class;
                    Intent intent = new Intent(RegisterActivity.this, dest);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Intent intent = new Intent(RegisterActivity.this, ReporterHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}
