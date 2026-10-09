package com.sahayakai.app.helpers;

import com.sahayakai.app.models.Scheme;
import com.sahayakai.app.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SchemeMatchHelper — compares a user's profile against scheme eligibility criteria.
 *
 * HOW IT WORKS (for viva explanation):
 * - We check each eligibility condition (age, gender, state, income, education, category).
 * - For each condition that matches, we add 1 to the "matched" count.
 * - Match % = (matched conditions / total conditions checked) × 100
 * - Based on match %, we assign a status: Likely Eligible, Possibly Eligible, etc.
 *
 * NOTE: This is a simplified matching algorithm for the academic demo.
 * Real government eligibility requires official verification.
 */
public class SchemeMatchHelper {

    // Match % thresholds
    private static final int THRESHOLD_LIKELY = 80;
    private static final int THRESHOLD_POSSIBLY = 50;
    private static final int THRESHOLD_VERIFY = 25;

    /**
     * Matches the user's profile against all available schemes and returns
     * a ranked list with match percentages and status labels.
     *
     * @param user    The user's profile
     * @param schemes All schemes from the database
     * @return Ranked list of schemes (highest match first)
     */
    public static List<Scheme> matchAndRank(User user, List<Scheme> schemes) {
        if (user == null || schemes == null || schemes.isEmpty()) {
            return schemes != null ? schemes : new ArrayList<>();
        }

        for (Scheme scheme : schemes) {
            MatchResult result = calculateMatch(user, scheme);
            scheme.setMatchPercentage(result.percentage);
            scheme.setMatchStatus(getStatusLabel(result.percentage));
            scheme.setMatchReason(result.reason);
        }

        // Sort by match percentage, highest first
        List<Scheme> sorted = new ArrayList<>(schemes);
        Collections.sort(sorted, (a, b) -> b.getMatchPercentage() - a.getMatchPercentage());
        return sorted;
    }

    /**
     * Filter schemes that are relevant to a specific category (e.g., "Education", "Women").
     */
    public static List<Scheme> filterByCategory(List<Scheme> schemes, String category) {
        List<Scheme> filtered = new ArrayList<>();
        for (Scheme scheme : schemes) {
            if (scheme.getCategory() != null &&
                    scheme.getCategory().equalsIgnoreCase(category)) {
                filtered.add(scheme);
            }
        }
        return filtered;
    }

    /**
     * Filter schemes by keyword in name, summary, or eligibility text.
     */
    public static List<Scheme> filterByKeyword(List<Scheme> schemes, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return schemes;
        String kw = keyword.toLowerCase().trim();
        List<Scheme> filtered = new ArrayList<>();
        for (Scheme scheme : schemes) {
            if ((scheme.getName() != null && scheme.getName().toLowerCase().contains(kw)) ||
                (scheme.getSummary() != null && scheme.getSummary().toLowerCase().contains(kw)) ||
                (scheme.getEligibility() != null && scheme.getEligibility().toLowerCase().contains(kw)) ||
                (scheme.getCategory() != null && scheme.getCategory().toLowerCase().contains(kw))) {
                filtered.add(scheme);
            }
        }
        return filtered;
    }

    // ---- Private Helper Methods ----

    private static class MatchResult {
        int percentage;
        String reason;
        MatchResult(int percentage, String reason) {
            this.percentage = percentage;
            this.reason = reason;
        }
    }

