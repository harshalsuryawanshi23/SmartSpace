package com.smartspace.decorator.job;

import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.repository.DecoratorEnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DecoratorEnquiryExpiryJob {

    private final DecoratorEnquiryRepository enquiryRepository;

    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void expireOldEnquiries() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        List<DecoratorEnquiry> oldEnquiries = enquiryRepository.findByStatusAndCreatedAtBefore(
                DecoratorEnquiry.EnquiryStatus.SENT, threshold);
                
        for (DecoratorEnquiry enquiry : oldEnquiries) {
            enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.EXPIRED);
        }
        
        enquiryRepository.saveAll(oldEnquiries);
    }
}
