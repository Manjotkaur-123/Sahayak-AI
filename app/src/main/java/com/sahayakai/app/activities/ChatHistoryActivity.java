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
import com.sahayakai.app.adapters.ChatHistoryAdapter;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.ChatSession;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatHistoryActivity — shows all previous chat sessions.
 *
 * HOW IT WORKS:
 * - Loads all chat sessions for the current user from Supabase.
 * - Displays them grouped (sorted by date, newest first).
 * - Tapping a session opens ChatActivity and loads that session's messages.
 */
public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private TextView tvEmpty;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvEmpty = findViewById(R.id.tv_empty);
        recyclerHistory = findViewById(R.id.recycler_history);

        adapter = new ChatHistoryAdapter(
                this,
                sessions,
                session -> {
                    // Open the selected chat session
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("sessionId", session.getSessionId());
                    startActivity(intent);
                },
                (session, position) -> {
                    // Confirm and delete chat session
                    confirmDeleteChat(session, position);
                }
        );
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(adapter);

        loadChatHistory();
    }

    private void confirmDeleteChat(ChatSession session, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this chat conversation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteChat(session, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteChat(ChatSession session, int position) {
        SupabaseHelper.getInstance().deleteChatSession(session.getSessionId(), new SupabaseHelper.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    if (position >= 0 && position < sessions.size()) {
                        sessions.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, sessions.size());
                    } else {
                        loadChatHistory();
                    }
                    tvEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(ChatHistoryActivity.this, "Chat deleted", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatHistoryActivity.this, "Failed to delete: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadChatHistory() {
        SupabaseHelper.getInstance().getChatSessions(new SupabaseHelper.OnChatSessionsCallback() {
            @Override
            public void onSuccess(List<ChatSession> chatSessions) {
                runOnUiThread(() -> {
                    sessions.clear();
                    sessions.addAll(chatSessions);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatHistoryActivity.this,
                            "Could not load history: " + error, Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
