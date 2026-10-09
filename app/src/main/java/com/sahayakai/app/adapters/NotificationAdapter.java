package com.sahayakai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.models.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * NotificationAdapter — displays notifications in a RecyclerView.
 *
 * Unread notifications have highlighted background.
 * Tapping marks them as read.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    public interface OnNotifClickListener {
        void onClick(NotificationModel notif);
    }

    private Context context;
    private List<NotificationModel> notifications;
    private OnNotifClickListener listener;

    public NotificationAdapter(Context context, List<NotificationModel> notifications,
                               OnNotifClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationModel notif = notifications.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        holder.tvTime.setText(formatTime(notif.getTimestamp()));

        // Unread notifications have a distinct background
        if (!notif.isRead()) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.accent_light));
            holder.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.primary));
        } else {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.card_background));
            holder.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(notif));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";
        if (days < 7) return days + " days ago";

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        NotifViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
        }
    }
}
