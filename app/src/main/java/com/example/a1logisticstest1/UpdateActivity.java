// UpdateActivity.java
package com.example.a1logisticstest1;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateActivity extends AppCompatActivity implements UpdateChecker.UpdateListener {
    private TextView statusText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update); // You'll need to create this layout

        statusText = findViewById(R.id.status_text);
        progressBar = findViewById(R.id.progress_bar);

        UpdateChecker updateChecker = new UpdateChecker(this, this);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onUpdateAvailable(String newVersion, String changelog) {
        statusText.setText("New version " + newVersion + " available. Downloading...");
    }

    @Override
    public void onUpdateDownloading(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public void onUpdateDownloaded() {
        statusText.setText("Update downloaded. Installing...");
    }

    @Override
    public void onError(String message) {
        statusText.setText("Error: " + message);
        // Maybe add a retry button here
    }

    @Override
    public void onNoUpdateAvailable() {
        // This shouldn't happen as we only come here when update is required
        finish();
    }
}