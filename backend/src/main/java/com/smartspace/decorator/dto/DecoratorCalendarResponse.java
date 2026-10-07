package com.smartspace.decorator.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DecoratorCalendarResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String title;
    private String type; // "ENQUIRY" or "BLACKOUT"
}
