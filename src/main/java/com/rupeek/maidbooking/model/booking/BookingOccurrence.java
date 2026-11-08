package com.rupeek.maidbooking.model.booking;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;
import lombok.Setter;
import lombok.AccessLevel;

import com.rupeek.maidbooking.model.payment.PaymentReceipt;


@Data
public final class BookingOccurrence {
    private final UUID id;
    private final UUID bookingId;
    private final UUID maidId;
    private final TimeSlot slot;
    private final BigDecimal price;
    @Setter(AccessLevel.NONE) // Status changes only through markCancelled().
    private volatile OccurrenceStatus status = OccurrenceStatus.RESERVED;
    private volatile PaymentReceipt payment;

    @Builder
    public BookingOccurrence(UUID id, UUID bookingId, UUID maidId, TimeSlot slot, BigDecimal price) {
        this.id = id;
        this.bookingId = bookingId;
        this.maidId = maidId;
        this.slot = slot;
        this.price = price;
    }

    public void markCancelled() {
        status = OccurrenceStatus.CANCELLED;
    }

}
