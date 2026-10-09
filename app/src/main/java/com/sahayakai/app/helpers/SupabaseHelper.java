package com.sahayakai.app.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.sahayakai.app.BuildConfig;
import com.sahayakai.app.models.ApplicationModel;
import com.sahayakai.app.models.ChatMessage;
import com.sahayakai.app.models.ChatSession;
import com.sahayakai.app.models.NotificationModel;
import com.sahayakai.app.models.Scheme;
import com.sahayakai.app.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * SupabaseHelper — replaces FirebaseHelper entirely.
 *
 * HOW IT WORKS (for viva/explanation):
 * - Supabase exposes its PostgreSQL database as a REST API.
 * - We call this REST API using OkHttp (the same library used for Gemini API).
 * - Authentication uses Supabase Auth REST endpoints (signup, login).
 * - After login, we get a JWT access token and save it in SharedPreferences.
 * - Every database request sends this token as: Authorization: Bearer <token>
 * - Row Level Security (RLS) on Supabase ensures users can only see their own data.
 *
 * CALLBACK INTERFACES are identical to FirebaseHelper — so Activities need minimal changes.
 *
 * NEVER put the service_role key here. Only SUPABASE_ANON_KEY (public key) is used.
 */
public class SupabaseHelper {

    // ================================================================
    // CONFIGURATION — values injected from gradle.properties at build time
    // ================================================================
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // SharedPreferences key names for session storage
    private static final String PREFS_NAME = "sahayak_supabase_session";
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";

    // JSON media type for POST requests
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Singleton instance
    private static SupabaseHelper instance;

    private final OkHttpClient httpClient;
    private final SharedPreferences prefs;

