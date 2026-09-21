package com.smartspace.listing.dto;

import com.smartspace.listing.entity.StaffRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HallStaffDto {
    private String userId;
    private StaffRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
