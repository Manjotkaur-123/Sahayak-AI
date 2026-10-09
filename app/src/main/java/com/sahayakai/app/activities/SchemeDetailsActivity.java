package com.sahayakai.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.sahayakai.app.R;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.ApplicationModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * SchemeDetailsActivity — shows the full details of a selected scheme.
 *
 * FEATURES:
 * - Full scheme info (summary, benefits, eligibility, documents, steps)
 * - Match status and reason
 * - Deadline with days remaining
 * - Official links (open in browser)
 * - Save scheme button
 * - Application status tracker
 * - Document checklist
 */
public class SchemeDetailsActivity extends AppCompatActivity {

    // Views
    private TextView tvName, tvCategory, tvSummary, tvEligibility, tvDeadline,
            tvDaysLeft, tvMatchStatus, tvMatchReason, tvDemoNotice;
    private TextView tvBenefits, tvDocuments, tvSteps;
    private Button btnSave, btnOfficialLink, btnGuidelinesLink;
    private ProgressBar progressBar;
    private LinearLayout llDocumentChecklist, llApplicationTracker;
    private TextView tvAppStatus;
    private ProgressBar progressApplication;

    // Data from Intent
    private String schemeId, schemeName, schemeSummary, schemeEligibility,
            schemeDeadline, schemeOfficialLink, schemeApplyLink, schemeGuidelinesLink,
            schemeCategory, schemeMatchStatus, schemeMatchReason;
    private int schemeMatchPct;
    private ArrayList<String> schemeBenefits, schemeDocuments, schemeSteps;

