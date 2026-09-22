package com.smartspace.entry.controller;

import com.smartspace.entry.dto.OfflineSyncRequest;
import com.smartspace.entry.dto.OfflineSyncResponse;
import com.smartspace.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.smartspace.security.util.SecurityUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/entry")
@RequiredArgsConstructor
public class OfflineSyncController {

    private final EntryService entryService;

    @GetMapping("/manifest")
    @PreAuthorize("hasRole('WATCHMAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getManifest() {
        // Retrieve public keys (simplified for demo to single string)
        // Usually, this would come from a configuration or secret manager
        String publicKeyHex = "0000000000000000000000000000000000000000000000000000000000000000"; // Dummy test key for Phase 15 tests, real implementation uses QrTokenService logic
        
        // Retrieve revoked JTIs
        java.util.List<String> revokedJtis = entryService.getRevokedJtis();

        return ResponseEntity.ok(Map.of(
            "publicKey", publicKeyHex,
            "revokedJtis", revokedJtis,
            "issuedAt", java.time.Instant.now().toEpochMilli()
        ));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<OfflineSyncResponse> syncOfflineLogs(@RequestBody OfflineSyncRequest request) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        
        OfflineSyncResponse response = entryService.processOfflineSync(request, watchmanUserId);
        
        return ResponseEntity.ok(response);
    }
}
