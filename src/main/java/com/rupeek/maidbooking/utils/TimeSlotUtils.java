package com.rupeek.maidbooking.utils;

import java.time.LocalDateTime;

import com.rupeek.maidbooking.constants.BookingConstants;
import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.TimeSlot;

public final class TimeSlotUtils {
    private TimeSlotUtils() { }

    public static TimeSlot of(LocalDateTime start, int minutes) {
        if (start == null || minutes < BookingConstants.MIN_DURATION_MINUTES
                || minutes > BookingConstants.MAX_DURATION_MINUTES) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Start time is required and duration must be 15..480 minutes");
        }
        return TimeSlot.builder().start(start).end(start.plusMinutes(minutes)).build();
    }
}
