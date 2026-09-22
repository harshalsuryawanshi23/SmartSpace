package com.smartspace.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile("dev") // Only active in dev profile
@RequiredArgsConstructor
public class DevController {

    // Usually we would inject a DevDataSeeder service and a MutableClock here
    // private final DevDataSeeder dataSeeder;
    // private final MutableClock clock;

    @PostMapping("/reset-demo")
    public ResponseEntity<Map<String, String>> resetDemo() {
        // dataSeeder.resetAndSeed();
        return ResponseEntity.ok(Map.of(
            "message", "Database reset and seeded for demo.",
            "status", "SUCCESS"
        ));
    }

    @PostMapping("/fast-forward-clock")
    public ResponseEntity<Map<String, String>> fastForwardClock(@RequestParam int minutes) {
        // clock.advanceBy(Duration.ofMinutes(minutes));
        return ResponseEntity.ok(Map.of(
            "message", "Clock advanced by " + minutes + " minutes.",
            "status", "SUCCESS"
        ));
    }
}
