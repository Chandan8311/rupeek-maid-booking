package com.rupeek.maidbooking.model.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
public final class Booking {
    private final UUID id;
    private final UUID maidId;
    private final String customerId;
    private final String serviceType;
    private final BookingType type;
    private final LocalDateTime createdAt;
    private final List<UUID> occurrenceIds;

    @Builder
    public Booking(
            UUID id,
            UUID maidId,
            String customerId,
            String serviceType,
            BookingType type,
            LocalDateTime createdAt,
            List<UUID> occurrenceIds
    ) {
        occurrenceIds = List.copyOf(occurrenceIds);
        this.id = id;
        this.maidId = maidId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.type = type;
        this.createdAt = createdAt;
        this.occurrenceIds = occurrenceIds;
    }

}
