package com.sahayakai.app.helpers;

import com.sahayakai.app.BuildConfig;
import com.sahayakai.app.models.Scheme;
import com.sahayakai.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * GeminiHelper — handles all communication with Google Gemini API.
 * Includes automatic multi-model fallback (e.g. if one model is overloaded,
 * it instantly tries an alternate model).
 */
public class GeminiHelper {

    // Available Gemini models in priority order
    private static final String[] MODELS = {
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-3.5-flash"
    };

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String apiKey;

    public interface GeminiCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    public GeminiHelper() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.apiKey = BuildConfig.GEMINI_API_KEY;
    }

    /**
     * Main method — sends user query to Gemini with full profile & scheme context.
     */
    public void askGemini(
            String userQuery,
            User userProfile,
            List<Scheme> schemes,
            List<com.sahayakai.app.models.ChatMessage> chatHistory,
            GeminiCallback callback) {

        String prompt = buildPrompt(userQuery, userProfile, schemes, chatHistory);
        executeWithFallback(prompt, 0, callback);
    }

    /**
     * Attempts to call Gemini with the model at modelIndex.
     * If high demand / 503 / 429 occurs, it automatically falls back to the next available model.
     */
    private void executeWithFallback(String prompt, int modelIndex, GeminiCallback callback) {
        if (modelIndex >= MODELS.length) {
            callback.onError("All AI models are currently busy. Please try again in a few moments.");
            return;
        }

        String modelName = MODELS[modelIndex];
        String url = BASE_URL + modelName + ":generateContent?key=" + apiKey;
        String requestBodyJson = buildRequestJson(prompt);

        android.util.Log.d("SahayakAI", "Calling model: " + modelName);

        RequestBody body = RequestBody.create(requestBodyJson, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("SahayakAI", "Network failure on model " + modelName, e);
                // On network timeout or connection reset, try next model
                if (modelIndex + 1 < MODELS.length) {
                    executeWithFallback(prompt, modelIndex + 1, callback);
                } else {
                    callback.onError("Network error: " + e.getMessage() +
                            "\n\nPlease check your internet connection.");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                int code = response.code();

                android.util.Log.d("SahayakAI", "Model " + modelName + " responded code=" + code);

                // If 503 (high demand), 429 (rate limit), or 404 (model unavailable), fallback to next model
                if (code == 503 || code == 429 || code == 404 || code == 500) {
                    if (modelIndex + 1 < MODELS.length) {
                        android.util.Log.w("SahayakAI", "Model " + modelName + " busy (code " + code + "), trying next model...");
                        executeWithFallback(prompt, modelIndex + 1, callback);
                        return;
                    }
                }

                if (!response.isSuccessful()) {
                    String detailMsg = "HTTP " + code;
                    try {
                        JSONObject errJson = new JSONObject(responseBody);
                        if (errJson.has("error")) {
                            JSONObject errObj = errJson.getJSONObject("error");
                            detailMsg = errObj.optString("message", detailMsg);
                        }
                    } catch (Exception ignored) {}
                    callback.onError(detailMsg);
                    return;
                }

                if (responseBody.isEmpty()) {
                    callback.onError("Empty response received from Gemini API.");
                    return;
                }

                String parsedText = parseGeminiResponse(responseBody);
                callback.onResponse(parsedText);
            }
        });
    }

    /**
     * Builds the full prompt to send to Gemini.
     */
    private String buildPrompt(
            String userQuery,
            User userProfile,
            List<Scheme> schemes,
            List<com.sahayakai.app.models.ChatMessage> chatHistory) {

        StringBuilder prompt = new StringBuilder();

        // ==== SYSTEM INSTRUCTIONS ====
        prompt.append("You are Sahayak AI, a friendly and helpful assistant that helps Indian citizens ");
        prompt.append("find and understand government schemes. You are integrated into a mobile app.\n\n");

        prompt.append("YOUR RULES:\n");
        prompt.append("1. ONLY recommend schemes from the 'Available Schemes Database' section below. ");
        prompt.append("Do NOT invent or assume scheme details not in the database.\n");
        prompt.append("2. Explain things in simple, easy language. Use English, Hindi, or Hinglish based on user's language.\n");
        prompt.append("3. Be warm, encouraging, and conversational.\n");
        prompt.append("4. For each recommended scheme, explain WHY the user may be eligible based on their profile.\n");
        prompt.append("5. Always add this disclaimer: '⚠️ Final eligibility must be verified from the official government authority.'\n");
        prompt.append("6. If the user asks about documents, deadlines, or how to apply, use the scheme data provided.\n");
        prompt.append("7. If no schemes match, suggest the user update their profile for better recommendations.\n\n");

        // ==== USER PROFILE ====
        prompt.append("=== USER PROFILE ===\n");
        if (userProfile != null) {
            prompt.append(userProfile.toProfileSummary());
        } else {
            prompt.append("Profile not set yet. Ask the user to complete their profile for better recommendations.\n");
        }
        prompt.append("\n\n");

        // ==== AVAILABLE SCHEMES DATABASE ====
        prompt.append("=== AVAILABLE SCHEMES DATABASE ===\n");
        if (schemes != null && !schemes.isEmpty()) {
            for (int i = 0; i < schemes.size(); i++) {
                prompt.append("--- Scheme ").append(i + 1).append(" ---\n");
                prompt.append(schemes.get(i).toPromptSummary());
                prompt.append("\n");
            }
        } else {
            prompt.append("No schemes currently in database. Tell the user you are looking for relevant schemes.\n");
        }
        prompt.append("\n");

        // ==== CHAT HISTORY (last 6 messages for context) ====
        if (chatHistory != null && chatHistory.size() > 1) {
            prompt.append("=== RECENT CONVERSATION HISTORY ===\n");
            int start = Math.max(0, chatHistory.size() - 7);
            for (int i = start; i < chatHistory.size() - 1; i++) {
                com.sahayakai.app.models.ChatMessage msg = chatHistory.get(i);
                if (msg.isUserMessage()) {
                    prompt.append("User: ").append(msg.getMessage()).append("\n");
                } else {
                    prompt.append("Sahayak AI: ").append(msg.getMessage()).append("\n");
                }
            }
            prompt.append("\n");
        }

        // ==== CURRENT USER QUESTION ====
        prompt.append("=== USER'S CURRENT QUESTION ===\n");
        prompt.append("User: ").append(userQuery).append("\n\n");
        prompt.append("Sahayak AI: ");

        return prompt.toString();
    }

    private String buildRequestJson(String prompt) {
        try {
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray partsArray = new JSONArray();
            partsArray.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(content);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contentsArray);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 2048);
            requestBody.put("generationConfig", generationConfig);

            return requestBody.toString();
        } catch (JSONException e) {
            return "{\"contents\":[{\"parts\":[{\"text\":\"" +
                    prompt.replace("\"", "\\\"").replace("\n", "\\n") +
                    "\"}]}]}";
        }
    }

    private String parseGeminiResponse(String responseJson) {
        try {
            JSONObject jsonResponse = new JSONObject(responseJson);
            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content")) {
                        JSONObject content = firstCandidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < parts.length(); i++) {
                                JSONObject part = parts.getJSONObject(i);
                                if (part.has("text")) {
                                    result.append(part.getString("text"));
                                }
                            }
                            if (result.length() > 0) {
                                return result.toString();
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            return "Sorry, I had trouble understanding the response. Please try again.";
        }
        return "I didn't get a response. Please try again.";
    }

    public void generateChatTitle(String firstMessage, GeminiCallback callback) {
        String prompt = "Generate a short 3-5 word chat title (in English) for this question: " +
                "\"" + firstMessage + "\". Reply with ONLY the title, nothing else.";

        executeWithFallback(prompt, 0, new GeminiCallback() {
            @Override
            public void onResponse(String response) {
                String title = response.replaceAll("[\"']", "").trim();
                if (title.length() > 40) title = title.substring(0, 40);
                callback.onResponse(title);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onResponse("New Conversation");
            }
        });
    }
}
