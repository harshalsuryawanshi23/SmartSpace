package com.smartspace.decorator.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.decorator.dto.CreateEnquiryRequest;
import com.smartspace.decorator.dto.RespondEnquiryRequest;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.decorator.repository.DecoratorEnquiryRepository;
import com.smartspace.decorator.repository.DecoratorPackageRepository;
import com.smartspace.decorator.repository.DecoratorRepository;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.repository.EntryCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecoratorEnquiryService {

    private final DecoratorEnquiryRepository enquiryRepository;
    private final BookingRepository bookingRepository;
    private final DecoratorRepository decoratorRepository;
    private final DecoratorPackageRepository packageRepository;
    private final EntryCredentialRepository credentialRepository;

    @Transactional
    public DecoratorEnquiry createEnquiry(CreateEnquiryRequest request, Long renterUserId) {
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow();
        if (!booking.getRenter().getId().equals(renterUserId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking must be CONFIRMED or PENDING_PAYMENT");
        }

        Decorator decorator = decoratorRepository.findById(request.getDecoratorId()).orElseThrow();
        DecoratorPackage pkg = packageRepository.findById(request.getPackageId()).orElseThrow();

        DecoratorEnquiry enquiry = DecoratorEnquiry.builder()
                .booking(booking)
                .decorator(decorator)
                .decoratorPackage(pkg)
                .message(request.getMessage())
                .status(DecoratorEnquiry.EnquiryStatus.SENT)
                .build();
                
        return enquiryRepository.save(enquiry);
    }

    @Transactional
    public void respondToEnquiry(Long enquiryId, Long decoratorUserId, RespondEnquiryRequest request) {
        DecoratorEnquiry enquiry = enquiryRepository.findById(enquiryId).orElseThrow();
        if (!enquiry.getDecorator().getUser().getId().equals(decoratorUserId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        if (enquiry.getStatus() != DecoratorEnquiry.EnquiryStatus.SENT) {
            throw new RuntimeException("Enquiry is not in SENT state");
        }

        if (request.isAccept()) {
            enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.ACCEPTED);
            enquiry.setQuotedPrice(request.getQuotedPrice());
        } else {
            enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.DECLINED);
        }
        enquiry.setNote(request.getNote());
        
        enquiryRepository.save(enquiry);
    }

    @Transactional
    public void confirmEnquiry(Long enquiryId, Long renterUserId) {
        DecoratorEnquiry enquiry = enquiryRepository.findById(enquiryId).orElseThrow();
        if (!enquiry.getBooking().getRenter().getId().equals(renterUserId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        if (enquiry.getStatus() != DecoratorEnquiry.EnquiryStatus.ACCEPTED) {
            throw new RuntimeException("Enquiry must be ACCEPTED first");
        }
        
        enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.CONFIRMED_BY_RENTER);
        enquiryRepository.save(enquiry);
        
        Booking booking = enquiry.getBooking();
        DecoratorPackage pkg = enquiry.getDecoratorPackage();
        
        LocalDateTime setupStart = booking.getSlotStart().minusMinutes(pkg.getSetupMinutes());
        LocalDateTime teardownEnd = booking.getSlotEnd().plusMinutes(pkg.getTeardownMinutes());
        
        // TODO: Reserve SETUP cells (skipping complex overlap checks for simplicity, assuming user accepts overlap risk as per v1 spec)
        
        // Generate Entry Credential for Decorator
        EntryCredential credential = EntryCredential.builder()
                .jti(UUID.randomUUID().toString().replace("-", ""))
                .booking(booking)
                .kind("DECORATOR")
                .decoratorEnquiryId(enquiry.getId())
                .validFrom(setupStart.minusMinutes(15))
                .validUntil(teardownEnd)
                .keyId("KEY1")
                .build();
                
        credentialRepository.save(credential);
        
        // In-app notifications could be sent here
    }
}
