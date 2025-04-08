package com.example.a1logisticstest1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

public class MerchantDashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_dashboard);

        Map<String, String> user = getCurrentUser();
        if (user == null) {
            logout();
            return;
        }

        // Set welcome message
        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome " + user.get("name"));

        // Initialize buttons
        Button createPickupButton = findViewById(R.id.createPickupButton);
        Button activePackagesButton = findViewById(R.id.activePackagesButton);
        Button deliveredPackagesButton = findViewById(R.id.deliveredPackagesButton);
        Button returnedPackagesButton = findViewById(R.id.returnedPackagesButton);
        Button allPackagesButton = findViewById(R.id.allPackagesButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        // Set click listeners
        createPickupButton.setOnClickListener(v -> {
            startActivity(new Intent(MerchantDashboardActivity.this, CreatePickupActivity.class));
        });

        // In MerchantDashboardActivity.java
        // In MerchantDashboardActivity.java
        activePackagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPackagesActivity.class);
            intent.putExtra("packageType", "active");  // <-- THIS IS CRUCIAL
            startActivity(intent);
        });

        deliveredPackagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPackagesActivity.class);
            intent.putExtra("packageType", "delivered");  // <-- THIS IS CRUCIAL
            startActivity(intent);
        });

        returnedPackagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPackagesActivity.class);
            intent.putExtra("packageType", "returned");  // <-- THIS IS CRUCIAL
            startActivity(intent);
        });

        allPackagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPackagesActivity.class);
            intent.putExtra("packageType", "all");  // <-- THIS IS CRUCIAL
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());
    }
}