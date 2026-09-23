package com.smartspace.decorator.controller;

import com.smartspace.decorator.dto.DecoratorMatchRequest;
import com.smartspace.decorator.dto.DecoratorMatchResponse;
import com.smartspace.decorator.service.DecoratorMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DecoratorController {

    private final DecoratorMatchService matchService;

    @PostMapping("/decorator-matches")
    public ResponseEntity<List<DecoratorMatchResponse>> getMatches(
            @RequestBody DecoratorMatchRequest request,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(matchService.getMatches(request, limit));
    }
}
