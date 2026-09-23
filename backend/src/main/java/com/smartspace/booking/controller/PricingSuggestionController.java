package com.smartspace.booking.controller;

import com.smartspace.booking.entity.PricingSuggestion;
import com.smartspace.booking.repository.PricingSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/owner/halls/{hallId}/pricing-suggestions")
@RequiredArgsConstructor
public class PricingSuggestionController {

    private final PricingSuggestionRepository suggestionRepository;

    @GetMapping
    public ResponseEntity<List<PricingSuggestion>> getSuggestions(@PathVariable Long hallId) {
        // TODO: Validate owner owns this hall
        List<PricingSuggestion> suggestions = suggestionRepository.findByHallIdAndStatus(hallId, "PENDING");
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<Void> respondToSuggestion(
            @PathVariable Long hallId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        // TODO: Validate owner owns this hall
        PricingSuggestion suggestion = suggestionRepository.findById(id).orElseThrow();
        
        String action = body.get("action"); // APPLY, DISMISS
        if ("APPLY".equalsIgnoreCase(action)) {
            suggestion.setStatus("APPLIED");
            // Here we would also create a HallPriceRule for this suggestion
        } else if ("DISMISS".equalsIgnoreCase(action)) {
            suggestion.setStatus("DISMISSED");
        }
        
        suggestionRepository.save(suggestion);
        return ResponseEntity.ok().build();
    }
}
