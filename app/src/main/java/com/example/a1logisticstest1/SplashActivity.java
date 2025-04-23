package com.example.a1logisticstest1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    // Duration of the splash screen in milliseconds
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize the loading animation
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.playAnimation();

        // Optional: Add any status updates you want to show during loading
        // TextView statusText = findViewById(R.id.status_text);
        // statusText.setText("Initializing app...");

        // Use a Handler to delay the transition to the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // Finish this activity so user can't go back to it
                finish();
            }
        }, SPLASH_DURATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optional: Pause animations when activity is not visible
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        if (loadingAnimation != null) {
            loadingAnimation.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Resume animations when activity becomes visible again
        LottieAnimationView loadingAnimation = findViewById(R.id.loading_animation);
        if (loadingAnimation != null) {
            loadingAnimation.resumeAnimation();
        }
    }
}