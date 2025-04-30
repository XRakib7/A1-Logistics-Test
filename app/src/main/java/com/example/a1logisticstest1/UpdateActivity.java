package com.example.a1logisticstest1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity implements SplashActivity.DownloadCallback {
    private ProgressBar progressBar;
    private ProgressBar circularProgress;
    private Button downloadButton;
    private Button installButton;
    private TextView statusText;
    private TextView progressText;
    private TextView releaseNotesText;
    private MaterialCardView updateCard;
    private String downloadUrl;
    private String expectedChecksum;
    private File downloadedApk;
    private boolean isMandatory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        circularProgress = findViewById(R.id.circularProgress);
        downloadButton = findViewById(R.id.downloadButton);
        installButton = findViewById(R.id.installButton);
        statusText = findViewById(R.id.statusText);
        progressText = findViewById(R.id.progressText);
        releaseNotesText = findViewById(R.id.releaseNotesText);
        updateCard = findViewById(R.id.updateCard);

        // Get intent extras
        downloadUrl = getIntent().getStringExtra("downloadUrl");
        expectedChecksum = getIntent().getStringExtra("checksum");
        isMandatory = getIntent().getBooleanExtra("isMandatory", false);
        String releaseNotes = getIntent().getStringExtra("releaseNotes");

        // Setup UI
        setupUI(releaseNotes);

        // Set click listeners
        downloadButton.setOnClickListener(v -> startDownload());
        installButton.setOnClickListener(v -> {
            if (downloadedApk != null && SplashActivity.verifyApkChecksum(downloadedApk, expectedChecksum)) {
                SplashActivity.installApk(downloadedApk);
            } else {
                statusText.setText("APK verification failed. Please download again.");
                downloadButton.setEnabled(true);
                installButton.setEnabled(false);
            }
        });

        // For mandatory updates, disable back button
        if (isMandatory) {
            // Optional: You can also finish all previous activities
        }
    }

    private void setupUI(String releaseNotes) {
        // Customize based on mandatory/optional update
        if (isMandatory) {
            updateCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green));
            downloadButton.setText("Download Now");
            findViewById(R.id.cancelButton).setVisibility(View.GONE);
        } else {
            updateCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.optional_update));
            downloadButton.setText("Download Update");
            findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
            findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
        }

        releaseNotesText.setText(releaseNotes != null ? releaseNotes : "No release notes available");
        installButton.setEnabled(false);
        progressBar.setVisibility(View.GONE);
        circularProgress.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
    }

    private void startDownload() {
        downloadButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        circularProgress.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        statusText.setText("Downloading update...");
        SplashActivity.downloadApk(downloadUrl, this);
    }

    @Override
    public void onDownloadProgress(int progress) {
        progressBar.setProgress(progress);
        circularProgress.setProgress(progress);
        progressText.setText(String.format(Locale.getDefault(), "%d%%", progress));
    }

    @Override
    public void onDownloadComplete(File apkFile) {
        downloadedApk = apkFile;
        progressBar.setVisibility(View.GONE);
        circularProgress.setVisibility(View.GONE);
        statusText.setText("Download complete!");
        progressText.setText("Verifying...");

        // Verify checksum in background
        new Thread(() -> {
            boolean verified = SplashActivity.verifyApkChecksum(apkFile, expectedChecksum);
            runOnUiThread(() -> {
                if (verified) {
                    progressText.setVisibility(View.GONE);
                    installButton.setEnabled(true);
                } else {
                    statusText.setText("APK verification failed");
                    progressText.setText("Please try again");
                    downloadButton.setEnabled(true);
                }
            });
        }).start();
    }

    @Override
    public void onDownloadFailed(String error) {
        progressBar.setVisibility(View.GONE);
        circularProgress.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        statusText.setText("Download failed: " + error);
        downloadButton.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (!isMandatory) {
            super.onBackPressed();
        }
        // For mandatory updates, don't allow back press
    }
}