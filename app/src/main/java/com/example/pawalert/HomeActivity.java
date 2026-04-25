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
 * HomeActivity — placeholder dashboard shown after successful login/registration.
 * Displays the signed-in user's email and provides a logout button.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvHomeUser;
    private Button   btnReportAnimal;
    private Button   btnLogout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        tvHomeUser      = findViewById(R.id.tv_home_user);
        btnReportAnimal = findViewById(R.id.btn_report_animal);
        btnLogout       = findViewById(R.id.btn_logout);

        displayCurrentUser();

        btnReportAnimal.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ReportActivity.class)));

        btnLogout.setOnClickListener(v -> logout());
    }

    private void displayCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvHomeUser.setText("Signed in as: " + user.getEmail());
        }
    }

    private void logout() {
        mAuth.signOut();
        AppUtils.showToast(this, "Logged out");
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Prevent back button from returning to login screen
    @Override
    public void onBackPressed() {
        // Do nothing — user must explicitly logout
    }
}
