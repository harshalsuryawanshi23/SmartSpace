package com.smartspace.decorator.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.ApiError;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.decorator.repository.DecoratorEnquiryRepository;
import com.smartspace.decorator.repository.DecoratorPackageRepository;
import com.smartspace.decorator.repository.DecoratorRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class DecoratorEnquiryService {

    private final DecoratorEnquiryRepository enquiryRepository;
    private final BookingRepository bookingRepository;
    private final DecoratorRepository decoratorRepository;
    private final DecoratorPackageRepository packageRepository;
    private final UserRepository userRepository;

    public DecoratorEnquiryService(DecoratorEnquiryRepository enquiryRepository, BookingRepository bookingRepository, DecoratorRepository decoratorRepository, DecoratorPackageRepository packageRepository, UserRepository userRepository) {
        this.enquiryRepository = enquiryRepository;
        this.bookingRepository = bookingRepository;
        this.decoratorRepository = decoratorRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    public DecoratorEnquiry createEnquiry(Long renterUserId, Long bookingId, Long decoratorId, Long packageId, String message) {
        User renter = userRepository.findById(renterUserId).orElseThrow(() -> new ApiError("User not found"));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ApiError("Booking not found"));
        Decorator decorator = decoratorRepository.findById(decoratorId).orElseThrow(() -> new ApiError("Decorator not found"));
        
        DecoratorPackage pkg = null;
        if (packageId != null) {
            pkg = packageRepository.findById(packageId).orElse(null);
        }

        DecoratorEnquiry enquiry = new DecoratorEnquiry();
        enquiry.setPublicId(UUID.randomUUID().toString());
        enquiry.setRenterUser(renter);
        enquiry.setBooking(booking);
        enquiry.setDecorator(decorator);
        enquiry.setDecoratorPackage(pkg);
        enquiry.setMessage(message);
        enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.SENT);

        return enquiryRepository.save(enquiry);
    }

    public DecoratorEnquiry decoratorRespond(Long decoratorId, String enquiryPublicId, boolean accept, BigDecimal quotedPrice, String message) {
        DecoratorEnquiry enquiry = enquiryRepository.findByPublicId(enquiryPublicId)
                .orElseThrow(() -> new ApiError("Enquiry not found"));

        if (!enquiry.getDecorator().getId().equals(decoratorId)) {
            throw new ApiError("Not authorized to respond to this enquiry");
        }

        if (enquiry.getStatus() != DecoratorEnquiry.EnquiryStatus.SENT) {
            throw new ApiError("Enquiry is not in SENT state");
        }

        enquiry.setRespondedAt(LocalDateTime.now());
        if (accept) {
            enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.ACCEPTED);
            enquiry.setQuotedPrice(quotedPrice);
        } else {
            enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.DECLINED);
        }
        
        // append message if provided
        if (message != null && !message.isEmpty()) {
            enquiry.setMessage(enquiry.getMessage() + "\nDecorator: " + message);
        }

        return enquiryRepository.save(enquiry);
    }

    public DecoratorEnquiry renterConfirm(Long renterUserId, String enquiryPublicId) {
        DecoratorEnquiry enquiry = enquiryRepository.findByPublicId(enquiryPublicId)
                .orElseThrow(() -> new ApiError("Enquiry not found"));

        if (!enquiry.getRenterUser().getId().equals(renterUserId)) {
            throw new ApiError("Not authorized to confirm this enquiry");
        }

        if (enquiry.getStatus() != DecoratorEnquiry.EnquiryStatus.ACCEPTED) {
            throw new ApiError("Enquiry is not in ACCEPTED state");
        }

        enquiry.setStatus(DecoratorEnquiry.EnquiryStatus.CONFIRMED_BY_RENTER);
        return enquiryRepository.save(enquiry);
    }
}
