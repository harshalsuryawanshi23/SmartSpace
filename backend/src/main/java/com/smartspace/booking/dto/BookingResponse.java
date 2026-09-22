package com.smartspace.booking.dto;

import com.smartspace.booking.entity.BookingEventType;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.entity.CancellationPolicy;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class BookingResponse {
    private String publicId;
    private String bookingRef;
    private Long hallId;
    private Long renterUserId;
    private BookingEventType eventType;
    private String eventTitle;
    private List<String> themeTags;
    private Integer guestCount;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
    private Instant lockExpiresAt;
    private BigDecimal priceTotal;
    private String currency;
    private CancellationPolicy cancellationPolicy;
    private Instant createdAt;
}