    private SupabaseHelper(Context context) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // Use application context to avoid memory leaks
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Call this once in your Application class or first Activity.
     * After that, use getInstance() anywhere.
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SupabaseHelper(context);
        }
    }

    public static synchronized SupabaseHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "SupabaseHelper not initialized. Call SupabaseHelper.init(context) first.");
        }
        return instance;
    }

    // ==============================================================
    // SESSION MANAGEMENT (replaces FirebaseAuth state)
    // ==============================================================

    /** Returns true if user is currently logged in (valid token and userId stored). */
    public boolean isLoggedIn() {
        String token = prefs.getString(PREF_ACCESS_TOKEN, null);
        String uid = prefs.getString(PREF_USER_ID, null);
        return token != null && !token.isEmpty() && uid != null && !uid.isEmpty();
    }

    /** Returns current user's UUID (stored after login). */
    public String getCurrentUserId() {
        return prefs.getString(PREF_USER_ID, null);
    }

    /** Returns current user's email (stored after login). */
    public String getCurrentUserEmail() {
        return prefs.getString(PREF_USER_EMAIL, null);
    }

    /** Clears stored session (logout). */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /** Saves session data after login/signup. */
    private void saveSession(String accessToken, String refreshToken, String userId, String email) {
        prefs.edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .putString(PREF_REFRESH_TOKEN, refreshToken)
                .putString(PREF_USER_ID, userId)
                .putString(PREF_USER_EMAIL, email != null ? email : "")
                .apply();
    }

    /** Gets current access token for Authorization header. */
    private String getAccessToken() {
        return prefs.getString(PREF_ACCESS_TOKEN, "");
    }

    // ==============================================================
    // HTTP REQUEST BUILDERS
    // ==============================================================

    /**
     * Builds a GET request to Supabase REST API.
     * @param path  e.g. "/rest/v1/profiles?id=eq.UUID&select=*"
     */
    private Request buildGetRequest(String path) {
        return new Request.Builder()
                .url(SUPABASE_URL + path)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();
    }

    /**
     * Builds a POST request to Supabase REST API.
     * @param path  e.g. "/rest/v1/profiles"
     * @param json  JSON body string
     */
    private Request buildPostRequest(String path, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        return new Request.Builder()
                .url(SUPABASE_URL + path)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation") // Return created row
                .post(body)
                .build();
    }

    /**
     * Builds a PATCH (update) request to Supabase REST API.
     */
    private Request buildPatchRequest(String path, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        return new Request.Builder()
                .url(SUPABASE_URL + path)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();
    }

    /**
     * Builds a DELETE request to Supabase REST API.
     */
    private Request buildDeleteRequest(String path) {
        return new Request.Builder()
                .url(SUPABASE_URL + path)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();
    }

    // ==============================================================
    // CALLBACK INTERFACES (identical to FirebaseHelper)
    // ==============================================================

    public interface OnProfileCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnSchemesCallback {
        void onSuccess(List<Scheme> schemes);
        void onFailure(String error);
    }

    public interface OnChatSessionsCallback {
        void onSuccess(List<ChatSession> sessions);
        void onFailure(String error);
    }

    public interface OnChatMessagesCallback {
        void onSuccess(List<ChatMessage> messages);
        void onFailure(String error);
    }

    public interface OnNotificationsCallback {
        void onSuccess(List<NotificationModel> notifications);
        void onFailure(String error);
    }

    public interface OnApplicationCallback {
        void onSuccess(ApplicationModel application);
        void onFailure(String error);
    }

    public interface OnCompleteCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnProfileExistsCallback {
        void onResult(boolean exists);
    }

    public interface OnSavedSchemesCallback {
        void onSuccess(List<String> schemeIds);
        void onFailure(String error);
    }

    public interface OnSchemeSavedCallback {
        void onResult(boolean isSaved);
    }

    public interface OnAuthCallback {
        void onSuccess(String userId, String email);
        void onFailure(String error);
    }

    // ==============================================================
    // AUTHENTICATION (replaces FirebaseAuth)
    // ==============================================================

    /**
     * Sign up with email and password.
     * Supabase endpoint: POST /auth/v1/signup
     */
    public void signUp(String email, String password, OnAuthCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (!response.isSuccessful()) {
                            String msg = json.optString("msg",
                                    json.optString("error_description", "Sign up failed"));
                            callback.onFailure(msg);
                            return;
                        }
                        // Parse the user ID and email
                        String userId = json.optString("id", "");
                        String userEmail = json.optString("email", email);

                        // If user object is nested (standard Supabase format)
                        if (json.has("user")) {
                            JSONObject userObj = json.optJSONObject("user");
                            if (userObj != null) {
                                if (userId.isEmpty()) userId = userObj.optString("id", "");
                                if (userEmail.isEmpty() || userEmail.equals(email)) {
                                    userEmail = userObj.optString("email", email);
                                }
                            }
                        }

                        // Extract access token
                        String accessToken = json.optString("access_token", "");
                        String refreshToken = json.optString("refresh_token", "");
                        if (accessToken.isEmpty() && json.has("session")) {
                            JSONObject sessionObj = json.optJSONObject("session");
                            if (sessionObj != null) {
                                accessToken = sessionObj.optString("access_token", "");
                                refreshToken = sessionObj.optString("refresh_token", "");
                            }
                        }

                        if (!accessToken.isEmpty() && !userId.isEmpty()) {
                            saveSession(accessToken, refreshToken, userId, userEmail);
                            callback.onSuccess(userId, userEmail);
                        } else {
                            // If signup succeeded but didn't return session token, auto-sign in
                            signIn(email, password, callback);
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Sign in with email and password.
     * Supabase endpoint: POST /auth/v1/token?grant_type=password
     */
    public void signIn(String email, String password, OnAuthCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (!response.isSuccessful()) {
                            String msg = json.optString("error_description",
                                    json.optString("msg", "Login failed"));
                            callback.onFailure(msg);
                            return;
                        }
                        String accessToken = json.getString("access_token");
                        String refreshToken = json.optString("refresh_token", "");
                        // User info is inside the "user" object
                        JSONObject userObj = json.optJSONObject("user");
                        String userId = userObj != null ? userObj.optString("id", "") : "";
                        String userEmail = userObj != null ? userObj.optString("email", email) : email;
                        saveSession(accessToken, refreshToken, userId, userEmail);
                        callback.onSuccess(userId, userEmail);
                    } catch (JSONException e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Send password reset email.
     * Supabase endpoint: POST /auth/v1/recover
     */
    public void sendPasswordReset(String email, OnCompleteCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/recover")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed to send reset email");
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    // ==============================================================
    // USER PROFILE OPERATIONS (replaces Firebase users/{UID})
    // Table: profiles
    // ==============================================================

    /**
     * Save or update the user's profile.
     * Uses UPSERT (insert if new, update if exists) via POST with Prefer: resolution=merge-duplicates
     */
    public void saveProfile(User user, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            callback.onFailure("User not logged in. Please log in again.");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("id", uid);
            body.put("name", user.getName() != null ? user.getName() : "");
            body.put("email", getCurrentUserEmail());
            body.put("age", user.getAge() != null ? user.getAge() : "");
            body.put("gender", user.getGender() != null ? user.getGender() : "");
            body.put("state", user.getState() != null ? user.getState() : "");
            body.put("district", user.getDistrict() != null ? user.getDistrict() : "");
            body.put("income", user.getIncome() != null ? user.getIncome() : "");
            body.put("category", user.getCategory() != null ? user.getCategory() : "");
            body.put("education", user.getEducation() != null ? user.getEducation() : "");
            body.put("course", user.getCourse() != null ? user.getCourse() : "");
            body.put("occupation", user.getOccupation() != null ? user.getOccupation() : "");
            body.put("student_status", user.getStudentStatus() != null ? user.getStudentStatus() : "");
            body.put("profile_complete", true);

            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/profiles?on_conflict=id")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    android.util.Log.e("SahayakAI", "saveProfile network failure", e);
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body() != null ? response.body().string() : "";
                    android.util.Log.d("SahayakAI", "saveProfile response: code=" + response.code() + " body=" + respBody);
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Save failed (" + response.code() + "): " + respBody);
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Load the user's profile from Supabase.
     */
    public void getProfile(OnProfileCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest("/rest/v1/profiles?id=eq." + uid + "&select=*");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() == 0) {
                        callback.onSuccess(null); // No profile yet
                        return;
                    }
                    JSONObject obj = arr.getJSONObject(0);
                    User user = jsonToUser(obj);
                    callback.onSuccess(user);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /** Alias for getProfile */
    public void getUserProfile(OnProfileCallback callback) {
        getProfile(callback);
    }

    /**
     * Check if user has completed their profile.
     * Replaces Firebase checkProfileExists() — checks profile_complete field.
     */
    public void checkProfileExists(OnProfileExistsCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onResult(false);
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/profiles?id=eq." + uid + "&profile_complete=eq.true&select=id");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    callback.onResult(arr.length() > 0);
                } catch (JSONException e) {
                    callback.onResult(false);
                }
            }
        });
    }

    // ==============================================================
    // SCHEME OPERATIONS (replaces Firebase schemes/ node)
    // Table: schemes
    // ==============================================================

    /**
     * Fetch all schemes from Supabase.
     */
    public void getSchemes(OnSchemesCallback callback) {
        Request request = buildGetRequest("/rest/v1/schemes?select=*&order=name.asc");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    List<Scheme> schemes = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        Scheme s = jsonToScheme(arr.getJSONObject(i));
                        if (s != null) schemes.add(s);
                    }
                    callback.onSuccess(schemes);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================================================
    // CHAT SESSION OPERATIONS (replaces Firebase chats/{UID})
    // Tables: chat_sessions, chat_messages
    // ==============================================================

    /**
     * Create a new chat session. Returns the session ID string.
     * Unlike Firebase (synchronous push key), this is async — returns via callback.
     */
    public void createChatSession(String title, OnStringCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onResult(null);
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", uid);
            body.put("title", title);

            Request request = buildPostRequest("/rest/v1/chat_sessions", body.toString());
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body() != null ? response.body().string() : "[]";
                    try {
                        JSONArray arr = new JSONArray(respBody);
                        if (arr.length() > 0) {
                            String sessionId = arr.getJSONObject(0).getString("id");
                            callback.onResult(sessionId);
                        } else {
                            callback.onResult(null);
                        }
                    } catch (JSONException e) {
                        callback.onResult(null);
                    }
                }
            });
        } catch (JSONException e) {
            callback.onResult(null);
        }
    }

    public interface OnStringCallback {
        void onResult(String value);
    }

    /**
     * Update the title of a chat session.
     */
    public void updateChatTitle(String sessionId, String title) {
        try {
            JSONObject body = new JSONObject();
            body.put("title", title);
            Request request = buildPatchRequest(
                    "/rest/v1/chat_sessions?id=eq." + sessionId, body.toString());
            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}
                @Override public void onResponse(Call call, Response response) throws IOException {}
            });
        } catch (JSONException e) {
            // Ignore title update errors
        }
    }

    /**
     * Fetch all chat sessions for the current user.
     */
    public void getChatSessions(OnChatSessionsCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/chat_sessions?user_id=eq." + uid + "&select=*&order=created_at.desc");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    List<ChatSession> sessions = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        ChatSession session = new ChatSession(
                                obj.optString("id"),
                                obj.optString("title", "Conversation"),
                                parseTimestamp(obj.optString("created_at"))
                        );
                        sessions.add(session);
                    }
                    callback.onSuccess(sessions);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Delete a specific chat session (and all its messages via Postgres cascade delete).
     */
    public void deleteChatSession(String sessionId, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildDeleteRequest(
                "/rest/v1/chat_sessions?id=eq." + sessionId + "&user_id=eq." + uid);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Delete failed: HTTP " + response.code());
                }
            }
        });
    }

    /**
     * Save a chat message to a session.
     */
    public void saveChatMessage(String sessionId, ChatMessage message, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("session_id", sessionId);
            body.put("user_id", uid);
            body.put("sender", message.getSender());
            body.put("message", message.getMessage());

            Request request = buildPostRequest("/rest/v1/chat_messages", body.toString());
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Save failed: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Fetch all messages for a specific chat session.
     */
    public void getChatMessages(String sessionId, OnChatMessagesCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/chat_messages?session_id=eq." + sessionId
                        + "&user_id=eq." + uid
                        + "&select=*&order=created_at.asc");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    List<ChatMessage> messages = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        ChatMessage msg = new ChatMessage(
                                obj.optString("sender"),
                                obj.optString("message"),
                                parseTimestamp(obj.optString("created_at"))
                        );
                        messages.add(msg);
                    }
                    callback.onSuccess(messages);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================================================
    // SAVED SCHEMES OPERATIONS (replaces Firebase savedSchemes/{UID})
    // Table: saved_schemes
    // ==============================================================

    /**
     * Save a scheme for the user.
     */
    public void saveSchemeForUser(String schemeId, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", uid);
            body.put("scheme_id", schemeId);

            // Use UPSERT — if already saved, just ignore
            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/saved_schemes")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=ignore-duplicates,return=minimal")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Save failed: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Remove a saved scheme.
     */
    public void removeSavedScheme(String schemeId, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildDeleteRequest(
                "/rest/v1/saved_schemes?user_id=eq." + uid + "&scheme_id=eq." + schemeId);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Remove failed: " + response.code());
                }
            }
        });
    }

    /**
     * Get all saved scheme IDs for the user.
     */
    public void getSavedSchemeIds(OnSavedSchemesCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/saved_schemes?user_id=eq." + uid + "&select=scheme_id");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    List<String> ids = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        ids.add(arr.getJSONObject(i).getString("scheme_id"));
                    }
                    callback.onSuccess(ids);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Check if a specific scheme is already saved.
     */
    public void isSchemesSaved(String schemeId, OnSchemeSavedCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onResult(false);
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/saved_schemes?user_id=eq." + uid
                        + "&scheme_id=eq." + schemeId + "&select=id");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    callback.onResult(arr.length() > 0);
                } catch (JSONException e) {
                    callback.onResult(false);
                }
            }
        });
    }

    // ==============================================================
    // APPLICATION TRACKER (replaces Firebase applications/{UID})
    // Table: applications
    // ==============================================================

    /**
     * Save or update application status for a scheme.
     */
    public void saveApplication(ApplicationModel application, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", uid);
            body.put("scheme_id", application.getSchemeId());
            body.put("status", application.getStatus());

            RequestBody reqBody = RequestBody.create(body.toString(), JSON);
            // on_conflict=user_id,scheme_id ensures unlimited updates without duplicate key error
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/applications?on_conflict=user_id,scheme_id")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(reqBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        // Fallback to direct PATCH update if row already exists
                        try {
                            JSONObject patchBody = new JSONObject();
                            patchBody.put("status", application.getStatus());
                            Request patchReq = buildPatchRequest(
                                    "/rest/v1/applications?user_id=eq." + uid + "&scheme_id=eq." + application.getSchemeId(),
                                    patchBody.toString());
                            httpClient.newCall(patchReq).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call c, IOException ex) {
                                    callback.onFailure("Update failed: " + ex.getMessage());
                                }

                                @Override
                                public void onResponse(Call c, Response patchResp) throws IOException {
                                    if (patchResp.isSuccessful()) {
                                        callback.onSuccess();
                                    } else {
                                        callback.onFailure("Status update failed: " + patchResp.code());
                                    }
                                }
                            });
                        } catch (JSONException je) {
                            callback.onFailure("Save failed: " + response.code());
                        }
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Get application status for a specific scheme.
     */
    public void getApplicationStatus(String schemeId, OnApplicationCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/applications?user_id=eq." + uid + "&scheme_id=eq." + schemeId + "&select=*");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() == 0) {
                        callback.onSuccess(null);
                        return;
                    }
                    JSONObject obj = arr.getJSONObject(0);
                    ApplicationModel app = new ApplicationModel();
                    app.setSchemeId(obj.optString("scheme_id"));
                    app.setStatus(obj.optString("status"));
                    app.setUpdatedAt(parseTimestamp(obj.optString("updated_at")));
                    callback.onSuccess(app);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================================================
    // NOTIFICATIONS (replaces Firebase notifications/{UID})
    // Table: notifications
    // ==============================================================

    /**
     * Save a notification.
     */
    public void saveNotification(NotificationModel notification, OnCompleteCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", uid);
            body.put("title", notification.getTitle() != null ? notification.getTitle() : "");
            body.put("message", notification.getMessage() != null ? notification.getMessage() : "");
            body.put("is_read", false);

            Request request = buildPostRequest("/rest/v1/notifications", body.toString());
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Save failed: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onFailure("Build error: " + e.getMessage());
        }
    }

    /**
     * Fetch all notifications for the user.
     */
    public void getNotifications(OnNotificationsCallback callback) {
        String uid = getCurrentUserId();
        if (uid == null) {
            callback.onFailure("User not logged in");
            return;
        }
        Request request = buildGetRequest(
                "/rest/v1/notifications?user_id=eq." + uid + "&select=*&order=created_at.desc");
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "[]";
                try {
                    JSONArray arr = new JSONArray(body);
                    List<NotificationModel> notifications = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        NotificationModel notif = new NotificationModel();
                        notif.setNotifId(obj.optString("id"));
                        notif.setTitle(obj.optString("title"));
                        notif.setMessage(obj.optString("message"));
                        notif.setRead(obj.optBoolean("is_read", false));
                        notif.setTimestamp(parseTimestamp(obj.optString("created_at")));
                        notifications.add(notif);
                    }
                    callback.onSuccess(notifications);
                } catch (JSONException e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Mark a notification as read.
     */
    public void markNotificationRead(String notifId) {
        try {
            JSONObject body = new JSONObject();
            body.put("is_read", true);
            Request request = buildPatchRequest(
                    "/rest/v1/notifications?id=eq." + notifId, body.toString());
            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}
                @Override public void onResponse(Call call, Response response) throws IOException {}
            });
        } catch (JSONException e) {
            // Ignore
        }
    }

    // ==============================================================
    // HELPER: Supabase Scheme Seeding (called by SchemeDatabaseHelper)
    // ==============================================================

    /**
     * Insert schemes into Supabase (used for initial data population).
     * Uses UPSERT so running it multiple times is safe.
     */
    public void upsertScheme(JSONObject schemeJson, OnCompleteCallback callback) {
        RequestBody reqBody = RequestBody.create(schemeJson.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/schemes")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(reqBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    if (response.isSuccessful()) callback.onSuccess();
                    else callback.onFailure("Status: " + response.code());
                }
            }
        });
    }

    // ==============================================================
    // JSON PARSING HELPERS
    // ==============================================================

    /** Convert a Supabase profiles row (JSONObject) to a User model. */
    private User jsonToUser(JSONObject obj) throws JSONException {
        User user = new User();
        user.setUid(obj.optString("id"));
        user.setName(obj.optString("name"));
        user.setEmail(obj.optString("email"));
        user.setAge(obj.optString("age"));
        user.setGender(obj.optString("gender"));
        user.setState(obj.optString("state"));
        user.setDistrict(obj.optString("district"));
        user.setIncome(obj.optString("income"));
        user.setCategory(obj.optString("category"));
        user.setEducation(obj.optString("education"));
        user.setCourse(obj.optString("course"));
        user.setOccupation(obj.optString("occupation"));
        user.setStudentStatus(obj.optString("student_status"));
        return user;
    }

    /** Convert a Supabase schemes row (JSONObject) to a Scheme model. */
    private Scheme jsonToScheme(JSONObject obj) {
        try {
            Scheme s = new Scheme();
            s.setId(obj.optString("id"));
            s.setName(obj.optString("name"));
            s.setSummary(obj.optString("summary"));
            s.setEligibility(obj.optString("eligibility"));
            s.setCategory(obj.optString("category"));
            s.setOfficialLink(obj.optString("official_link"));
            s.setApplyLink(obj.optString("apply_link"));
            s.setGuidelinesLink(obj.optString("guidelines_link"));
            s.setDeadline(obj.optString("deadline"));

            // Parse JSON arrays
            s.setBenefits(jsonArrayToList(obj.optJSONArray("benefits")));
            s.setDocuments(jsonArrayToList(obj.optJSONArray("documents")));
            s.setApplicationSteps(jsonArrayToList(obj.optJSONArray("application_steps")));
            s.setStates(jsonArrayToList(obj.optJSONArray("states")));
            s.setGenders(jsonArrayToList(obj.optJSONArray("genders")));
            s.setCategories(jsonArrayToList(obj.optJSONArray("categories")));
            s.setEducationLevels(jsonArrayToList(obj.optJSONArray("education_levels")));

            if (!obj.isNull("min_age")) s.setMinAge(obj.optInt("min_age", 0));
            if (!obj.isNull("max_age")) s.setMaxAge(obj.optInt("max_age", 99));
            s.setMaxIncome(obj.optString("max_income"));

            return s;
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> jsonArrayToList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.optString(i));
        }
        return list;
    }

    /**
     * Parses a Supabase ISO 8601 timestamp string to epoch milliseconds.
     * e.g. "2024-01-15T10:30:00.000000+00:00" → long millis
     */
    private long parseTimestamp(String isoString) {
        if (isoString == null || isoString.isEmpty()) return System.currentTimeMillis();
        try {
            // Java 8+ SimpleDateFormat approach
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            // Remove microseconds and timezone suffix for simple parsing
            String trimmed = isoString.replaceAll("\\.[0-9]+.*$", "");
            return sdf.parse(trimmed).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}
