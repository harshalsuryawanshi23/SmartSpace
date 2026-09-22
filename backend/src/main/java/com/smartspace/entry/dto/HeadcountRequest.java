package com.smartspace.entry.dto;

import lombok.Data;

@Data
public class HeadcountRequest {
    private Long bookingId;
    private Integer count;
    private String deviceId;
}