    /**
     * Core matching algorithm.
     * For each eligibility criterion in the scheme, we check if the user matches.
     */
    private static MatchResult calculateMatch(User user, Scheme scheme) {
        int totalChecks = 0;
        int matched = 0;
        StringBuilder reasons = new StringBuilder();

        // Combine both summary and eligibility fields.
        // This is critical because in the database helper, the descriptive text was saved to eligibility
        // and the structured key-value criteria string was saved to summary.
        String criteria = (scheme.getSummary() != null ? scheme.getSummary() : "") + " " +
                (scheme.getEligibility() != null ? scheme.getEligibility() : "");
        String eligLower = criteria.toLowerCase();
        String category = scheme.getCategory();
        if (category == null) category = "";

        // 1. Check GENDER
        if (eligLower.contains("women") || eligLower.contains("girl") ||
                eligLower.contains("female") || category.equalsIgnoreCase("Women")) {
            totalChecks++;
            if ("Female".equalsIgnoreCase(user.getGender())) {
                matched++;
                reasons.append("✓ Gender matches (Female)\n");
            } else {
                // Hard disqualification
                return new MatchResult(0, "🔴 Scheme is only for female applicants.");
            }
        }

        // 2. Check STUDENT STATUS
        if (eligLower.contains("student") || category.equalsIgnoreCase("Education") ||
                category.equalsIgnoreCase("Scholarships")) {
            totalChecks++;
            if ("Yes".equalsIgnoreCase(user.getStudentStatus())) {
                matched++;
                reasons.append("✓ Currently a student\n");
            } else {
                // Hard disqualification
                return new MatchResult(0, "🔴 Scheme is only for current students.");
            }
        }

        // 3. Check INCOME
        if (eligLower.contains("income") || eligLower.contains("lakh") || eligLower.contains("bpl") || eligLower.contains("taxpayer")) {
            totalChecks++;
            MatchResult incomeResult = checkIncomeEligibility(user.getIncome(), eligLower);
            if (incomeResult != null) {
                if (incomeResult.percentage == 0) {
                    // Hard disqualification: user's income strictly exceeds the scheme's limit!
                    return incomeResult;
                } else {
                    matched++;
                    reasons.append(incomeResult.reason).append("\n");
                }
            } else {
                // If user didn't select income yet, don't penalize
                totalChecks--;
            }
        }

        // 4. Check CATEGORY (SC/ST/OBC/Minority)
        if (eligLower.contains("sc") || eligLower.contains("scheduled caste") ||
                eligLower.contains("st") || eligLower.contains("obc") ||
                eligLower.contains("minority") || eligLower.contains("backward")) {
            totalChecks++;
            String userCategory = user.getCategory();
            if (userCategory != null) {
                String catLower = userCategory.toLowerCase();
                if ((eligLower.contains("sc") || eligLower.contains("scheduled caste")) && catLower.contains("sc")) {
                    matched++; reasons.append("✓ Category matches (SC)\n");
                } else if (eligLower.contains("st") && catLower.contains("st")) {
                    matched++; reasons.append("✓ Category matches (ST)\n");
                } else if (eligLower.contains("obc") && catLower.contains("obc")) {
                    matched++; reasons.append("✓ Category matches (OBC)\n");
                } else if (eligLower.contains("minority") && catLower.contains("minority")) {
                    matched++; reasons.append("✓ Category matches (Minority)\n");
                } else if (catLower.contains("general")) {
                    // General category is eligible for most schemes
                    matched++; reasons.append("✓ Open to General category\n");
                }
            }
        }

        // 5. Check STATE
        if (eligLower.contains("state") || eligLower.contains("india") || eligLower.contains("uttar pradesh") || eligLower.contains("bihar")) {
            totalChecks++;
            // Central government schemes match all states
            if (eligLower.contains("all india") || eligLower.contains("india")) {
                matched++;
                reasons.append("✓ Available across India\n");
            } else if (user.getState() != null &&
                    eligLower.contains(user.getState().toLowerCase())) {
                matched++;
                reasons.append("✓ Available in " + user.getState() + "\n");
            } else {
                // Hard disqualification
                return new MatchResult(0, "🔴 Scheme is not available in your state.");
            }
        }

        // 6. Check EDUCATION
        if (eligLower.contains("graduation") || eligLower.contains("class 12") ||
                eligLower.contains("diploma") || eligLower.contains("undergraduate") ||
                eligLower.contains("post graduate") || eligLower.contains("10th") ||
                eligLower.contains("12th")) {
            totalChecks++;
            String edu = user.getEducation();
            if (edu != null) {
                String eduLower = edu.toLowerCase();
                if ((eligLower.contains("graduation") || eligLower.contains("undergraduate"))
                        && (eduLower.contains("b.tech") || eduLower.contains("b.sc") ||
                            eduLower.contains("ba") || eduLower.contains("b.com") ||
                            eduLower.contains("graduate"))) {
                    matched++;
                    reasons.append("✓ Education level matches (Graduate)\n");
                } else if (eligLower.contains("12th") && eduLower.contains("class 12")) {
                    matched++; reasons.append("✓ Class 12 completed\n");
                } else if (eligLower.contains("10th") && (eduLower.contains("class 10") ||
                        eduLower.contains("class 12") || eduLower.contains("graduate"))) {
                    matched++; reasons.append("✓ Minimum education met\n");
                } else if (eligLower.contains("post graduate") && eduLower.contains("post graduate")) {
                    matched++; reasons.append("✓ Post-graduate education matches\n");
                } else {
                    // Generic education check
                    matched++;
                    reasons.append("✓ Education may qualify (verify officially)\n");
                }
            }
        }

        // 7. Check AGE
        if (eligLower.contains("age") || eligLower.contains("years") || eligLower.contains("yr")) {
            totalChecks++;
            String ageStr = user.getAge();
            if (ageStr == null || ageStr.trim().isEmpty()) {
                // Can't check age — skip this criterion
                totalChecks--;
            } else {
                try {
                    int userAge = Integer.parseInt(ageStr.trim());
                    // Use regex-based helper for precise validation
                    MatchResult ageResult = checkAgeEligibility(userAge, criteria);
                    if (ageResult != null) {
                        if (ageResult.percentage == 0) {
                            // Hard disqualification
                            return ageResult;
                        } else {
                            matched++;
                            reasons.append(ageResult.reason).append("\n");
                        }
                    } else {
                        matched++;
                        reasons.append("✓ Age qualifies\n");
                    }
                } catch (NumberFormatException e) {
                    // Can't parse age, skip this check
                    totalChecks--;
                }
            }
        }

        // 8. Check OCCUPATION
        if (eligLower.contains("occupation") || eligLower.contains("farmer") ||
                eligLower.contains("entrepreneur") || eligLower.contains("business") ||
                eligLower.contains("salaried") || eligLower.contains("unemployed")) {
            totalChecks++;
            String userOcc = user.getOccupation();
            if (userOcc == null || userOcc.trim().isEmpty()) {
                totalChecks--; // Skip check if profile has no occupation
            } else {
                String occLower = userOcc.toLowerCase();
                if (eligLower.contains("farmer")) {
                    if (occLower.contains("farmer")) {
                        matched++; reasons.append("✓ Occupation matches (Farmer)\n");
                    } else {
                        // Hard disqualification
                        return new MatchResult(0, "🔴 Scheme is only for farmers.");
                    }
                } else if (eligLower.contains("entrepreneur") || eligLower.contains("business")) {
                    if (occLower.contains("entrepreneur") || occLower.contains("business")) {
                        matched++; reasons.append("✓ Occupation matches (Business/Entrepreneur)\n");
                    } else {
                        // Hard disqualification
                        return new MatchResult(0, "🔴 Scheme is only for entrepreneurs/business owners.");
                    }
                } else if (eligLower.contains("salaried")) {
                    if (occLower.contains("salaried")) {
                        matched++; reasons.append("✓ Occupation matches (Salaried)\n");
                    } else {
                        // Hard disqualification
                        return new MatchResult(0, "🔴 Scheme is only for salaried employees.");
                    }
                } else {
                    matched++; reasons.append("✓ Occupation qualifies\n");
                }
            }
        }

        // If no specific criteria were found, treat it as a general scheme
        if (totalChecks == 0) {
            return new MatchResult(60, "✓ General scheme — may be applicable\n(Verify eligibility officially)");
        }

        int percentage = (int) Math.round((double) matched / totalChecks * 100);
        String reason = reasons.length() > 0 ? reasons.toString() :
                "Review eligibility criteria carefully.";

        return new MatchResult(percentage, reason);
    }

