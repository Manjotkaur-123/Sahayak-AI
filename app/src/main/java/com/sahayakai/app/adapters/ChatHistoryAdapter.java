package com.sahayakai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.models.ChatSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ChatHistoryAdapter — displays a list of previous chat sessions.
 *
 * Each item shows:
 * - Chat title (auto-generated from first message)
 * - Date/time the chat was created
 * Tapping opens that chat in ChatActivity.
 */
public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }

    public interface OnSessionDeleteListener {
        void onDeleteClick(ChatSession session, int position);
    }

    private Context context;
    private List<ChatSession> sessions;
    private OnSessionClickListener clickListener;
    private OnSessionDeleteListener deleteListener;

    public ChatHistoryAdapter(Context context, List<ChatSession> sessions,
                              OnSessionClickListener clickListener,
                              OnSessionDeleteListener deleteListener) {
        this.context = context;
        this.sessions = sessions;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.tvTitle.setText(session.getTitle() != null ? session.getTitle() : "Conversation");
        holder.tvDate.setText(formatDate(session.getCreatedAt()));
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSessionClick(session);
            }
        });

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick(session, pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        View btnDelete;

        SessionViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_session_title);
            tvDate = itemView.findViewById(R.id.tv_session_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_session);
        }
    }
}
