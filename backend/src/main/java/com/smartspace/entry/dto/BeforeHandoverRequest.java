package com.smartspace.entry.dto;

import lombok.Data;

import java.util.List;

@Data
public class BeforeHandoverRequest {
    private Long bookingId;
    private String checklist; // JSON string representing the checklist answers
    private String notes;
    private List<String> photos; // list of base64 encoded photos (mock upload)
}
