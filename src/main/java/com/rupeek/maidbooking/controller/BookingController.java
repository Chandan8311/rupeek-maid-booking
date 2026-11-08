package com.rupeek.maidbooking.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.rupeek.maidbooking.constants.ApiPaths;
import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.dto.booking.BookingResponse;
import com.rupeek.maidbooking.dto.booking.CancelBookingRequest;
import com.rupeek.maidbooking.dto.booking.CancellationResponse;
import com.rupeek.maidbooking.dto.booking.CreateBookingRequest;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.mapper.BookingMapper;
import com.rupeek.maidbooking.model.booking.CancellationScope;
import com.rupeek.maidbooking.service.BookingService;

@RestController
@RequestMapping(ApiPaths.BOOKINGS)
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookings;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = BookingMapper.toResponse(bookings.create(BookingMapper.toCommand(request)));
        return ResponseEntity.created(URI.create(ApiPaths.BOOKINGS + "/" + response.getId()))
                .body(response);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable UUID bookingId) {
        return BookingMapper.toResponse(bookings.get(bookingId));
    }

    @PostMapping("/cancellations")
    public CancellationResponse cancel(@Valid @RequestBody CancelBookingRequest request) {
        if (request.getScope() == CancellationScope.OCCURRENCE) {
            if (request.getOccurrenceId() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "occurrenceId is required for OCCURRENCE cancellation");
            }
            return BookingMapper.toResponse(bookings.cancelOccurrence(request.getBookingId(), request.getOccurrenceId()));
        }
        if (request.getOccurrenceId() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "occurrenceId must not be provided for SERIES cancellation");
        }
        return BookingMapper.toResponse(bookings.cancelSeries(request.getBookingId()));
    }
}
