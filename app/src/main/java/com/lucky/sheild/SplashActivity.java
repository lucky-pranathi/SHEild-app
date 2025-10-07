package com.lucky.sheild;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;

            if (user != null && user.getEmail() != null) {
                if (user.getEmail().equalsIgnoreCase("allurpranathi@gmail.com")) {
                    // Admin user
                    intent = new Intent(SplashActivity.this, AdminActivity.class);
                } else {
                    // Regular logged-in user
                    intent = new Intent(SplashActivity.this, DisplayActivity.class);
                }
            } else {
                // No user logged in
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
