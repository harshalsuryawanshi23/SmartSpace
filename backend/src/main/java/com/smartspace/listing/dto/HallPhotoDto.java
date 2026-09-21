package com.smartspace.listing.dto;

import lombok.Data;

@Data
public class HallPhotoDto {
    private String id;
    private String url;
    private String caption;
    private Integer sortOrder;
}
