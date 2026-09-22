package com.smartspace.booking.dto;

import com.smartspace.booking.entity.BookingEventType;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class BookingCreateRequest {
    private String idempotencyKey;
    private Long hallId;
    private BookingEventType eventType;
    private String eventTitle;
    private List<String> themeTags;
    private Integer guestCount;
    private Instant startAt;
    private Instant endAt;
}
