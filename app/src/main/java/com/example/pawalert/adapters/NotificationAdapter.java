package com.example.pawalert.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawalert.R;
import com.example.pawalert.models.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvMessage, tvTime;
        private final View unreadIndicator;
        private final View rootLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            rootLayout = itemView.findViewById(R.id.ll_notification_root);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            
            // Format timestamp
            tvTime.setText(getFormattedTime(notification.getTimestamp()));

            // Handle Read/Unread state
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                tvTitle.setTypeface(null, Typeface.NORMAL);
                tvTitle.setTextColor(0xFF99AABB); // Dimmer color for read
                rootLayout.setAlpha(0.7f);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvTitle.setTextColor(0xFFFFFFFF);
                rootLayout.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNotificationClick(notification);
            });
        }

        private String getFormattedTime(long timestamp) {
            try {
                // Use relative time if recent (e.g., "2 hours ago"), otherwise formatted date
                long now = System.currentTimeMillis();
                if (now - timestamp < DateUtils.WEEK_IN_MILLIS) {
                    return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS).toString();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    return sdf.format(new Date(timestamp));
                }
            } catch (Exception e) {
                return String.valueOf(timestamp);
            }
        }
    }
}
