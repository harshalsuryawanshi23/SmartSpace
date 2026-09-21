package com.smartspace.listing.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HallOpeningHoursDto {
    private Integer dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
}
