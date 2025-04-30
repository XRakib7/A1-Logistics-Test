package com.example.a1logisticstest1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ErrorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        TextView errorText = findViewById(R.id.errorText);
        Button retryButton = findViewById(R.id.retryButton);

        String error = getIntent().getStringExtra("error");
        errorText.setText(error != null ? error : "Unknown error occurred");

        retryButton.setOnClickListener(v -> {
            finish();
            startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
        });
    }
}