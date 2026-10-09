package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.adapters.NotificationAdapter;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.NotificationModel;
import com.sahayakai.app.models.Scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationsActivity — shows all in-app notifications for the user.
 *
 * HOW IT WORKS:
 * - Loads all notifications from Supabase (notifications table for this user).
 * - Marks notifications as read when tapped.
 * - Navigates to ProfileActivity or SchemeDetailsActivity based on notification content.
 * - Sorted newest first.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvEmpty = findViewById(R.id.tv_empty);
        recyclerNotifications = findViewById(R.id.recycler_notifications);

        adapter = new NotificationAdapter(this, notifications, notif -> {
            onNotificationClicked(notif);
        });
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);

        // Add demo notifications if first time, then load all
        addDemoNotificationsIfEmpty();
    }

    private void onNotificationClicked(NotificationModel notif) {
        // 1. Mark as read
        if (!notif.isRead()) {
            SupabaseHelper.getInstance().markNotificationRead(notif.getNotifId());
            notif.setRead(true);
            adapter.notifyDataSetChanged();
        }

        String title = notif.getTitle() != null ? notif.getTitle().trim().toLowerCase() : "";
        String type = notif.getType() != null ? notif.getType().trim() : "";
        String message = notif.getMessage() != null ? notif.getMessage().trim().toLowerCase() : "";

        // Determine destination: profile completion vs scheme match
        boolean isProfileNotification = title.contains("complete your profile")
                || title.contains("update profile")
                || title.contains("fill profile")
                || (title.contains("profile") && !title.contains("scheme") && !title.contains("match"));

        if (isProfileNotification) {
            // Open Profile screen in edit mode
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("editMode", true);
            startActivity(intent);
        } else {
            // Fetch user profile first, then match schemes
            SupabaseHelper supabase = SupabaseHelper.getInstance();
            supabase.getProfile(new SupabaseHelper.OnProfileCallback() {
                @Override
                public void onSuccess(com.sahayakai.app.models.User user) {
                    supabase.getSchemes(new SupabaseHelper.OnSchemesCallback() {
                        @Override
                        public void onSuccess(List<Scheme> allSchemes) {
                            runOnUiThread(() -> {
                                if (allSchemes == null || allSchemes.isEmpty()) {
                                    Toast.makeText(NotificationsActivity.this,
                                            "Abhi koi schemes uplabdh nahi hain.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Match and rank schemes against user's actual profile (including income)
                                List<Scheme> ranked = com.sahayakai.app.helpers.SchemeMatchHelper.matchAndRank(user, allSchemes);
                                List<Scheme> eligibleSchemes = new ArrayList<>();
                                for (Scheme s : ranked) {
                                    if (s.getMatchPercentage() >= 50) {
                                        eligibleSchemes.add(s);
                                    }
                                }

                                if (eligibleSchemes.isEmpty()) {
                                    String incomeInfo = (user != null && user.getIncome() != null && !user.getIncome().isEmpty())
                                            ? " (" + user.getIncome() + ")" : "";
                                    new androidx.appcompat.app.AlertDialog.Builder(NotificationsActivity.this)
                                            .setTitle("No Matching Scheme Found")
                                            .setMessage("Aapki current profile income" + incomeInfo + " ke anusaar abhi koi eligible nayi scheme nahi aayi hai.\n\nJaise hi aapke liye nayi scheme aayegi, hum aapko notify kar denge.")
                                            .setPositiveButton("OK", null)
                                            .setNeutralButton("Explore All Schemes", (d, w) -> {
                                                openSchemeDetails(allSchemes.get(0));
                                            })
                                            .show();
                                } else {
                                    // Open top matched eligible scheme for this user
                                    openSchemeDetails(eligibleSchemes.get(0));
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(NotificationsActivity.this,
                                        "Could not open scheme: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    // Fallback to loading schemes directly if profile fetch fails
                    supabase.getSchemes(new SupabaseHelper.OnSchemesCallback() {
                        @Override
                        public void onSuccess(List<Scheme> allSchemes) {
                            runOnUiThread(() -> {
                                if (allSchemes != null && !allSchemes.isEmpty()) {
                                    openSchemeDetails(allSchemes.get(0));
                                }
                            });
                        }

                        @Override
                        public void onFailure(String err) {
                            runOnUiThread(() -> Toast.makeText(NotificationsActivity.this,
                                    "Error: " + err, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            });
        }
    }

    private void openSchemeDetails(Scheme scheme) {
        Intent intent = new Intent(this, SchemeDetailsActivity.class);
        intent.putExtra("schemeId", scheme.getId());
        intent.putExtra("schemeName", scheme.getName());
        intent.putExtra("schemeSummary", scheme.getSummary());
        intent.putExtra("schemeEligibility", scheme.getEligibility());
        intent.putExtra("schemeDeadline", scheme.getDeadline());
        intent.putExtra("schemeOfficialLink", scheme.getOfficialLink());
        intent.putExtra("schemeApplyLink", scheme.getApplyLink());
        intent.putExtra("schemeGuidelinesLink", scheme.getGuidelinesLink());
        intent.putExtra("schemeCategory", scheme.getCategory());
        intent.putExtra("schemeMatchStatus", scheme.getMatchStatus());
        intent.putExtra("schemeMatchReason", scheme.getMatchReason());
        intent.putExtra("schemeMatchPct", scheme.getMatchPercentage());
        if (scheme.getBenefits() != null)
            intent.putStringArrayListExtra("schemeBenefits", new ArrayList<>(scheme.getBenefits()));
        if (scheme.getDocuments() != null)
            intent.putStringArrayListExtra("schemeDocuments", new ArrayList<>(scheme.getDocuments()));
        if (scheme.getApplicationSteps() != null)
            intent.putStringArrayListExtra("schemeSteps", new ArrayList<>(scheme.getApplicationSteps()));
        startActivity(intent);
    }

    private void loadNotifications() {
        SupabaseHelper.getInstance().getNotifications(new SupabaseHelper.OnNotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationModel> notifList) {
                runOnUiThread(() -> {
                    notifications.clear();
                    notifications.addAll(notifList);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Adds demo notifications on first launch so the screen isn't empty.
     * Calls loadNotifications() once complete.
     */
    private void addDemoNotificationsIfEmpty() {
        SupabaseHelper supabase = SupabaseHelper.getInstance();
        supabase.getNotifications(new SupabaseHelper.OnNotificationsCallback() {
            @Override
            public void onSuccess(List<NotificationModel> notifList) {
                if (notifList.isEmpty()) {
                    // Add demo notifications
                    NotificationModel n1 = new NotificationModel(
                            "🔔 New Scheme Match Found",
                            "A new scholarship scheme may match your profile! Tap to view details.",
                            NotificationModel.TYPE_NEW_SCHEME,
                            System.currentTimeMillis() - 1000 * 60 * 30 // 30 min ago
                    );
                    n1.setSchemeId("scheme_001");

                    NotificationModel n2 = new NotificationModel(
                            "⏰ Deadline Reminder",
                            "NSP Post-Matric Scholarship deadline is approaching. Don't miss it!",
                            NotificationModel.TYPE_DEADLINE,
                            System.currentTimeMillis() - 1000 * 60 * 60 * 2 // 2 hours ago
                    );
                    n2.setSchemeId("scheme_002");

                    NotificationModel n3 = new NotificationModel(
                            "📋 Complete Your Profile",
                            "Adding more profile details helps us find better matching schemes for you.",
                            NotificationModel.TYPE_DOCUMENT,
                            System.currentTimeMillis() - 1000 * 60 * 60 * 24 // yesterday
                    );

                    supabase.saveNotification(n1, new SupabaseHelper.OnCompleteCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });
                    supabase.saveNotification(n2, new SupabaseHelper.OnCompleteCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });
                    supabase.saveNotification(n3, new SupabaseHelper.OnCompleteCallback() {
                        @Override public void onSuccess() { loadNotifications(); }
                        @Override public void onFailure(String error) { loadNotifications(); }
                    });
                } else {
                    loadNotifications();
                }
            }
            @Override
            public void onFailure(String error) {
                loadNotifications(); // Try to load anyway
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
