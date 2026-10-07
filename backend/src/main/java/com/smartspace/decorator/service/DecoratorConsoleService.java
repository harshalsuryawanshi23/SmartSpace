package com.smartspace.decorator.service;

import com.smartspace.decorator.dto.DecoratorCalendarResponse;
import com.smartspace.decorator.dto.DecoratorEnquiryResponse;
import com.smartspace.decorator.dto.DecoratorPackageResponse;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.repository.DecoratorBlackoutRepository;
import com.smartspace.decorator.repository.DecoratorEnquiryRepository;
import com.smartspace.decorator.repository.DecoratorPackageRepository;
import com.smartspace.decorator.repository.DecoratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecoratorConsoleService {

    private final DecoratorRepository decoratorRepository;
    private final DecoratorEnquiryRepository enquiryRepository;
    private final DecoratorPackageRepository packageRepository;
    private final DecoratorBlackoutRepository blackoutRepository;

    @Transactional(readOnly = true)
    public List<DecoratorEnquiryResponse> getEnquiries(Long userId) {
        Decorator decorator = decoratorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Decorator profile not found"));

        return enquiryRepository.findByDecoratorIdOrderByCreatedAtDesc(decorator.getId()).stream()
                .map(e -> DecoratorEnquiryResponse.builder()
                        .id(e.getId())
                        .bookingRef(e.getBooking().getBookingRef())
                        .message(e.getMessage())
                        .status(e.getStatus())
                        .packageName(e.getDecoratorPackage().getName())
                        .quotedPrice(e.getQuotedPrice())
                        .note(e.getNote())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DecoratorPackageResponse> getPackages(Long userId) {
        Decorator decorator = decoratorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Decorator profile not found"));

        return packageRepository.findByDecoratorIdAndActiveTrue(decorator.getId()).stream()
                .map(p -> DecoratorPackageResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .basePrice(p.getBasePrice())
                        .active(p.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DecoratorCalendarResponse> getCalendar(Long userId) {
        Decorator decorator = decoratorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Decorator profile not found"));

        List<DecoratorCalendarResponse> calendar = new ArrayList<>();

        // Add blackouts
        blackoutRepository.findByDecoratorId(decorator.getId()).forEach(b -> 
            calendar.add(DecoratorCalendarResponse.builder()
                    .id(b.getId())
                    .start(b.getStartTime())
                    .end(b.getEndTime())
                    .title("Blackout: " + b.getReason())
                    .type("BLACKOUT")
                    .build())
        );

        // Add accepted/confirmed enquiries
        enquiryRepository.findByDecoratorIdOrderByCreatedAtDesc(decorator.getId()).forEach(e -> {
            if (e.getStatus() == com.smartspace.decorator.entity.DecoratorEnquiry.EnquiryStatus.ACCEPTED ||
                e.getStatus() == com.smartspace.decorator.entity.DecoratorEnquiry.EnquiryStatus.CONFIRMED_BY_RENTER) {
                
                calendar.add(DecoratorCalendarResponse.builder()
                        .id(e.getId())
                        .start(e.getBooking().getStartAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .end(e.getBooking().getEndAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .title("Booking: " + e.getBooking().getBookingRef() + " (" + e.getDecoratorPackage().getName() + ")")
                        .type("ENQUIRY")
                        .build());
            }
        });

        return calendar;
    }
}
