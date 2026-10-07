package com.smartspace.user.controller;

import com.smartspace.user.service.PrivacyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/privacy")
public class PrivacyController {

    private final PrivacyService privacyService;

    @Autowired
    public PrivacyController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportData(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String email = principal.getName();
        Map<String, Object> data = privacyService.exportUserData(email);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> requestAccountDeletion(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String email = principal.getName();
        privacyService.anonymizeUser(email);
        return ResponseEntity.ok().build();
    }
}
