package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.sahayakai.app.R;
import com.sahayakai.app.adapters.SchemeAdapter;
import com.sahayakai.app.helpers.SchemeDatabaseHelper;
import com.sahayakai.app.helpers.SchemeMatchHelper;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.Scheme;
import com.sahayakai.app.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity — the main dashboard screen.
 *
 * FEATURES:
 * - Greeting with user's name
 * - Quick action chips (tap to open chat with pre-filled query)
 * - Recommended schemes section (matched and ranked)
 * - Bottom navigation to switch between screens
 * - Bell icon for notifications
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting, tvEmpty;
    private RecyclerView recyclerRecommended;
    private SchemeAdapter schemeAdapter;
    private List<Scheme> recommendedSchemes = new ArrayList<>();
    private boolean schemesPopulated = false; // Prevents duplicate populateDemoSchemes() calls

    private SupabaseHelper supabase;
    private User currentUser;

    // Quick suggestion chips
    private Chip chipScholarships, chipWomen, chipStudents, chipEmployment, chipMy;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        supabase = SupabaseHelper.getInstance();

        // Initialize views
        tvGreeting = findViewById(R.id.tv_greeting);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerRecommended = findViewById(R.id.recycler_recommended);

        // Setup RecyclerView
        schemeAdapter = new SchemeAdapter(this, recommendedSchemes, scheme -> {
            // On scheme card click → open details
            openSchemeDetails(scheme);
        });
        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommended.setAdapter(schemeAdapter);

        // Quick suggestion chips
        chipScholarships = findViewById(R.id.chip_scholarships);
        chipWomen = findViewById(R.id.chip_women);
        chipStudents = findViewById(R.id.chip_students);
        chipEmployment = findViewById(R.id.chip_employment);
        chipMy = findViewById(R.id.chip_my_schemes);

        setupChips();

        // Ask box click to open chat
        findViewById(R.id.ll_ask_box).setOnClickListener(v -> openChat());

        // Notification bell
        findViewById(R.id.iv_notifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNav();

        // Load user profile + schemes
        loadUserAndSchemes();
    }

    private void loadUserAndSchemes() {
        supabase.getProfile(new SupabaseHelper.OnProfileCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                runOnUiThread(() -> {
                    if (user != null) {
                        String name = user.getName();
                        String firstName = (name != null && !name.isEmpty()) ? name.split(" ")[0] : "there";
                        tvGreeting.setText("Hello, " + firstName + " 👋");
                    }
                });
                // Load and match schemes
                loadAndMatchSchemes();
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> tvGreeting.setText("Hello! 👋"));
                loadAndMatchSchemes();
            }
        });
    }

    private void loadAndMatchSchemes() {
        supabase.getSchemes(new SupabaseHelper.OnSchemesCallback() {
            @Override
            public void onSuccess(List<Scheme> schemes) {
                if (schemes.isEmpty()) {
                    // Database is empty — populate demo schemes to Supabase (only once)
                    if (!schemesPopulated) {
                        schemesPopulated = true;
                        SchemeDatabaseHelper.populateSchemesToSupabase(supabase);
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this,
                                "Loading schemes for the first time... 🎉", Toast.LENGTH_SHORT).show());
                    }
                    // Try again after a short delay
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> loadAndMatchSchemes(), 3000);
                    return;
                }

                // Match and rank schemes against user profile
                List<Scheme> matched = SchemeMatchHelper.matchAndRank(currentUser, schemes);

                // Show top 5 recommended
                runOnUiThread(() -> {
                    recommendedSchemes.clear();
                    int limit = Math.min(5, matched.size());
                    recommendedSchemes.addAll(matched.subList(0, limit));
                    schemeAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(recommendedSchemes.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this,
                        "Error loading schemes: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupChips() {
        chipScholarships.setOnClickListener(v -> openChatWith("Find scholarships for me"));
        chipWomen.setOnClickListener(v -> openChatWith("Show me schemes for women"));
        chipStudents.setOnClickListener(v -> openChatWith("Show me schemes for students"));
        chipEmployment.setOnClickListener(v -> openChatWith("Show employment schemes"));
        chipMy.setOnClickListener(v -> openChatWith("What government schemes can I apply for?"));
    }

    public void openChat(View view) {
        openChat();
    }

    public void openChat() {
        startActivity(new Intent(this, ChatActivity.class));
    }

    private void openChatWith(String query) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("quickQuery", query);
        startActivity(intent);
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
        // Pass lists as ArrayList
        if (scheme.getBenefits() != null)
            intent.putStringArrayListExtra("schemeBenefits", new ArrayList<>(scheme.getBenefits()));
        if (scheme.getDocuments() != null)
            intent.putStringArrayListExtra("schemeDocuments", new ArrayList<>(scheme.getDocuments()));
        if (scheme.getApplicationSteps() != null)
            intent.putStringArrayListExtra("schemeSteps", new ArrayList<>(scheme.getApplicationSteps()));
        startActivity(intent);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // Already here
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedSchemesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("editMode", true);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recommended schemes when returning to home
        if (currentUser != null) {
            loadAndMatchSchemes();
        }
    }
}
