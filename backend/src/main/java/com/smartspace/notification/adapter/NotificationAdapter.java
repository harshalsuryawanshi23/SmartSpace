package com.smartspace.notification.adapter;

import com.smartspace.notification.entity.NotificationOutbox;

public interface NotificationAdapter {
    void send(NotificationOutbox outbox);
    boolean supports(com.smartspace.notification.entity.NotificationChannel channel);
}
