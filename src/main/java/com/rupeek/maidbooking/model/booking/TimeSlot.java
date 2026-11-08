package com.rupeek.maidbooking.model.booking;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Data;
import lombok.Builder;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;

/** Half-open time interval [start, end). Adjacent bookings do not overlap. */
@Data
public final class TimeSlot {
    private final LocalDateTime start;
    private final LocalDateTime end;

    @Builder
    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Slot end must be after start");
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Cross-midnight slots are not supported");
        }
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimeSlot other)) return false;
        return start.equals(other.start) && end.equals(other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
