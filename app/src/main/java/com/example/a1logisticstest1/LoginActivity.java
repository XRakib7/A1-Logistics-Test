package com.example.a1logisticstest1;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
// Add these imports
import android.content.SharedPreferences;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.Manifest;
import com.example.a1logisticstest1.UIBlocker;
/*
 * This file is part of [A1 Logistics 2025]
 * Copyright (c) [RAKIB]
 *
 * Licensed for personal, non-commercial use only.
 * Full license available at: [https://github.com/XRakib7/A1-Logistics-Test/blob/main/Licences]
 */
public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "A1LogisticsPrefs";
    private static final String USER_KEY = "currentUser";


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupRedirectText;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    private UIBlocker uiBlocker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        uiBlocker = new UIBlocker(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        signupRedirectText = findViewById(R.id.signupRedirectText);

        loginButton.setOnClickListener(v -> attemptLogin());

        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
                        // Optional: Show rationale or disable features
                    }
                }
        );

        checkExistingLogin();
        checkAndRequestStoragePermission();
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) emailEditText.setError("Email required");
            if (password.isEmpty()) passwordEditText.setError("Password required");
            return;
        }

        uiBlocker.blockUI("Logging in...");
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        uiBlocker.showError(task.getException().getMessage());
                        loginButton.setEnabled(true);
                    }
                });
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        loginButton.setEnabled(true);

                    }
                });
    }

    // Add this inside your checkUserRole method (after successful login)
    private void checkUserRole(String uid) {
        uiBlocker.blockUI("Verifying account...");

        db.collection("Admins")
                .whereEqualTo("email", emailEditText.getText().toString().trim())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot adminDoc = task.getResult().getDocuments().get(0);
                        updateLastLogin(adminDoc.getReference());
                        saveUserToPrefs("Admin", adminDoc.getId(), adminDoc.getString("adminName"));

                        uiBlocker.showSuccess("Welcome Admin!", () -> {
                            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                            finish();
                        });
                    } else {
                        db.collection("Merchants")
                                .whereEqualTo("email", emailEditText.getText().toString().trim())
                                .limit(1)
                                .get()
                                .addOnCompleteListener(merchantTask -> {
                                    if (merchantTask.isSuccessful() && !merchantTask.getResult().isEmpty()) {
                                        DocumentSnapshot merchantDoc = merchantTask.getResult().getDocuments().get(0);
                                        updateLastLogin(merchantDoc.getReference());
                                        saveUserToPrefs("Merchant", merchantDoc.getId(), merchantDoc.getString("businessName"));

                                        uiBlocker.showSuccess("Welcome!", () -> {
                                            startActivity(new Intent(LoginActivity.this, MerchantDashboardActivity.class));
                                            finish();
                                        });
                                    } else {
                                        uiBlocker.showError("User not registered");
                                        mAuth.signOut();
                                        loginButton.setEnabled(true);
                                    }
                                });
                    }
                });
    }
    private void updateLastLogin(DocumentReference userRef) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", FieldValue.serverTimestamp());

        userRef.update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating last login", e));
    }

    private void saveUserToPrefs(String role, String uid, String name) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Map<String, String> userData = new HashMap<>();
        userData.put("role", role);
        userData.put("uid", uid);
        userData.put("name", name);
        userData.put("email", emailEditText.getText().toString().trim());

        editor.putString(USER_KEY, new Gson().toJson(userData));
        editor.apply();
    }
    // Add this method to check if user is already logged in
    private void checkExistingLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userJson = prefs.getString(USER_KEY, null);

        if (userJson != null) {
            Map<String, String> userData = new Gson().fromJson(userJson, Map.class);
            String role = userData.get("role");
            String uid = userData.get("uid");
            String email = userData.get("email");

            uiBlocker.blockUI("Restoring session...");

            // Update last login in Firestore
            DocumentReference userRef = db.collection(role + "s") // "Admins" or "Merchants"
                    .document(uid);

            updateLastLogin(userRef);

            // Show success animation before redirecting
            uiBlocker.showSuccess("Welcome back!", () -> {
                Intent intent;
                if ("Admin".equals(role)) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, MerchantDashboardActivity.class);
                }
                startActivity(intent);
                finish();
            });
        }
    }
    // Step 4: Check if permission is needed
    private boolean needsStoragePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    // Step 5: Request permission with rationale (optional)
    private void checkAndRequestStoragePermission() {
        if (needsStoragePermission()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain why permission is needed
                new AlertDialog.Builder(this)
                        .setTitle("Storage Permission Needed")
                        .setMessage("This app needs access to your storage to save/load files.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Directly request permission
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

}
