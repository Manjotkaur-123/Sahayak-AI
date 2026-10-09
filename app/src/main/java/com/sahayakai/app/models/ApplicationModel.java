package com.sahayakai.app.models;

/**
 * ApplicationModel — tracks the user's application status for a scheme.
 * Stored in Firebase under: applications/{UID}/{schemeId}
 */
public class ApplicationModel {

    // Application status constants
    public static final String STATUS_NOT_STARTED = "Not Started";
    public static final String STATUS_DOCS_PREPARING = "Documents Preparing";
    public static final String STATUS_APP_STARTED = "Application Started";
    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_COMPLETED = "Completed";

    private String schemeId;
    private String schemeName;  // For display purposes
    private String status;
    private long updatedAt;

    // Required no-argument constructor for Firebase
    public ApplicationModel() {}

    public ApplicationModel(String schemeId, String schemeName) {
        this.schemeId = schemeId;
        this.schemeName = schemeName;
        this.status = STATUS_NOT_STARTED;
        this.updatedAt = System.currentTimeMillis();
    }

    // ---- Getters and Setters ----

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns 0-4 progress value for ProgressBar (max = 4).
     */
    public int getProgressValue() {
        switch (status) {
            case STATUS_NOT_STARTED:    return 0;
            case STATUS_DOCS_PREPARING: return 1;
            case STATUS_APP_STARTED:    return 2;
            case STATUS_SUBMITTED:      return 3;
            case STATUS_COMPLETED:      return 4;
            default:                    return 0;
        }
    }
}
