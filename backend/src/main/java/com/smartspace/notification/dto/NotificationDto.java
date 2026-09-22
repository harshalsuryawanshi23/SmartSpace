package com.smartspace.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String body;
    private String link;
    private Instant readAt;
    private Instant createdAt;
}
