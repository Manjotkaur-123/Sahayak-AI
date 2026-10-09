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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sahayakai.app.R;
import com.sahayakai.app.adapters.SavedSchemeAdapter;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.Scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * SavedSchemesActivity — shows all schemes the user has saved.
 *
 * HOW IT WORKS:
 * 1. Get list of saved scheme IDs from Supabase (saved_schemes table).
 * 2. Fetch full scheme details from schemes/ node.
 * 3. Display in a RecyclerView.
 * 4. Allow removing saved schemes.
 */
public class SavedSchemesActivity extends AppCompatActivity {

    private RecyclerView recyclerSaved;
    private TextView tvEmpty;
    private SavedSchemeAdapter adapter;
    private List<Scheme> savedSchemes = new ArrayList<>();

    private SupabaseHelper supabase;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_schemes);

        supabase = SupabaseHelper.getInstance();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Schemes");
        }

        tvEmpty = findViewById(R.id.tv_empty);
        recyclerSaved = findViewById(R.id.recycler_saved);

        adapter = new SavedSchemeAdapter(this, savedSchemes,
                scheme -> openSchemeDetails(scheme),   // View details
                scheme -> removeSavedScheme(scheme)    // Remove
        );
        recyclerSaved.setLayoutManager(new LinearLayoutManager(this));
        recyclerSaved.setAdapter(adapter);

        // Bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNav();

        loadSavedSchemes();
    }

    private void loadSavedSchemes() {
        supabase.getSavedSchemeIds(new SupabaseHelper.OnSavedSchemesCallback() {
            @Override
            public void onSuccess(List<String> schemeIds) {
                if (schemeIds.isEmpty()) {
                    runOnUiThread(() -> tvEmpty.setVisibility(View.VISIBLE));
                    return;
                }
                // Fetch all schemes then filter by saved IDs
                supabase.getSchemes(new SupabaseHelper.OnSchemesCallback() {
                    @Override
                    public void onSuccess(List<Scheme> allSchemes) {
                        runOnUiThread(() -> {
                            savedSchemes.clear();
                            for (Scheme s : allSchemes) {
                                if (schemeIds.contains(s.getId())) {
                                    savedSchemes.add(s);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(savedSchemes.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> Toast.makeText(SavedSchemesActivity.this,
                                "Error loading schemes: " + error, Toast.LENGTH_SHORT).show());
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(SavedSchemesActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
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

    private void removeSavedScheme(Scheme scheme) {
        supabase.removeSavedScheme(scheme.getId(), new SupabaseHelper.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    int index = savedSchemes.indexOf(scheme);
                    if (index >= 0) {
                        savedSchemes.remove(index);
                        adapter.notifyItemRemoved(index);
                    }
                    tvEmpty.setVisibility(savedSchemes.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(SavedSchemesActivity.this, "Scheme removed.", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(SavedSchemesActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_saved);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_saved) {
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
}
