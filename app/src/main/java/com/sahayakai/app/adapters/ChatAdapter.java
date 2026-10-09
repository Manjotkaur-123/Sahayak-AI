package com.sahayakai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ChatAdapter — displays chat messages in the RecyclerView.
 *
 * HOW IT WORKS:
 * - Two view types: VIEW_TYPE_USER and VIEW_TYPE_AI.
 * - User messages appear on the RIGHT with blue bubble.
 * - AI messages appear on the LEFT with gray bubble.
 * - getItemViewType() decides which layout to inflate.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_message_user, parent, false);
            return new UserMessageHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_message_ai, parent, false);
            return new AiMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String time = formatTime(message.getTimestamp());

        if (holder instanceof UserMessageHolder) {
            UserMessageHolder userHolder = (UserMessageHolder) holder;
            userHolder.tvMessage.setText(message.getMessage());
            userHolder.tvTime.setText(time);
        } else if (holder instanceof AiMessageHolder) {
            AiMessageHolder aiHolder = (AiMessageHolder) holder;
            aiHolder.tvMessage.setText(message.getMessage());
            aiHolder.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ViewHolder for user messages (right-aligned, blue bubble)
    static class UserMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        UserMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    // ViewHolder for AI messages (left-aligned, gray bubble)
    static class AiMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        AiMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
