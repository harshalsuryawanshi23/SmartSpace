package com.smartspace.listing.dto;

import com.smartspace.listing.entity.StaffRole;
import lombok.Data;

@Data
public class HallStaffRequest {
    private String fullName;
    private String email;
    private String phone;
    private String tempPassword;
    private StaffRole role;
}
