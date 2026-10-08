package com.smartspace.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev") // Update base path to match application conventions, or keep /api/dev if front-end expects it. Wait, the frontend code isn't using it yet, but the task says `POST /dev/reset-demo`. It's better to stick to `/api/v1/dev` or `/api/dev`. Let's use `/api/v1/dev`.
@Profile("dev") // Only active in dev profile
@RequiredArgsConstructor
public class DevController {

    private final DevDataSeeder dataSeeder;
    private final MutableClock clock;

    @PostMapping("/reset-demo")
    public ResponseEntity<Map<String, String>> resetDemo() {
        dataSeeder.resetAndSeed();
        return ResponseEntity.ok(Map.of(
            "message", "Database reset and seeded for demo.",
            "status", "SUCCESS"
        ));
    }

    @PostMapping("/fast-forward-clock")
    public ResponseEntity<Map<String, String>> fastForwardClock(@RequestParam int minutes) {
        clock.advanceBy(Duration.ofMinutes(minutes));
        return ResponseEntity.ok(Map.of(
            "message", "Clock advanced by " + minutes + " minutes.",
            "status", "SUCCESS"
        ));
    }
}