    /**
     * Regex-based age parser and validator.
     * Matches patterns like "18-25", "0-18", "18+", "10+", etc.
     */
    private static MatchResult checkAgeEligibility(int userAge, String eligText) {
        String lower = eligText.toLowerCase();

        // Pattern 1: Age range like "18-25", "0-18", "18-65"
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile("age:\\s*(\\d+)\\s*-\\s*(\\d+)");
        java.util.regex.Matcher rangeMatcher = rangePattern.matcher(lower);
        if (rangeMatcher.find()) {
            try {
                int minAge = Integer.parseInt(rangeMatcher.group(1));
                int maxAge = Integer.parseInt(rangeMatcher.group(2));
                if (userAge >= minAge && userAge <= maxAge) {
                    return new MatchResult(100, "✓ Age: " + userAge + " (in range " + minAge + "-" + maxAge + " years)");
                } else {
                    return new MatchResult(0, "🔴 Age is outside the required range (" + minAge + "-" + maxAge + " years)");
                }
            } catch (NumberFormatException e) {}
        }

        // Pattern 1b: Alternative range syntax like "18 to 25", "6 to 14"
        java.util.regex.Pattern wordRangePattern = java.util.regex.Pattern.compile("age:\\s*(\\d+)\\s*to\\s*(\\d+)");
        java.util.regex.Matcher wordRangeMatcher = wordRangePattern.matcher(lower);
        if (wordRangeMatcher.find()) {
            try {
                int minAge = Integer.parseInt(wordRangeMatcher.group(1));
                int maxAge = Integer.parseInt(wordRangeMatcher.group(2));
                if (userAge >= minAge && userAge <= maxAge) {
                    return new MatchResult(100, "✓ Age: " + userAge + " (in range " + minAge + "-" + maxAge + " years)");
                } else {
                    return new MatchResult(0, "🔴 Age is outside the required range (" + minAge + "-" + maxAge + " years)");
                }
            } catch (NumberFormatException e) {}
        }

        // Pattern 2: Minimum limit with "+" like "18+", "10+", "60+"
        java.util.regex.Pattern plusPattern = java.util.regex.Pattern.compile("(\\d+)\\s*\\+");
        java.util.regex.Matcher plusMatcher = plusPattern.matcher(lower);
        if (plusMatcher.find()) {
            try {
                int minAge = Integer.parseInt(plusMatcher.group(1));
                if (userAge >= minAge) {
                    return new MatchResult(100, "✓ Age: " + userAge + " (" + minAge + "+ years)");
                } else {
                    return new MatchResult(0, "🔴 Minimum age required is " + minAge + " years");
                }
            } catch (NumberFormatException e) {}
        }

        // Pattern 3: Words indicating minimum age
        java.util.regex.Pattern minWordPattern = java.util.regex.Pattern.compile("(?:above|minimum|at least|older than|greater than)\\s*(\\d+)");
        java.util.regex.Matcher minWordMatcher = minWordPattern.matcher(lower);
        if (minWordMatcher.find()) {
            try {
                int minAge = Integer.parseInt(minWordMatcher.group(1));
                if (userAge >= minAge) {
                    return new MatchResult(100, "✓ Age qualifies (minimum " + minAge + ")");
                } else {
                    return new MatchResult(0, "🔴 Minimum age required is " + minAge + " years");
                }
            } catch (NumberFormatException e) {}
        }

        // Pattern 4: Words indicating maximum age
        java.util.regex.Pattern maxWordPattern = java.util.regex.Pattern.compile("(?:below|under|maximum|up to|younger than)\\s*(\\d+)");
        java.util.regex.Matcher maxWordMatcher = maxWordPattern.matcher(lower);
        if (maxWordMatcher.find()) {
            try {
                int maxAge = Integer.parseInt(maxWordMatcher.group(1));
                if (userAge <= maxAge) {
                    return new MatchResult(100, "✓ Age qualifies (up to " + maxAge + ")");
                } else {
                    return new MatchResult(0, "🔴 Age is outside the required limit (max " + maxAge + " years)");
                }
            } catch (NumberFormatException e) {}
        }

        return null; // Return null if no specific rule found (will default to eligible)
    }

