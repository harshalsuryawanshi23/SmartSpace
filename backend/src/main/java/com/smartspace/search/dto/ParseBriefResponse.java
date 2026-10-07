package com.smartspace.search.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ParseBriefResponse {
    private String eventType;
    private Integer guests;
    private String date; // ISO Date YYYY-MM-DD
    private String startTime; // HH:mm
    private Integer durationMinutes;
    private List<String> amenities;
}
