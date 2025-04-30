package com.example.a1logisticstest1;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final String VERSION_COLLECTION = "app_updates";
    private static final String VERSION_DOCUMENT = "latest_version";
    private static final long MIN_SPLASH_DURATION = 2000;
    private static final int MAX_RETRY_COUNT = 3;

    private FirebaseFirestore db;
    private long startTime;
    private File downloadedApkFile;
    private static SplashActivity instance;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        instance = this;
        startTime = System.currentTimeMillis();

        // Check network before proceeding
        if (!isNetworkAvailable()) {
            showNetworkErrorAndRetry();
            return;
        }

        db = FirebaseFirestore.getInstance();
        checkForUpdates();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNetworkErrorAndRetry() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                checkForUpdates();
            } else {
                proceedToErrorActivity("Network unavailable after multiple retries");
            }
        }, 3000);
    }

    private void checkForUpdates() {
        db.collection(VERSION_COLLECTION)
                .document(VERSION_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            processVersionCheck(document);
                        } else {
                            handleVersionCheckError("Version document not found");
                        }
                    } else {
                        handleVersionCheckError("Error fetching version: " + task.getException());
                    }
                });
    }

    private void processVersionCheck(DocumentSnapshot document) {
        try {
            int latestVersionCode = document.getLong("versionCode").intValue();
            String downloadUrl = document.getString("downloadUrl");
            boolean forceUpdate = Boolean.TRUE.equals(document.getBoolean("forceUpdate"));
            String releaseNotes = document.getString("releaseNotes");
            String checksum = document.getString("checksum");
            int currentVersionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;

            if (latestVersionCode > currentVersionCode) {
                if (forceUpdate) {
                    proceedToUpdateActivity(downloadUrl, releaseNotes, checksum, true);
                } else {
                    proceedToUpdateActivity(downloadUrl, releaseNotes, checksum, false);
                }
            } else {
                proceedAfterMinimumDuration(this::proceedToLogin);
            }
        } catch (Exception e) {
            handleVersionCheckError("Error parsing version data: " + e.getMessage());
        }
    }

    private void handleVersionCheckError(String error) {
        Log.e(TAG, error);
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            new Handler(Looper.getMainLooper()).postDelayed(this::checkForUpdates, 2000);
        } else {
            proceedAfterMinimumDuration(this::proceedToLogin);
        }
    }

    private void proceedAfterMinimumDuration(Runnable action) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = MIN_SPLASH_DURATION - elapsedTime;
        new Handler(Looper.getMainLooper()).postDelayed(action, Math.max(remainingTime, 0));
    }

    private void proceedToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void proceedToErrorActivity(String error) {
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra("error", error);
        startActivity(intent);
        finish();
    }

    private void proceedToUpdateActivity(String downloadUrl, String releaseNotes,
                                         String checksum, boolean isMandatory) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("downloadUrl", downloadUrl);
        intent.putExtra("releaseNotes", releaseNotes);
        intent.putExtra("checksum", checksum);
        intent.putExtra("isMandatory", isMandatory);
        startActivity(intent);
        finish();
    }

    public static void downloadApk(String apkUrl, DownloadCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            FileOutputStream output = null;

            try {
                URL url = new URL(apkUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // Check for valid response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Server returned HTTP " + connection.getResponseCode());
                }

                File apkFile = new File(instance.getExternalFilesDir(null), "update.apk");
                if (apkFile.exists()) {
                    apkFile.delete();
                }

                input = connection.getInputStream();
                output = new FileOutputStream(apkFile);

                byte[] buffer = new byte[4096];
                int count;
                long total = 0;
                int fileLength = connection.getContentLength();

                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    total += count;
                    if (fileLength > 0) {
                        int progress = (int) (total * 100 / fileLength);
                        instance.runOnUiThread(() -> callback.onDownloadProgress(progress));
                    }
                }

                instance.downloadedApkFile = apkFile;
                instance.runOnUiThread(() -> callback.onDownloadComplete(apkFile));

            } catch (IOException e) {
                Log.e(TAG, "Download failed", e);
                instance.runOnUiThread(() -> callback.onDownloadFailed(e.getMessage()));
            } finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException ignored) {}
            }
        }).start();
    }

    public static boolean verifyApkChecksum(File apkFile, String expectedChecksum) {
        if (apkFile == null || !apkFile.exists() || expectedChecksum == null) {
            return false;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream input = new java.io.FileInputStream(apkFile);
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(expectedChecksum.toLowerCase(Locale.US));
        } catch (Exception e) {
            Log.e(TAG, "Checksum verification failed", e);
            return false;
        }
    }

    public static void installApk(File apkFile) {
        if (apkFile != null && apkFile.exists()) {
            try {
                Uri apkUri = FileProvider.getUriForFile(
                        instance,
                        instance.getPackageName() + ".provider",
                        apkFile);

                Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                installIntent.setData(apkUri);
                installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

                instance.startActivity(installIntent);

                // Schedule file cleanup after 60 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                }, 60000);
            } catch (Exception e) {
                Toast.makeText(instance, "Installation failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(instance, "APK file not found", Toast.LENGTH_SHORT).show();
        }
    }

    public interface DownloadCallback {
        void onDownloadProgress(int progress);
        void onDownloadComplete(File apkFile);
        void onDownloadFailed(String error);
    }
}