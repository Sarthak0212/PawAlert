package com.example.pawalert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.pawalert.utils.DatabaseMigrator;

import androidx.appcompat.app.AppCompatActivity;

public class LoginSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_selection);

        // Run one-time database migration and admin seeding
        DatabaseMigrator.runMigration(this);

        Button btnAdmin = findViewById(R.id.btn_select_admin);
        Button btnUser = findViewById(R.id.btn_select_user);

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
