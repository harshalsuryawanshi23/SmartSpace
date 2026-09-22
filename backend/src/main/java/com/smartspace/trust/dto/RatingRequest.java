package com.smartspace.trust.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class RatingRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;

    private Map<String, Integer> dimensions;

    private String comment;
}
