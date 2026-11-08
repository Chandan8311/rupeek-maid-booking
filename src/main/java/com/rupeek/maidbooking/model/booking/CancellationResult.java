package com.rupeek.maidbooking.model.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
public final class CancellationResult {
    private final UUID bookingId;
    private final List<UUID> cancelledOccurrenceIds;
    private final BigDecimal totalRefund;

    @Builder
    public CancellationResult(UUID bookingId, List<UUID> cancelledOccurrenceIds, BigDecimal totalRefund) {
        cancelledOccurrenceIds = List.copyOf(cancelledOccurrenceIds);
        this.bookingId = bookingId;
        this.cancelledOccurrenceIds = cancelledOccurrenceIds;
        this.totalRefund = totalRefund;
    }

}
