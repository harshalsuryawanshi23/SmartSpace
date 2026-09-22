package com.smartspace.booking.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BookingQuoteRequest {
    private Long hallId;
    private Instant startAt;
    private Instant endAt;
    private Integer guestCount;
}