    private SupabaseHelper supabase;
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_details);

        supabase = SupabaseHelper.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scheme Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get data from Intent
        extractIntentData();

        // Initialize views
        initViews();

        // Populate the UI
        populateUI();

        // Check if already saved
        checkSavedStatus();

        // Load application status
        loadApplicationStatus();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        schemeId = intent.getStringExtra("schemeId");
        schemeName = intent.getStringExtra("schemeName");
        schemeSummary = intent.getStringExtra("schemeSummary");
        schemeEligibility = intent.getStringExtra("schemeEligibility");
        schemeDeadline = intent.getStringExtra("schemeDeadline");
        schemeOfficialLink = intent.getStringExtra("schemeOfficialLink");
        schemeApplyLink = intent.getStringExtra("schemeApplyLink");
        schemeGuidelinesLink = intent.getStringExtra("schemeGuidelinesLink");
        schemeCategory = intent.getStringExtra("schemeCategory");
        schemeMatchStatus = intent.getStringExtra("schemeMatchStatus");
        schemeMatchReason = intent.getStringExtra("schemeMatchReason");
        schemeMatchPct = intent.getIntExtra("schemeMatchPct", 0);
        schemeBenefits = intent.getStringArrayListExtra("schemeBenefits");
        schemeDocuments = intent.getStringArrayListExtra("schemeDocuments");
        schemeSteps = intent.getStringArrayListExtra("schemeSteps");
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_scheme_name);
        tvCategory = findViewById(R.id.tv_category);
        tvSummary = findViewById(R.id.tv_summary);
        tvEligibility = findViewById(R.id.tv_eligibility);
        tvDeadline = findViewById(R.id.tv_deadline);
        tvDaysLeft = findViewById(R.id.tv_days_left);
        tvMatchStatus = findViewById(R.id.tv_match_status);
        tvMatchReason = findViewById(R.id.tv_match_reason);
        tvBenefits = findViewById(R.id.tv_benefits);
        tvDocuments = findViewById(R.id.tv_documents);
        tvSteps = findViewById(R.id.tv_steps);
        tvDemoNotice = findViewById(R.id.tv_demo_notice);
        tvAppStatus = findViewById(R.id.tv_app_status);
        progressApplication = findViewById(R.id.progress_application);
        btnSave = findViewById(R.id.btn_save);
        btnOfficialLink = findViewById(R.id.btn_official_link);
        btnGuidelinesLink = findViewById(R.id.btn_guidelines_link);
        progressBar = findViewById(R.id.progress_bar);
        llDocumentChecklist = findViewById(R.id.ll_document_checklist);
        llApplicationTracker = findViewById(R.id.ll_application_tracker);
    }

    private void populateUI() {
        // Basic info
        tvName.setText(schemeName != null ? schemeName : "Scheme");
        tvCategory.setText(schemeCategory != null ? schemeCategory : "");
        tvSummary.setText(schemeSummary != null ? schemeSummary : "");
        tvEligibility.setText(schemeEligibility != null ? schemeEligibility : "");

        // Match status
        if (schemeMatchStatus != null) {
            tvMatchStatus.setText(schemeMatchStatus + "  (" + schemeMatchPct + "% match)");
            tvMatchReason.setText(schemeMatchReason != null ? schemeMatchReason :
                    "Based on your profile information.");
            // Color coding
            if (schemeMatchStatus.contains("Likely")) {
                tvMatchStatus.setTextColor(ContextCompat.getColor(this, R.color.status_eligible));
            } else if (schemeMatchStatus.contains("Possibly")) {
                tvMatchStatus.setTextColor(ContextCompat.getColor(this, R.color.status_possibly));
            } else if (schemeMatchStatus.contains("Needs")) {
                tvMatchStatus.setTextColor(ContextCompat.getColor(this, R.color.status_verify));
            } else {
                tvMatchStatus.setTextColor(ContextCompat.getColor(this, R.color.status_not_eligible));
            }
        }

        // Deadline
        if (schemeDeadline != null) {
            tvDeadline.setText("📅 " + schemeDeadline);
            tvDaysLeft.setText(calculateDaysLeft(schemeDeadline));
        }

        // Benefits
        if (schemeBenefits != null && !schemeBenefits.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String b : schemeBenefits) sb.append("• ").append(b).append("\n");
            tvBenefits.setText(sb.toString().trim());
        }

        // Documents
        if (schemeDocuments != null && !schemeDocuments.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String d : schemeDocuments) sb.append("☐ ").append(d).append("\n");
            tvDocuments.setText(sb.toString().trim());
        }

        // Application steps
        if (schemeSteps != null && !schemeSteps.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < schemeSteps.size(); i++) {
                sb.append((i + 1)).append(". ").append(schemeSteps.get(i)).append("\n");
            }
            tvSteps.setText(sb.toString().trim());
        }

        // Links
        setupLink(btnOfficialLink, schemeOfficialLink, "official");
        
        // Guidelines button shows comprehensive in-app guidelines dialog (no external redirects)
        btnGuidelinesLink.setOnClickListener(v -> showGuidelinesDialog());

        // Save button
        btnSave.setOnClickListener(v -> toggleSaveScheme());

        // Application tracker status update
        tvAppStatus.setOnClickListener(v -> showStatusUpdateDialog());
    }

    private void showGuidelinesDialog() {
        StringBuilder sb = new StringBuilder();

        sb.append("📌 ELIGIBILITY RULES & CRITERIA\n");
        sb.append(schemeEligibility != null ? schemeEligibility : "General eligibility applies as per Government norms.");
        sb.append("\n\n");

        if (schemeBenefits != null && !schemeBenefits.isEmpty()) {
            sb.append("💰 SCHEME BENEFITS & ASSISTANCE\n");
            for (String b : schemeBenefits) {
                sb.append("• ").append(b).append("\n");
            }
            sb.append("\n");
        }

        if (schemeDocuments != null && !schemeDocuments.isEmpty()) {
            sb.append("📄 MANDATORY DOCUMENTS CHECKLIST\n");
            for (String d : schemeDocuments) {
                sb.append("☑ ").append(d).append("\n");
            }
            sb.append("\n");
        }

        if (schemeSteps != null && !schemeSteps.isEmpty()) {
            sb.append("📝 STEP-BY-STEP APPLICATION PROCEDURE\n");
            for (int i = 0; i < schemeSteps.size(); i++) {
                sb.append((i + 1)).append(". ").append(schemeSteps.get(i)).append("\n");
            }
            sb.append("\n");
        }

        sb.append("⚠️ IMPORTANT INSTRUCTIONS\n");
        sb.append("• Ensure all uploaded documents are self-attested and clearly legible.\n");
        sb.append("• Bank account must be linked with Aadhaar and active for DBT benefits.\n");
        sb.append("• Deadline: ").append(schemeDeadline != null ? schemeDeadline : "Ongoing").append(" (Late submissions are not accepted).");

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        TextView tv = new TextView(this);
        tv.setText(sb.toString());
        tv.setTextSize(14f);
        tv.setLineSpacing(6f, 1.25f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tv.setPadding(48, 32, 48, 32);
        scrollView.addView(tv);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📋 Scheme Guidelines & Rules")
                .setView(scrollView)
                .setPositiveButton("Back", null)
                .show();
    }

    private void setupLink(Button button, String rawUrl, String linkType) {
        button.setEnabled(true);
        button.setAlpha(1.0f);
        button.setOnClickListener(v -> {
            String cleanSchemeName = schemeName != null ? schemeName.replace("⚠️ [DEMO] ", "").replace("⚠️", "").trim() : "Government Scheme";
            String targetUrl = rawUrl;

            if (targetUrl == null || targetUrl.trim().isEmpty() || !targetUrl.startsWith("http")) {
                if (schemeOfficialLink != null && schemeOfficialLink.startsWith("http")) {
                    targetUrl = schemeOfficialLink;
                } else {
                    targetUrl = "https://www.google.com/search?q=" + Uri.encode(cleanSchemeName + " official government portal");
                }
            }

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                try {
                    String fallback = "https://www.google.com/search?q=" + Uri.encode(cleanSchemeName + " " + linkType + " official gov in portal");
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallback));
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(fallbackIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String calculateDaysLeft(String deadlineStr) {
        if ("Ongoing".equalsIgnoreCase(deadlineStr)) return "🟢 Ongoing";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
            Date deadline = sdf.parse(deadlineStr);
            if (deadline == null) return "";
            long diff = deadline.getTime() - System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days < 0) return "❌ Expired";
            if (days == 0) return "⚠️ Deadline Today!";
            if (days <= 7) return "🔴 " + days + " days left";
            if (days <= 15) return "🟡 " + days + " days left";
            return "🟢 " + days + " days left";
        } catch (ParseException e) {
            return "";
        }
    }

    private void checkSavedStatus() {
        if (schemeId == null) return;
        supabase.isSchemesSaved(schemeId, saved -> {
            isSaved = saved;
            runOnUiThread(this::updateSaveButton);
        });
    }

    private void toggleSaveScheme() {
        if (schemeId == null) return;
        if (isSaved) {
            supabase.removeSavedScheme(schemeId, new SupabaseHelper.OnCompleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        isSaved = false;
                        updateSaveButton();
                        Toast.makeText(SchemeDetailsActivity.this, "Scheme removed from saved.", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> Toast.makeText(SchemeDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            supabase.saveSchemeForUser(schemeId, new SupabaseHelper.OnCompleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        isSaved = true;
                        updateSaveButton();
                        Toast.makeText(SchemeDetailsActivity.this, "Scheme saved! ✓", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> Toast.makeText(SchemeDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void updateSaveButton() {
        btnSave.setText(isSaved ? "✓ Saved" : "Save Scheme");
        btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isSaved ? R.color.status_eligible : R.color.primary));
    }

    private void loadApplicationStatus() {
        if (schemeId == null) return;
        supabase.getApplicationStatus(schemeId, new SupabaseHelper.OnApplicationCallback() {
            @Override
            public void onSuccess(ApplicationModel app) {
                runOnUiThread(() -> {
                    if (app != null) {
                        tvAppStatus.setText(app.getStatus());
                        progressApplication.setProgress(app.getProgressValue() * 25);
                    } else {
                        tvAppStatus.setText(ApplicationModel.STATUS_NOT_STARTED);
                        progressApplication.setProgress(0);
                    }
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void showStatusUpdateDialog() {
        String[] statuses = {
                ApplicationModel.STATUS_NOT_STARTED,
                ApplicationModel.STATUS_DOCS_PREPARING,
                ApplicationModel.STATUS_APP_STARTED,
                ApplicationModel.STATUS_SUBMITTED,
                ApplicationModel.STATUS_COMPLETED
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Update Application Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    ApplicationModel app = new ApplicationModel(schemeId, schemeName);
                    app.setStatus(newStatus);
                    supabase.saveApplication(app, new SupabaseHelper.OnCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                tvAppStatus.setText(newStatus);
                                progressApplication.setProgress(app.getProgressValue() * 25);
                                Toast.makeText(SchemeDetailsActivity.this,
                                        "Status updated: " + newStatus, Toast.LENGTH_SHORT).show();
                            });
                        }
                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> Toast.makeText(SchemeDetailsActivity.this,
                                    "Error updating status: " + error, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
