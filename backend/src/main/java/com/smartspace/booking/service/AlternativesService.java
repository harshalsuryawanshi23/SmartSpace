package com.smartspace.booking.service;

import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlternativesService {

    private final HallRepository hallRepository;
    private final SlotService slotService;

    /**
     * Calculates alternatives when a requested slot is unavailable.
     * Returns a 409-friendly payload with alternative options.
     */
    public Map<String, Object> computeAlternatives(Long hallId, Instant requestedStart, Instant requestedEnd, int guests) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", "SLOT_UNAVAILABLE");
        payload.put("message", "The requested time slot is no longer available.");
        
        List<Map<String, Object>> alternatives = new ArrayList<>();
        
        Hall hall = hallRepository.findById(hallId).orElse(null);
        if (hall != null) {
            long durationMinutes = ChronoUnit.MINUTES.between(requestedStart, requestedEnd);
            
            // Look for an alternative slot later the same day
            Instant laterStart = requestedStart.plus(2, ChronoUnit.HOURS);
            Instant laterEnd = laterStart.plus(durationMinutes, ChronoUnit.MINUTES);
            
            if (slotService.isSlotAvailable(hallId, laterStart, laterEnd)) {
                alternatives.add(Map.of(
                        "type", "DIFFERENT_TIME",
                        "hallId", hallId,
                        "startAt", laterStart.toString(),
                        "endAt", laterEnd.toString()
                ));
            }
            
            // MVP: Could also query different halls nearby
        }
        
        payload.put("alternatives", alternatives);
        return payload;
    }
}
