package com.example.a1logisticstest1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.encoders.json.BuildConfig;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int INSTALL_PERMISSION_CODE = 102;
    private static final String TAG = "SplashActivity";
    private static final String UPDATE_FOLDER = "app_updates";
    private static final String PREFS_NAME = "UpdatePrefs";
    private static final String APK_NAME_KEY = "apk_name";
    private static final String APK_SIZE_KEY = "apk_size";
    private static final String APK_PATH_KEY = "apk_path";

    private boolean updateCheckComplete = false;
    private boolean permissionsGranted = false;
    private boolean updateAvailable = false;
    private String apkDownloadUrl;
    private String apkFileName;
    private long apkFileSize;
    private int latestVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.playAnimation();

        // Check permissions first
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        STORAGE_PERMISSION_CODE);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    !getPackageManager().canRequestPackageInstalls()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                        INSTALL_PERMISSION_CODE);
            } else {
                permissionsGranted = true;
                checkForUpdates();
            }
        } else {
            permissionsGranted = true;
            checkForUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!getPackageManager().canRequestPackageInstalls()) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                                INSTALL_PERMISSION_CODE);
                        return;
                    }
                }
                permissionsGranted = true;
                checkForUpdates();
            } else {
                // Permission denied, proceed without update capability
                proceedToLogin();
            }
        } else if (requestCode == INSTALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true;
                checkForUpdates();
            } else {
                // Permission denied, proceed without update capability
                proceedToLogin();
            }
        }
    }

    private void checkForUpdates() {
        cleanUpOldApks();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("app_updates").document("latest_version")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            latestVersionCode = document.getLong("versionCode").intValue();
                            apkDownloadUrl = document.getString("downloadUrl");

                            // Extract filename from URL
                            apkFileName = apkDownloadUrl.substring(apkDownloadUrl.lastIndexOf('/') + 1);

                            // Get file size from server
                            getApkFileSize(apkDownloadUrl, size -> {
                                apkFileSize = size;

                                int currentVersionCode = BuildConfig.VERSION_CODE;
                                boolean forceUpdate = document.getBoolean("forceUpdate") != null
                                        && document.getBoolean("forceUpdate");

                                updateAvailable = latestVersionCode > currentVersionCode;

                                if (updateAvailable && forceUpdate) {
                                    checkForExistingApk();
                                } else {
                                    proceedToLogin();
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Failed to get version info", task.getException());
                        proceedToLogin();
                    }
                });
    }
    private void getApkFileSize(String fileUrl, FileSizeCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    long fileSize = connection.getContentLengthLong();
                    runOnUiThread(() -> callback.onSizeReceived(fileSize));
                } else {
                    runOnUiThread(() -> callback.onSizeReceived(-1));
                }
            } catch (IOException e) {
                runOnUiThread(() -> callback.onSizeReceived(-1));
            }
        }).start();
    }

    interface FileSizeCallback {
        void onSizeReceived(long size);
    }

    private void cleanUpOldApks() {
        File updateDir = new File(getExternalFilesDir(null), UPDATE_FOLDER);
        if (updateDir.exists()) {
            File[] files = updateDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".apk")) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "Deleted old APK: " + file.getName() + " - " + deleted);
                    }
                }
            }
        }
    }

    private void proceedAfterUpdateCheck() {
        if (!updateCheckComplete || !permissionsGranted) return;

        if (updateAvailable) {
            checkForExistingApk();
        } else {
            proceedToLogin();
        }
    }

    private void checkForExistingApk() {
        // Check SharedPreferences for existing download info
        Map<String, ?> prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getAll();
        String savedApkName = (String) prefs.get(APK_NAME_KEY);
        long savedApkSize = prefs.get(APK_SIZE_KEY) != null ? (Long) prefs.get(APK_SIZE_KEY) : 0;
        String savedApkPath = (String) prefs.get(APK_PATH_KEY);

        if (savedApkName != null && savedApkPath != null && savedApkSize > 0) {
            // Verify the file exists and matches expected name and size
            File apkFile = new File(savedApkPath);
            if (apkFile.exists() &&
                    apkFile.getName().equals(apkFileName) &&
                    apkFile.length() == apkFileSize) {

                // Valid APK exists, go to update activity with install option
                goToUpdateActivity(savedApkPath, true);
                return;
            } else {
                // Invalid file, clear preferences
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            }
        }

        // No valid APK exists, need to download
        goToUpdateActivity(apkDownloadUrl, false);
    }

    private void goToUpdateActivity(String apkPathOrUrl, boolean isApkDownloaded) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("apk_path_or_url", apkPathOrUrl);
        intent.putExtra("is_downloaded", isApkDownloaded);
        intent.putExtra("apk_name", apkFileName);
        intent.putExtra("apk_size", apkFileSize);
        startActivity(intent);
        finish();
    }

    private void proceedToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public static void installApk(File apkFile, AppCompatActivity activity) {
        Uri apkUri = FileProvider.getUriForFile(
                activity,
                activity.getPackageName() + ".provider",
                apkFile);

        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.setData(apkUri);
        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        activity.startActivity(installIntent);

        // Save installation time to clean up later
        activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putLong("last_install_time", System.currentTimeMillis())
                .apply();
    }

    public static void downloadApk(String apkUrl, String fileName, long expectedSize, DownloadCallback callback, AppCompatActivity activity) {
        new Thread(() -> {
            try {
                File updateDir = new File(activity.getExternalFilesDir(null), UPDATE_FOLDER);
                if (!updateDir.exists()) {
                    updateDir.mkdirs();
                }

                File outputFile = new File(updateDir, fileName);
                URL url = new URL(apkUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    callback.onError("Server returned HTTP " + connection.getResponseCode());
                    return;
                }

                try (InputStream input = connection.getInputStream();
                     FileOutputStream output = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                    }
                }

                // Verify file size
                if (outputFile.length() != expectedSize) {
                    outputFile.delete();
                    callback.onError("File size mismatch");
                    return;
                }

                // Save download info to SharedPreferences
                Map<String, Object> prefs = new HashMap<>();
                prefs.put(APK_NAME_KEY, fileName);
                prefs.put(APK_SIZE_KEY, expectedSize);
                prefs.put(APK_PATH_KEY, outputFile.getAbsolutePath());

                activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                        .putString(APK_NAME_KEY, fileName)
                        .putLong(APK_SIZE_KEY, expectedSize)
                        .putString(APK_PATH_KEY, outputFile.getAbsolutePath())
                        .apply();

                callback.onSuccess(outputFile);
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public interface DownloadCallback {
        void onSuccess(File file);
        void onError(String error);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        if (loadingAnimation != null) {
            loadingAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        if (loadingAnimation != null) {
            loadingAnimation.resumeAnimation();
        }

        // Check if we're returning from installation
        checkInstallationStatus();
    }

    private void checkInstallationStatus() {
        long lastInstallTime = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong("last_install_time", 0);

        if (lastInstallTime > 0) {
            // Clear the installed APK
            Map<String, ?> prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getAll();
            String apkPath = (String) prefs.get(APK_PATH_KEY);
            if (apkPath != null) {
                File apkFile = new File(apkPath);
                if (apkFile.exists()) {
                    apkFile.delete();
                }
            }

            // Clear installation record
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .remove("last_install_time")
                    .remove(APK_NAME_KEY)
                    .remove(APK_SIZE_KEY)
                    .remove(APK_PATH_KEY)
                    .apply();
        }
    }
}