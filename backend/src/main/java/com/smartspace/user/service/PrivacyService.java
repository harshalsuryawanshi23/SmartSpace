package com.smartspace.user.service;

import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PrivacyService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PrivacyService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportUserData(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        Map<String, Object> export = new HashMap<>();
        export.put("profile", user);
        
        List<Booking> bookings = bookingRepository.findByRenterIdOrderByStartAtDesc(user.getId());
        export.put("bookings", bookings);

        return export;
    }

    @Transactional
    public void anonymizeUser(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();
        
        // Scrub PII
        user.setFullName("Deleted User");
        user.setEmail(UUID.randomUUID().toString() + "@deleted.local");
        user.setPhone(UUID.randomUUID().toString().substring(0, 15));
        user.setPasswordHash("DELETED");
        
        userRepository.save(user);
    }
}