    /**
     * Precise income eligibility validator.
     * Evaluates user income bracket against scheme income caps.
     */
    private static MatchResult checkIncomeEligibility(String userIncome, String eligLower) {
        if (userIncome == null || userIncome.trim().isEmpty() || userIncome.contains("Select")) {
            return null; // Can't verify income, skip
        }

        String uInc = userIncome.toLowerCase().replace(" ", "").replace("₹", "");
        // Normalized user levels:
        // "below1lakh" -> 1
        // "1-3lakh" -> 2
        // "3-5lakh" -> 3
        // "5-8lakh" -> 4
        // "8-12lakh" -> 5
        // "above12lakh" -> 6
        int userLevel = 1;
        if (uInc.contains("1-3") || uInc.contains("1to3")) {
            userLevel = 2;
        } else if (uInc.contains("3-5") || uInc.contains("3to5")) {
            userLevel = 3;
        } else if (uInc.contains("5-8") || uInc.contains("5to8")) {
            userLevel = 4;
        } else if (uInc.contains("8-12") || uInc.contains("8to12")) {
            userLevel = 5;
        } else if (uInc.contains("above") || uInc.contains(">12") || uInc.contains("12+")) {
            userLevel = 6;
        }

        // Check if scheme has "any income"
        if (eligLower.contains("any income")) {
            return new MatchResult(100, "✓ Open to all income groups");
        }

        // Check "below 1 lakh" / "below 1.5 lakh" / "bpl"
        if (eligLower.contains("below 1 lakh") || eligLower.contains("below ₹1 lakh") ||
                eligLower.contains("below 1.5") || eligLower.contains("bpl") ||
                eligLower.contains("secc listed")) {
            if (userLevel <= 1) {
                return new MatchResult(100, "✓ Income qualifies (Below ₹1 Lakh limit)");
            } else {
                return new MatchResult(0, "🔴 Annual family income exceeds scheme limit (Requires Below ₹1 Lakh).");
            }
        }

        // Check "below 2.5 lakh" / "below 3 lakh" / "below 2 lakh"
        if (eligLower.contains("2.5 lakh") || eligLower.contains("3 lakh") ||
                eligLower.contains("2 lakh") || eligLower.contains("below ₹2.5") ||
                eligLower.contains("below ₹3")) {
            if (userLevel <= 2) {
                return new MatchResult(100, "✓ Income qualifies (Below ₹3 Lakh limit)");
            } else {
                return new MatchResult(0, "🔴 Annual family income (" + userIncome + ") exceeds scheme limit (Requires Below ₹3 Lakh).");
            }
        }

        // Check "below 5 lakh" / "below 4 lakh"
        if (eligLower.contains("5 lakh") || eligLower.contains("4 lakh") || eligLower.contains("below ₹5")) {
            if (userLevel <= 3) {
                return new MatchResult(100, "✓ Income qualifies (Within ₹5 Lakh limit)");
            } else {
                return new MatchResult(0, "🔴 Annual family income (" + userIncome + ") exceeds scheme limit (Requires Below ₹5 Lakh).");
            }
        }

        // Check "below 8 lakh" / "below 6 lakh"
        if (eligLower.contains("8 lakh") || eligLower.contains("6 lakh") || eligLower.contains("below ₹8")) {
            if (userLevel <= 4) {
                return new MatchResult(100, "✓ Income qualifies (Within ₹8 Lakh limit)");
            } else {
                return new MatchResult(0, "🔴 Annual family income (" + userIncome + ") exceeds scheme limit (Requires Below ₹8 Lakh).");
            }
        }

        // Check "not an income taxpayer"
        if (eligLower.contains("not an income taxpayer") || eligLower.contains("non-taxpayer")) {
            if (userLevel <= 3) {
                return new MatchResult(100, "✓ Income qualifies (Non-taxpayer)");
            } else {
                return new MatchResult(0, "🔴 Scheme is only for non-income taxpayers.");
            }
        }

        return new MatchResult(100, "✓ Income criteria met");
    }

    /**
     * Returns a status label based on match percentage.
     */
    public static String getStatusLabel(int matchPercentage) {
        if (matchPercentage >= THRESHOLD_LIKELY) {
            return "🟢 Likely Eligible";
        } else if (matchPercentage >= THRESHOLD_POSSIBLY) {
            return "🟡 Possibly Eligible";
        } else if (matchPercentage >= THRESHOLD_VERIFY) {
            return "⚠️ Needs Verification";
        } else {
            return "🔴 Not Eligible";
        }
    }
}
