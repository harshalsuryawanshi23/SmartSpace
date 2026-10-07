package com.smartspace.notification.dto;

import lombok.Data;

@Data
public class PushSubscriptionRequest {
    private String endpoint;
    private Keys keys;
    private String userAgent;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
