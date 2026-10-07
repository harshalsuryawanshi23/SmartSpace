package com.smartspace.booking.dto;

import com.smartspace.booking.entity.BookingCohost;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CohostResponse {
    private String displayName;
    private BigDecimal shareAmount;
    private String status;
    private String payToken;

    public static CohostResponse fromEntity(BookingCohost entity) {
        CohostResponse response = new CohostResponse();
        response.setDisplayName(entity.getDisplayName());
        response.setShareAmount(entity.getShareAmount());
        response.setStatus(entity.getStatus().name());
        response.setPayToken(entity.getPayToken());
        return response;
    }
}
