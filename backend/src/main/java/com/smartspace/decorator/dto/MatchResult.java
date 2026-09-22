package com.smartspace.decorator.dto;

import com.smartspace.decorator.entity.DecoratorPackage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MatchResult {
    private Long decoratorId;
    private String businessName;
    private DecoratorPackage bestPackage;
    private BigDecimal matchScore;
    private List<String> positiveReasons;
    private List<String> warnings;
    private Integer alternativePackagesCount;
    
    // sorting details
    private BigDecimal proximityKm;
    private BigDecimal trustScore;
}
