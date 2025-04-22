package com.example.a1logisticstest1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity implements UpdateChecker.UpdateListener {
    private UpdateChecker updateChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        updateChecker = new UpdateChecker(this, this);
        updateChecker.checkForUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateChecker != null) {
            updateChecker.cleanup();
        }
    }

    @Override
    public void onUpdateAvailable(String newVersion, String changelog) {
        // Start UpdateActivity when update is available
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("newVersion", newVersion);
        intent.putExtra("changelog", changelog);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateDownloading(int progress) {
        // Not used here, handled in UpdateActivity
    }

    @Override
    public void onUpdateDownloaded() {
        // Not used here, handled in UpdateActivity
    }

    @Override
    public void onError(String message) {
        // Show error and proceed (you might want to handle this differently)
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