package com.rupeek.maidbooking.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.rupeek.maidbooking.constants.ApiPaths;
import com.rupeek.maidbooking.dto.payment.PaymentRequest;
import com.rupeek.maidbooking.dto.payment.PaymentResponse;
import com.rupeek.maidbooking.mapper.BookingMapper;
import com.rupeek.maidbooking.service.BookingService;

@RestController
@RequestMapping(ApiPaths.PAYMENTS)
@RequiredArgsConstructor
public class PaymentController {
    private final BookingService bookings;

    @PostMapping
    public PaymentResponse pay(@Valid @RequestBody PaymentRequest request) {
        return BookingMapper.toResponse(bookings.pay(request.getBookingId(), request.getOccurrenceId(),
                request.getMethod(), request.getIdempotencyKey()));
    }
}
