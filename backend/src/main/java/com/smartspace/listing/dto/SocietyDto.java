package com.smartspace.listing.dto;

import com.smartspace.listing.entity.VerificationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SocietyDto {
    private String id;
    private String name;
    private String addressLine;
    private String locality;
    private String city;
    private String pincode;
    private BigDecimal lat;
    private BigDecimal lng;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
}
