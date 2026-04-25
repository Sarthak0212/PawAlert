package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ReporterHomeActivity — Dashboard shown to users with the "Reporter" role.
 * Provides a single primary action: report an animal in distress.
 */
public class ReporterHomeActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private Button   btnReportAnimal;
    private Button   btnLogout;
    private android.widget.ImageButton btnInbox;
    private android.widget.ImageButton btnProfile;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporter_home);

        mAuth = FirebaseAuth.getInstance();

        tvUserEmail     = findViewById(R.id.tv_reporter_user);
        btnReportAnimal = findViewById(R.id.btn_reporter_report_animal);
        btnLogout       = findViewById(R.id.btn_reporter_logout);
        btnInbox        = findViewById(R.id.btn_inbox);
        btnProfile      = findViewById(R.id.btn_profile);

        displayUser();

        btnReportAnimal.setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        btnLogout.setOnClickListener(v -> logout());
        btnInbox.setOnClickListener(v -> startActivity(new Intent(this, InboxActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void displayUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvUserEmail.setText("Signed in as: " + user.getEmail());
        }
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
