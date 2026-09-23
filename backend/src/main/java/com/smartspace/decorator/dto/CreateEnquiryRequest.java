package com.smartspace.decorator.dto;

import lombok.Data;

@Data
public class CreateEnquiryRequest {
    private Long bookingId;
    private Long decoratorId;
    private Long packageId;
    private String message;
}
