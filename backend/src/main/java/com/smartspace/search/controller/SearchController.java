package com.smartspace.search.controller;

import com.smartspace.search.dto.ParseBriefRequest;
import com.smartspace.search.dto.ParseBriefResponse;
import com.smartspace.search.service.BriefParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final BriefParserService briefParserService;

    @PostMapping("/parse-brief")
    public ResponseEntity<ParseBriefResponse> parseBrief(@RequestBody ParseBriefRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(briefParserService.parse(request.getText()));
    }
}
