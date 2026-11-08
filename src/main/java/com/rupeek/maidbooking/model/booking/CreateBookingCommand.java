package com.rupeek.maidbooking.model.booking;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
public final class CreateBookingCommand {
    private final UUID maidId;
    private final String customerId;
    private final String serviceType;
    private final BookingType type;
    private final LocalDateTime startAt;
    private final int durationMinutes;
    private final Set<DayOfWeek> repeatOn;
    private final LocalDate repeatUntil;

    @Builder
    public CreateBookingCommand(
            UUID maidId,
            String customerId,
            String serviceType,
            BookingType type,
            LocalDateTime startAt,
            int durationMinutes,
            Set<DayOfWeek> repeatOn,
            LocalDate repeatUntil
    ) {
        repeatOn = repeatOn == null ? null : Set.copyOf(repeatOn);
        this.maidId = maidId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.type = type;
        this.startAt = startAt;
        this.durationMinutes = durationMinutes;
        this.repeatOn = repeatOn;
        this.repeatUntil = repeatUntil;
    }

}
