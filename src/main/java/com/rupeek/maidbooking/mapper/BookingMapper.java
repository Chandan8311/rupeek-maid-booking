package com.rupeek.maidbooking.mapper;

import com.rupeek.maidbooking.dto.booking.BookingResponse;
import com.rupeek.maidbooking.dto.booking.CancellationResponse;
import com.rupeek.maidbooking.dto.booking.CreateBookingRequest;
import com.rupeek.maidbooking.dto.booking.OccurrenceResponse;
import com.rupeek.maidbooking.dto.payment.PaymentResponse;
import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingDetails;
import com.rupeek.maidbooking.model.booking.BookingOccurrence;
import com.rupeek.maidbooking.model.booking.CancellationResult;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.payment.PaymentReceipt;


public final class BookingMapper {
    private BookingMapper() { }

    public static CreateBookingCommand toCommand(CreateBookingRequest request) {
        return CreateBookingCommand.builder()
                .maidId(request.getMaidId())
                .customerId(request.getCustomerId())
                .serviceType(request.getServiceType())
                .type(request.getType())
                .startAt(request.getStartAt())
                .durationMinutes(request.getDurationMinutes())
                .repeatOn(request.getRepeatOn())
                .repeatUntil(request.getRepeatUntil())
                .build();
    }

    public static BookingResponse toResponse(BookingDetails details) {
        Booking booking = details.getBooking();
        return BookingResponse.builder()
                .id(booking.getId())
                .maidId(booking.getMaidId())
                .customerId(booking.getCustomerId())
                .serviceType(booking.getServiceType())
                .type(booking.getType())
                .createdAt(booking.getCreatedAt())
                .occurrences(details.getOccurrences().stream()
                        .map(BookingMapper::toOccurrenceResponse).toList())
                .build();
    }

    private static OccurrenceResponse toOccurrenceResponse(BookingOccurrence occurrence) {
        return OccurrenceResponse.builder()
                .id(occurrence.getId())
                .startAt(occurrence.getSlot().getStart())
                .endAt(occurrence.getSlot().getEnd())
                .price(occurrence.getPrice())
                .status(occurrence.getStatus())
                .payment(toResponse(occurrence.getPayment()))
                .build();
    }

    public static PaymentResponse toResponse(PaymentReceipt receipt) {
        if (receipt == null) {
            return null;
        }
        return PaymentResponse.builder()
                .providerPaymentId(receipt.getProviderPaymentId())
                .method(receipt.getMethod())
                .amount(receipt.getAmount())
                .refundedAmount(receipt.getRefundedAmount())
                .idempotencyKey(receipt.getIdempotencyKey())
                .build();
    }

    public static CancellationResponse toResponse(CancellationResult result) {
        return CancellationResponse.builder()
                .bookingId(result.getBookingId())
                .cancelledOccurrenceIds(result.getCancelledOccurrenceIds())
                .totalRefund(result.getTotalRefund())
                .build();
    }
}
