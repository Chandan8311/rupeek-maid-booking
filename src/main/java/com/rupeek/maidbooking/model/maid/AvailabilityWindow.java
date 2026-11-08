package com.rupeek.maidbooking.model.maid;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

import lombok.Data;
import lombok.Builder;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.TimeSlot;

@Data
public final class AvailabilityWindow {
    private final DayOfWeek dayOfWeek;
    private final LocalTime from;
    private final LocalTime to;

    @Builder
    public AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime from, LocalTime to) {
        if (dayOfWeek == null || from == null || to == null || !to.isAfter(from)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Availability requires a weekday and valid start/end times");
        }
        this.dayOfWeek = dayOfWeek;
        this.from = from;
        this.to = to;
    }

    public boolean covers(TimeSlot slot) {
        return slot.getStart().getDayOfWeek() == dayOfWeek
                && !slot.getStart().toLocalTime().isBefore(from)
                && !slot.getEnd().toLocalTime().isAfter(to);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AvailabilityWindow other)) return false;
        return dayOfWeek.equals(other.dayOfWeek) && from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, from, to);
    }
}
