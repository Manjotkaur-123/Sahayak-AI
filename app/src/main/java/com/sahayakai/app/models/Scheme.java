package com.sahayakai.app.models;

import java.util.List;

/**
 * Scheme model — represents a government scheme.
 * This maps to Supabase table: schemes
 * The matchPercentage, matchStatus, matchReason fields are calculated locally — NOT stored in DB.
 */
public class Scheme {

    private String id;
    private String name;
    private String summary;
    private String eligibility;
    private String deadline;         // Format: "30 September 2026" or "Ongoing"
    private String officialLink;
    private String applyLink;
    private String guidelinesLink;
    private String category;         // Education, Women, Employment, etc.

    // Lists for multiple items
    private List<String> benefits;
    private List<String> documents;
    private List<String> applicationSteps;

    // Eligibility filter fields (stored in Supabase, used for local matching)
    private List<String> states;           // Which states this scheme applies to
    private List<String> genders;          // "Male", "Female", "Other" or empty = all
    private List<String> categories;       // Caste/social categories eligible
    private List<String> educationLevels;  // Education qualifications eligible
    private int minAge;                    // Minimum age (0 = no limit)
    private int maxAge;                    // Maximum age (0 = no limit)
    private String maxIncome;              // Max annual income eligible (e.g. "Below ₹1 lakh")

    // Calculated locally during matching — not stored in Supabase
    private int matchPercentage;
    private String matchStatus;      // "Likely Eligible", "Possibly Eligible", etc.
    private String matchReason;      // Why this scheme matches the user's profile

    // Required no-argument constructor
    public Scheme() {}

    public Scheme(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    // ---- Getters and Setters ----

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getOfficialLink() { return officialLink; }
    public void setOfficialLink(String officialLink) { this.officialLink = officialLink; }

    public String getApplyLink() { return applyLink; }
    public void setApplyLink(String applyLink) { this.applyLink = applyLink; }

    public String getGuidelinesLink() { return guidelinesLink; }
    public void setGuidelinesLink(String guidelinesLink) { this.guidelinesLink = guidelinesLink; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }

    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }

    public List<String> getApplicationSteps() { return applicationSteps; }
    public void setApplicationSteps(List<String> applicationSteps) { this.applicationSteps = applicationSteps; }

    public List<String> getStates() { return states; }
    public void setStates(List<String> states) { this.states = states; }

    public List<String> getGenders() { return genders; }
    public void setGenders(List<String> genders) { this.genders = genders; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public List<String> getEducationLevels() { return educationLevels; }
    public void setEducationLevels(List<String> educationLevels) { this.educationLevels = educationLevels; }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public String getMaxIncome() { return maxIncome; }
    public void setMaxIncome(String maxIncome) { this.maxIncome = maxIncome; }

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    /**
     * Returns a simple text summary of the scheme for use in Gemini prompts.
     */
    public String toPromptSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scheme: ").append(name).append("\n");
        sb.append("Category: ").append(category).append("\n");
        sb.append("Summary: ").append(summary).append("\n");
        sb.append("Eligibility: ").append(eligibility).append("\n");
        sb.append("Deadline: ").append(deadline).append("\n");
        if (benefits != null && !benefits.isEmpty()) {
            sb.append("Benefits: ").append(String.join(", ", benefits)).append("\n");
        }
        return sb.toString();
    }
}
