package com.smartspace.notification.controller;

import com.smartspace.notification.dto.PushSubscriptionRequest;
import com.smartspace.notification.entity.PushSubscription;
import com.smartspace.notification.repository.PushSubscriptionRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Slf4j
public class PushSubscriptionController {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;

    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<Void> subscribe(
            @RequestHeader("X-User-Id") Long userId, // MVP authentication
            @RequestBody PushSubscriptionRequest request) {
            
        User user = userRepository.findById(userId).orElseThrow();
        
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(request.getEndpoint());
        if (existing.isPresent()) {
            return ResponseEntity.ok().build(); // Already subscribed
        }

        PushSubscription sub = PushSubscription.builder()
                .user(user)
                .endpoint(request.getEndpoint())
                .p256dh(request.getKeys().getP256dh())
                .auth(request.getKeys().getAuth())
                .userAgent(request.getUserAgent())
                .build();
                
        pushSubscriptionRepository.save(sub);
        log.info("Saved Web Push subscription for user {}", userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionRequest request) {
        pushSubscriptionRepository.findByEndpoint(request.getEndpoint())
                .ifPresent(pushSubscriptionRepository::delete);
        return ResponseEntity.ok().build();
    }
}
