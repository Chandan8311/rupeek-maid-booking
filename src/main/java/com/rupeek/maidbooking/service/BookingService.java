package com.rupeek.maidbooking.service;

import java.util.UUID;

import com.rupeek.maidbooking.model.booking.BookingDetails;
import com.rupeek.maidbooking.model.booking.CancellationResult;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentReceipt;

public interface BookingService {
    BookingDetails create(CreateBookingCommand command);
    BookingDetails get(UUID bookingId);
    PaymentReceipt pay(UUID bookingId, UUID occurrenceId, PaymentMethod method, String idempotencyKey);
    CancellationResult cancelOccurrence(UUID bookingId, UUID occurrenceId);
    CancellationResult cancelSeries(UUID bookingId);
}
