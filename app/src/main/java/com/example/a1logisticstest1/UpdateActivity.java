package com.example.a1logisticstest1;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class UpdateActivity extends AppCompatActivity implements SplashActivity.DownloadCallback {

    private String apkPathOrUrl;
    private boolean isDownloaded;
    private String apkFileName;
    private long apkFileSize;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        apkPathOrUrl = getIntent().getStringExtra("apk_path_or_url");
        isDownloaded = getIntent().getBooleanExtra("is_downloaded", false);
        apkFileName = getIntent().getStringExtra("apk_name");
        apkFileSize = getIntent().getLongExtra("apk_size", 0);

        TextView updateText = findViewById(R.id.update_text);
        Button updateButton = findViewById(R.id.update_button);

        if (isDownloaded) {
            updateText.setText("An update is ready to install");
            updateButton.setText("Install Now");
        } else {
            updateText.setText("A new version is available. Please download and install.");
            updateButton.setText("Download Update");
        }

        updateButton.setOnClickListener(v -> {
            if (isDownloaded) {
                File apkFile = new File(apkPathOrUrl);
                SplashActivity.installApk(apkFile, this);
            } else {
                downloadUpdate();
            }
        });
    }

    private void downloadUpdate() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading update...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SplashActivity.downloadApk(apkPathOrUrl, apkFileName, apkFileSize, this, this);
    }

    @Override
    public void onSuccess(File file) {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            isDownloaded = true;
            apkPathOrUrl = file.getAbsolutePath();

            Button updateButton = findViewById(R.id.update_button);
            updateButton.setText("Install Now");
            TextView updateText = findViewById(R.id.update_text);
            updateText.setText("Update downloaded. Ready to install.");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, "Download failed: " + error, Toast.LENGTH_LONG).show();
        });
    }
}