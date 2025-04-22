package com.example.a1logisticstest1;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Build;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.util.Map;

public class UpdateChecker {
    private static final String TAG = "UpdateChecker";
    private final Context context;
    private final FirebaseFirestore db;
    private final UpdateListener listener;
    private long downloadId;
    private BroadcastReceiver onDownloadComplete;

    public interface UpdateListener {
        void onUpdateAvailable(String newVersion, String changelog);
        void onUpdateDownloading(int progress);
        void onUpdateDownloaded();
        void onError(String message);
        void onNoUpdateAvailable();
    }

    public UpdateChecker(Context context, UpdateListener listener) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
    }

    // Only showing the modified parts of UpdateChecker.java

    public void checkForUpdates() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int currentVersion = pInfo.versionCode;

            db.collection("app_updates")
                    .document("latest_version")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = task.getResult().getData();
                            if (data != null) {
                                int latestVersion = ((Long) data.get("version_code")).intValue();
                                String changelog = (String) data.get("changelog");
                                String apkUrl = (String) data.get("apk_url");
                                boolean isMandatory = data.containsKey("is_mandatory") && (boolean) data.get("is_mandatory");

                                if (latestVersion > currentVersion) {
                                    if (isMandatory) {
                                        listener.onUpdateAvailable(
                                                (String) data.get("version_name"),
                                                changelog
                                        );
                                    } else {
                                        // Optional update - proceed to app
                                        listener.onNoUpdateAvailable();
                                    }
                                } else {
                                    listener.onNoUpdateAvailable();
                                }
                            } else {
                                listener.onError("No update data found in Firestore");
                            }
                        } else {
                            listener.onError("Failed to check for updates: " + task.getException().getMessage());
                        }
                    });
        } catch (PackageManager.NameNotFoundException e) {
            listener.onError("Could not get current version: " + e.getMessage());
            Log.e(TAG, "Package name not found", e);
        }
    }

    private void downloadUpdate(String apkUrl) {
        try {
            String fileName = "A1Logistics_update.apk";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File apkFile = new File(downloadsDir, fileName);

            if (apkFile.exists()) {
                apkFile.delete();
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl))
                    .setTitle("A1 Logistics Update")
                    .setDescription("Downloading new version")
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = dm.enqueue(request);

            // Register receiver to track download completion
            onDownloadComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long receivedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (receivedDownloadId == downloadId) {
                        listener.onUpdateDownloaded();
                        installApk(fileName);
                        try {
                            context.unregisterReceiver(this);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Receiver already unregistered", e);
                        }
                    }
                }
            };

            // Register receiver with proper export flag
            onDownloadComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long receivedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (receivedDownloadId == downloadId) {
                        listener.onUpdateDownloaded();
                        installApk(fileName);
                        try {
                            context.unregisterReceiver(this);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Receiver already unregistered", e);
                        }
                    }
                }
            };

            // Android 13+ requires explicit export flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                        onDownloadComplete,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        Context.RECEIVER_NOT_EXPORTED  // Critical fix!
                );
            } else {
                // Pre-Android 13 behavior (no flag needed)
                context.registerReceiver(
                        onDownloadComplete,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                );
            }

        } catch (Exception e) {
            listener.onError("Download failed: " + e.getMessage());
            Log.e(TAG, "Download failed", e);
        }
    }


    private void installApk(String fileName) {
        File apkFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
        );

        if (apkFile.exists()) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    apkFile
            );

            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(install);
            } catch (ActivityNotFoundException e) {
                listener.onError("Could not open installer. Please enable 'Unknown Sources' in settings.");
            }
        } else {
            listener.onError("Downloaded file not found");
        }
    }

    // Add this method to clean up the receiver when done
    public void cleanup() {
        try {
            if (onDownloadComplete != null) {
                context.unregisterReceiver(onDownloadComplete);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver was not registered", e);
        }
    }
}