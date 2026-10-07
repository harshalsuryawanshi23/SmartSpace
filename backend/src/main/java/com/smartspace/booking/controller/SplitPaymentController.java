package com.smartspace.booking.controller;

import com.smartspace.booking.dto.AddCohostRequest;
import com.smartspace.booking.dto.CohostResponse;
import com.smartspace.booking.service.SplitPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings/{bookingId}/cohosts")
@RequiredArgsConstructor
public class SplitPaymentController {

    private final SplitPaymentService splitPaymentService;

    @PostMapping
    public ResponseEntity<CohostResponse> addCohost(
            @PathVariable String bookingId,
            @Valid @RequestBody AddCohostRequest request) {
        return new ResponseEntity<>(splitPaymentService.addCohost(bookingId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CohostResponse>> getCohosts(@PathVariable String bookingId) {
        return ResponseEntity.ok(splitPaymentService.getCohosts(bookingId));
    }

    @PostMapping("/mock-pay/{payToken}")
    public ResponseEntity<Void> mockPay(@PathVariable String bookingId, @PathVariable String payToken) {
        splitPaymentService.mockPayCohost(payToken);
        return ResponseEntity.ok().build();
    }
}
