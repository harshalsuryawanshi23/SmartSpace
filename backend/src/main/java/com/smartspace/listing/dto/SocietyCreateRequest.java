package com.smartspace.listing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SocietyCreateRequest {
    private String name;
    private String addressLine;
    private String locality;
    private String city;
    private String pincode;
    private BigDecimal lat;
    private BigDecimal lng;
}
