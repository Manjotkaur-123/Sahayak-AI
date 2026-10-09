package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sahayakai.app.R;
import com.sahayakai.app.adapters.ChatAdapter;
import com.sahayakai.app.helpers.GeminiHelper;
import com.sahayakai.app.helpers.SchemeMatchHelper;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.ChatMessage;
import com.sahayakai.app.models.Scheme;
import com.sahayakai.app.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatActivity — the main AI chatbot screen.
 *
 * HOW IT WORKS:
 * 1. Load user profile from Supabase (for personalized recommendations).
 * 2. Load all schemes from Supabase.
 * 3. User types a question.
 * 4. We send the question + profile + relevant schemes to Gemini API.
 * 5. Gemini responds with a helpful recommendation.
 * 6. Every message is saved to Supabase chat history.
 */
public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private View tvThinking;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private BottomNavigationView bottomNav;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();

    private SupabaseHelper supabase;
    private GeminiHelper geminiHelper;
    private User currentUser;
    private List<Scheme> allSchemes = new ArrayList<>();

    private String currentSessionId = null; // Current chat session ID

    // Main thread handler for UI updates from background threads
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        supabase = SupabaseHelper.getInstance();
        geminiHelper = new GeminiHelper();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sahayak AI");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recyclerChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvThinking = findViewById(R.id.tv_thinking);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);

        // History button
        findViewById(R.id.btn_history).setOnClickListener(v ->
                startActivity(new Intent(this, ChatHistoryActivity.class)));

        // New Chat button
        findViewById(R.id.btn_new_chat).setOnClickListener(v -> startNewChat());

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Messages scroll from bottom
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);

        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        setupBottomNav();

        // Load profile and schemes
        loadDataAndStart();
    }

    private void loadDataAndStart() {
        setLoading(true);

        // Load user profile
        supabase.getProfile(new SupabaseHelper.OnProfileCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                // Load schemes next
                loadSchemes();
            }
            @Override
            public void onFailure(String error) {
                currentUser = null;
                loadSchemes();
            }
        });
    }

    private void loadSchemes() {
        supabase.getSchemes(new SupabaseHelper.OnSchemesCallback() {
            @Override
            public void onSuccess(List<Scheme> schemes) {
                allSchemes.clear();
                allSchemes.addAll(schemes);
                mainHandler.post(() -> setLoading(false));

                // Check if we have a pre-filled query from HomeActivity
                String quickQuery = getIntent().getStringExtra("quickQuery");

                // Check if we're continuing an old session
                String continueSessionId = getIntent().getStringExtra("sessionId");

                if (continueSessionId != null) {
                    currentSessionId = continueSessionId;
                    loadSessionMessages(continueSessionId);
                } else {
                    // Create a new chat session asynchronously
                    supabase.createChatSession("New Conversation", sessionId -> {
                        currentSessionId = sessionId;
                        if (quickQuery != null && !quickQuery.isEmpty()) {
                            mainHandler.post(() -> processUserMessage(quickQuery));
                        } else {
                            mainHandler.post(() -> showWelcomeMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    setLoading(false);
                    showWelcomeMessage();
                    Toast.makeText(ChatActivity.this,
                            "Warning: Could not load schemes from database.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadSessionMessages(String sessionId) {
        setLoading(true);
        supabase.getChatMessages(sessionId, new SupabaseHelper.OnChatMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> loadedMessages) {
                mainHandler.post(() -> {
                    setLoading(false);
                    messages.clear();
                    messages.addAll(loadedMessages);
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom();
                    updateEmptyState();
                });
            }
            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    setLoading(false);
                    Toast.makeText(ChatActivity.this,
                            "Could not load chat: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showWelcomeMessage() {
        String welcomeText;
        if (currentUser != null) {
            String name = currentUser.getName();
            String firstName = (name != null && !name.isEmpty()) ? name.split(" ")[0] : "there";
            welcomeText = "Namaste " + firstName + "! 🙏\n\nI am Sahayak AI. I can help you find government schemes and scholarships that match your profile.\n\n" +
                    "You can ask me things like:\n" +
                    "• \"Show me scholarships I can apply for\"\n" +
                    "• \"What schemes are available for women?\"\n" +
                    "• \"Tell me about Ayushman Bharat\"\n" +
                    "• \"How do I apply for PM Kisan?\"\n\n" +
                    "How can I help you today?";
        } else {
            welcomeText = "Namaste! 🙏\n\nI am Sahayak AI. Please complete your profile first for personalized scheme recommendations.\n\nYou can still ask me general questions about government schemes!";
        }
        addAIMessage(welcomeText, false); // Don't save welcome message to Firebase
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        etMessage.setText(""); // Clear input
        processUserMessage(messageText);
    }

    private void processUserMessage(String messageText) {
        // Add user message to UI
        ChatMessage userMessage = new ChatMessage(
                ChatMessage.SENDER_USER, messageText, System.currentTimeMillis());
        addMessageToUI(userMessage);

        // Save user message to Supabase
        if (currentSessionId != null) {
            supabase.saveChatMessage(currentSessionId, userMessage,
                    new SupabaseHelper.OnCompleteCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });

            // Auto-generate title from first user message
            if (messages.size() == 1 || (messages.size() == 2
                    && messages.get(0).getSender().equals("assistant")
                    && messages.get(0).getMessage().startsWith("Namaste"))) {
                geminiHelper.generateChatTitle(messageText, new GeminiHelper.GeminiCallback() {
                    @Override
                    public void onResponse(String response) {
                        mainHandler.post(() ->
                                supabase.updateChatTitle(currentSessionId, response));
                    }
                    @Override
                    public void onError(String error) {
                        // Ignore title generation errors
                    }
                });
            }
        }

        // Show "thinking" indicator
        setThinking(true);

        // Get relevant schemes based on query + user profile matching
        List<Scheme> relevantSchemes = getRelevantSchemes(messageText);

        // Call Gemini API with profile context
        geminiHelper.askGemini(messageText, currentUser, relevantSchemes, messages,
                new GeminiHelper.GeminiCallback() {
                    @Override
                    public void onResponse(String response) {
                        mainHandler.post(() -> {
                            setThinking(false);
                            ChatMessage aiMessage = new ChatMessage(
                                    ChatMessage.SENDER_AI, response, System.currentTimeMillis());
                            addMessageToUI(aiMessage);

                            // Save AI response to Supabase
                            if (currentSessionId != null) {
                                supabase.saveChatMessage(currentSessionId, aiMessage,
                                        new SupabaseHelper.OnCompleteCallback() {
                                            @Override public void onSuccess() {}
                                            @Override public void onFailure(String error) {}
                                        });
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        mainHandler.post(() -> {
                            setThinking(false);
                            String errorText = "Sorry, I couldn't connect to the AI. 😔\n\n" +
                                    "Possible reasons:\n" +
                                    "• No internet connection\n" +
                                    "• Invalid API key (check gradle.properties)\n\n" +
                                    "Error: " + errorMessage;
                            addAIMessage(errorText, true);
                        });
                    }
                });
    }

    /**
     * Gets the most relevant schemes based on the user's message keywords.
     * This narrows down what we send to Gemini to keep the prompt focused.
     */
    private List<Scheme> getRelevantSchemes(String query) {
        if (allSchemes.isEmpty()) return new ArrayList<>();

        // First match and rank by user profile
        List<Scheme> ranked = SchemeMatchHelper.matchAndRank(currentUser, new ArrayList<>(allSchemes));

        // Then filter by keywords in the query
        String queryLower = query.toLowerCase();

        // Detect category keywords
        String categoryFilter = null;
        if (queryLower.contains("scholarship") || queryLower.contains("education") ||
                queryLower.contains("study") || queryLower.contains("student")) {
            categoryFilter = "Education";
        } else if (queryLower.contains("women") || queryLower.contains("girl") ||
                queryLower.contains("female") || queryLower.contains("mahila")) {
            categoryFilter = "Women";
        } else if (queryLower.contains("employment") || queryLower.contains("job") ||
                queryLower.contains("work") || queryLower.contains("rozgar")) {
            categoryFilter = "Employment";
        } else if (queryLower.contains("health") || queryLower.contains("hospital") ||
                queryLower.contains("medical") || queryLower.contains("swasthya")) {
            categoryFilter = "Healthcare";
        } else if (queryLower.contains("farm") || queryLower.contains("kisan") ||
                queryLower.contains("agriculture") || queryLower.contains("crop")) {
            categoryFilter = "Agriculture";
        } else if (queryLower.contains("housing") || queryLower.contains("house") ||
                queryLower.contains("awas") || queryLower.contains("home")) {
            categoryFilter = "Housing";
        } else if (queryLower.contains("skill") || queryLower.contains("training") ||
                queryLower.contains("kaushal")) {
            categoryFilter = "Skill Development";
        } else if (queryLower.contains("startup") || queryLower.contains("business") ||
                queryLower.contains("loan") || queryLower.contains("mudra")) {
            categoryFilter = "Entrepreneurship";
        }

        // Also try keyword filtering
        List<Scheme> keywordFiltered = SchemeMatchHelper.filterByKeyword(ranked, query);

        // Combine: category-specific first, then keyword matches, then all top-ranked
        List<Scheme> result = new ArrayList<>();
        if (categoryFilter != null) {
            result.addAll(SchemeMatchHelper.filterByCategory(ranked, categoryFilter));
        }
        for (Scheme s : keywordFiltered) {
            if (!result.contains(s)) result.add(s);
        }
        for (Scheme s : ranked) {
            if (!result.contains(s)) result.add(s);
        }

        // Return top 8 schemes for context
        return result.subList(0, Math.min(8, result.size()));
    }

    private void addMessageToUI(ChatMessage message) {
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        updateEmptyState();
    }

    private void addAIMessage(String text, boolean saveToSupabase) {
        ChatMessage aiMsg = new ChatMessage(ChatMessage.SENDER_AI, text, System.currentTimeMillis());
        addMessageToUI(aiMsg);
        if (saveToSupabase && currentSessionId != null) {
            supabase.saveChatMessage(currentSessionId, aiMsg,
                    new SupabaseHelper.OnCompleteCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });
        }
    }

    private void startNewChat() {
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        supabase.createChatSession("New Conversation", sessionId -> {
            currentSessionId = sessionId;
            mainHandler.post(this::showWelcomeMessage);
        });
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerChat.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private void updateEmptyState() {
        tvEmpty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setThinking(boolean thinking) {
        tvThinking.setVisibility(thinking ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!thinking);
        etMessage.setEnabled(!thinking);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_chat);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
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
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
