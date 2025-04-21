package com.example.a1logisticstest1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity implements UpdateChecker.UpdateListener {
    private UIBlocker uiBlocker;
    private UpdateChecker updateChecker; // Declare the variable here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        uiBlocker = new UIBlocker(this);
        uiBlocker.blockUI("Checking for updates...");

        // Initialize the UpdateChecker here
        updateChecker = new UpdateChecker(this, this);
        updateChecker.checkForUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateChecker != null) {
            updateChecker.cleanup(); // Now this will work
        }
    }

    @Override
    public void onUpdateAvailable(String newVersion, String changelog) {
        uiBlocker.showSuccess("New version " + newVersion + " available. Downloading...", () -> {
            proceedToLogin();
        });
    }

    @Override
    public void onUpdateDownloading(int progress) {
        // Optional: Update progress if you implement a progress bar
    }

    @Override
    public void onUpdateDownloaded() {
        Toast.makeText(this, "Update downloaded. Installing...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String message) {
        uiBlocker.showError("Update check failed: " + message);
        new Handler().postDelayed(() -> proceedToLogin(), 2000);
    }

    @Override
    public void onNoUpdateAvailable() {
        proceedToLogin();
    }

    private void proceedToLogin() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 1500);
    }
}