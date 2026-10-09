package com.sahayakai.app;

import android.app.Application;

import com.sahayakai.app.helpers.SupabaseHelper;

/**
 * SahayakAIApp — Custom Application class.
 *
 * WHY WE NEED THIS (for viva explanation):
 * Android's Application class is the first thing that runs when the app starts —
 * even before any Activity. This is the right place to initialize singletons
 * that are used throughout the app.
 *
 * Previously, FirebaseHelper used Firebase SDK which auto-initializes via
 * google-services.json. Supabase has no such auto-init — we do it manually here.
 *
 * IMPORTANT: This class must be registered in AndroidManifest.xml via
 * android:name=".SahayakAIApp" on the <application> tag.
 */
public class SahayakAIApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SupabaseHelper with the application context.
        // After this, any Activity can call SupabaseHelper.getInstance() without needing context.
        SupabaseHelper.init(this);
    }
}
