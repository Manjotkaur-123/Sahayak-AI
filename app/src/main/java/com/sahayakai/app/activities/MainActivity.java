package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.sahayakai.app.R;
import com.sahayakai.app.helpers.SupabaseHelper;

/**
 * MainActivity — Splash screen and routing logic.
 *
 * HOW IT WORKS:
 * 1. Shows the splash screen for 2 seconds.
 * 2. Checks if user is already logged in via SupabaseHelper (checks SharedPreferences token).
 * 3. If logged in, checks if profile is complete.
 * 4. Routes to the correct screen (Login, Profile, or Home).
 */
public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // After splash duration, decide where to go
        new Handler(android.os.Looper.getMainLooper()).postDelayed(this::checkUserAndRoute, SPLASH_DURATION);
    }

    /**
     * Decides which screen to open based on login and profile status.
     */
    private void checkUserAndRoute() {
        SupabaseHelper supabase = SupabaseHelper.getInstance();

        if (!supabase.isLoggedIn()) {
            // No stored session → go to Login screen
            goToLogin();
        } else {
            // Session exists → check if profile is complete
            supabase.checkProfileExists(exists -> {
                if (exists) {
                    // Profile complete → go to Home
                    goToHome();
                } else {
                    // Profile incomplete → go to Profile setup
                    goToProfile();
                }
            });
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void goToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}
