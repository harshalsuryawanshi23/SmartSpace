package com.smartspace.listing.service;

import com.smartspace.listing.dto.HallOpeningHoursDto;
import com.smartspace.listing.dto.HallPriceRuleDto;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallOpeningHours;
import com.smartspace.listing.entity.HallPriceRule;
import com.smartspace.listing.repository.HallOpeningHoursRepository;
import com.smartspace.listing.repository.HallPriceRuleRepository;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallSettingsService {

    private final HallOpeningHoursRepository openingHoursRepository;
    private final HallPriceRuleRepository priceRuleRepository;
    private final HallRepository hallRepository;

    @Transactional
    public void replaceOpeningHours(String hallPublicId, Long ownerUserId, List<HallOpeningHoursDto> dtos) {
        Hall hall = hallRepository.findByPublicId(hallPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized");
        }
        
        openingHoursRepository.deleteByHallId(hall.getId());
        
        List<HallOpeningHours> hoursList = dtos.stream().map(dto -> {
            return HallOpeningHours.builder()
                    .hallId(hall.getId())
                    .dayOfWeek(dto.getDayOfWeek())
                    .openTime(dto.getOpenTime())
                    .closeTime(dto.getCloseTime())
                    .build();
        }).collect(Collectors.toList());
        
        openingHoursRepository.saveAll(hoursList);
    }

    @Transactional
    public void replacePriceRules(String hallPublicId, Long ownerUserId, List<HallPriceRuleDto> dtos) {
        Hall hall = hallRepository.findByPublicId(hallPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized");
        }
        
        priceRuleRepository.deleteByHallId(hall.getId());
        
        List<HallPriceRule> rulesList = dtos.stream().map(dto -> {
            return HallPriceRule.builder()
                    .hallId(hall.getId())
                    .label(dto.getLabel())
                    .daysMask(dto.getDaysMask())
                    .fromTime(dto.getFromTime())
                    .toTime(dto.getToTime())
                    .pricePerHour(dto.getPricePerHour())
                    .priority(dto.getPriority())
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();
        }).collect(Collectors.toList());
        
        priceRuleRepository.saveAll(rulesList);
    }
}
