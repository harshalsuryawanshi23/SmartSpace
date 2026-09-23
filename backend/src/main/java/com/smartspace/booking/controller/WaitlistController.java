package com.smartspace.booking.controller;

import com.smartspace.booking.entity.WaitlistEntry;
import com.smartspace.booking.repository.WaitlistEntryRepository;
import com.smartspace.auth.entity.User;
import com.smartspace.auth.repository.UserRepository;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/halls/{hallId}/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final HallRepository hallRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WaitlistEntry> joinWaitlist(
            @PathVariable Long hallId,
            @RequestBody JoinWaitlistRequest request) {
        
        Long userId = 3L; // TODO: SecurityContext

        Hall hall = hallRepository.findById(hallId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        WaitlistEntry entry = new WaitlistEntry();
        entry.setHall(hall);
        entry.setUser(user);
        entry.setStartTime(request.getStartAt());
        entry.setDurationMinutes(request.getDurationMinutes());
        entry.setStatus("WAITING");

        waitlistEntryRepository.save(entry);
        return ResponseEntity.ok(entry);
    }
}

class JoinWaitlistRequest {
    private Instant startAt;
    private int durationMinutes;

    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}
