package com.example.a1logisticstest1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.firebase.Timestamp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DownloadUtils {
    private static final String TAG = "DownloadUtils";
    private static final String PACKAGES_FOLDER = "Packages";

    // For single package download (horizontal layout)
    public static void downloadPackageDetails(Context context, Map<String, Object> packageData, boolean isAdmin) {
        try {
            if (!checkStoragePermission(context)) return;

            File packagesDir = getPackagesDirectory();
            String fileName = packageData.get("orderId") + ".csv";
            File file = new File(packagesDir, fileName);

            // Define the fields we want to export
            String[][] fields = getFieldsForUser(isAdmin);

            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                writeHeaders(writer, fields);

                // Write values
                writePackageData(writer, fields, packageData, isAdmin);

                notifyFileCreated(file, context);
            }
        } catch (Exception e) {
            handleError(context, "Failed to download package", e);
        }
    }

    // For multiple packages download (table layout)
    public static void downloadPackageList(Context context, List<Map<String, Object>> packages, boolean isAdmin) {
        try {
            if (!checkStoragePermission(context)) return;

            File packagesDir = getPackagesDirectory();
            String fileName = "Bulk-" + packages.size() + "Packages.csv";
            File file = new File(packagesDir, fileName);

            // Define the fields we want to export
            String[][] fields = getFieldsForUser(isAdmin);

            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                writeHeaders(writer, fields);

                // Write data rows
                for (Map<String, Object> pkg : packages) {
                    writePackageData(writer, fields, pkg, isAdmin);
                }

                notifyFileCreated(file, context);
            }
        } catch (Exception e) {
            handleError(context, "Failed to download packages", e);
        }
    }

    private static String[][] getFieldsForUser(boolean isAdmin) {
        if (isAdmin) {
            return new String[][]{
                    {"orderId", "Order ID"},
                    {"customerName", "Customer Name"},
                    {"customerNumber", "Phone"},
                    {"deliveryLocation", "Delivery Location"},
                    {"codPrice", "COD Price"},
                    {"status", "Status"},
                    {"pickupLocation", "Pickup Location"},
                    {"merchantId", "Merchant ID"},
                    {"merchantName", "Merchant Name"},
                    {"packageDetails", "Package Details"},
                    {"packageWeight", "Weight"},
                    {"createdDate", "Created Date"}
            };
        } else {
            return new String[][]{
                    {"orderId", "Order ID"},
                    {"customerName", "Customer Name"},
                    {"customerNumber", "Phone"},
                    {"deliveryLocation", "Delivery Location"},
                    {"codPrice", "COD Price"},
                    {"status", "Status"},
                    {"pickupLocation", "Pickup Location"},
                    {"packageDetails", "Package Details"},
                    {"packageWeight", "Weight"},
                    {"createdDate", "Created Date"}
            };
        }
    }

    private static void writeHeaders(FileWriter writer, String[][] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            writer.append('"').append(fields[i][1]).append('"');
            if (i < fields.length - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");
    }

    private static void writePackageData(FileWriter writer, String[][] fields, Map<String, Object> packageData, boolean isAdmin) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i][0];
            Object value = packageData.get(fieldName);

            // Always wrap value in quotes to treat as text
            writer.append('"');

            // Format special fields
            if ("customerNumber".equals(fieldName)) {
                writer.append(getSafeString(value)); // Phone number as text
            } else if ("codPrice".equals(fieldName)) {
                writer.append("BDT ").append(getSafeString(value)); // COD as text
            } else if ("packageWeight".equals(fieldName)) {
                writer.append(getSafeString(value)).append(" KG"); // Weight as text
            } else if ("createdDate".equals(fieldName)) {
                writer.append(formatDate(value));
            } else {
                writer.append(getSafeString(value));
            }

            writer.append('"');

            if (i < fields.length - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");
    }

    // Rest of the helper methods remain the same...
    private static File getPackagesDirectory() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File packagesDir = new File(downloadsDir, PACKAGES_FOLDER);
        if (!packagesDir.exists()) {
            packagesDir.mkdirs();
        }
        return packagesDir;
    }

    private static boolean checkStoragePermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Storage permission required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (dateObj instanceof Timestamp) {
            return dateFormat.format(((Timestamp) dateObj).toDate());
        } else if (dateObj instanceof Date) {
            return dateFormat.format((Date) dateObj);
        }
        return "N/A";
    }

    private static String getSafeString(Object value) {
        return value != null ? value.toString() : "N/A";
    }

    private static void notifyFileCreated(File file, Context context) {
        MediaScannerConnection.scanFile(context,
                new String[]{file.getAbsolutePath()},
                null,
                (path, uri) -> {
                    Log.d(TAG, "File scanned: " + path);
                    Toast.makeText(context,
                            "File saved to Downloads/" + PACKAGES_FOLDER + "/" + file.getName(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private static void handleError(Context context, String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(context, message + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}