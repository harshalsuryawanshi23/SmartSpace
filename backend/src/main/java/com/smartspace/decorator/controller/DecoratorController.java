package com.smartspace.decorator.controller;

import com.smartspace.common.exception.ApiError;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.decorator.service.DecoratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decorators")
public class DecoratorController {

    private final DecoratorService decoratorService;

    public DecoratorController(DecoratorService decoratorService) {
        this.decoratorService = decoratorService;
    }

    @PostMapping("/profile")
    public ResponseEntity<Decorator> createProfile(@RequestHeader("X-User-Id") Long userId, @RequestBody Decorator request) {
        return ResponseEntity.ok(decoratorService.createProfile(userId, request));
    }

    @GetMapping("/profile")
    public ResponseEntity<Decorator> getProfile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(decoratorService.getProfileByUserId(userId));
    }

    @PostMapping("/{decoratorId}/packages")
    public ResponseEntity<DecoratorPackage> addPackage(
            @PathVariable Long decoratorId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody DecoratorPackage request) {
        
        Decorator decorator = decoratorService.getProfileByUserId(userId);
        if (!decorator.getId().equals(decoratorId)) {
            throw new ApiError("Not authorized to modify this decorator");
        }

        return ResponseEntity.ok(decoratorService.addPackage(decoratorId, request));
    }

    @GetMapping("/{decoratorId}/packages")
    public ResponseEntity<List<DecoratorPackage>> getPackages(@PathVariable Long decoratorId) {
        return ResponseEntity.ok(decoratorService.getPackages(decoratorId));
    }
}
