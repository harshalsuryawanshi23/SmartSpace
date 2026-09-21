package com.smartspace.listing.controller;

import com.smartspace.auth.service.AuthContextService;
import com.smartspace.listing.dto.SocietyDto;
import com.smartspace.listing.service.SocietyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/societies")
@RequiredArgsConstructor
public class SocietyController {

    private final SocietyService societyService;
    private final AuthContextService authContextService;

    @GetMapping
    public ResponseEntity<List<SocietyDto>> searchSocieties(@RequestParam(value = "q", defaultValue = "") String query) {
        return ResponseEntity.ok(societyService.searchSocieties(query));
    }

    @PostMapping("/{id}/join-requests")
    public ResponseEntity<?> requestJoin(@PathVariable("id") String societyPublicId, @RequestBody Map<String, String> body) {
        Long currentUserId = authContextService.getCurrentUserId();
        String flatLabel = body.get("flatLabel");
        societyService.requestJoin(societyPublicId, currentUserId, flatLabel);
        return ResponseEntity.ok().build();
    }
}
