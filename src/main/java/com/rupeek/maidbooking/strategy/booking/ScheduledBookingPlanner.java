package com.rupeek.maidbooking.strategy.booking;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.utils.TimeSlotUtils;
@Component
public final class ScheduledBookingPlanner implements BookingPlanner {
    public BookingType supports() { return BookingType.SCHEDULED; }
    public List<TimeSlot> plan(CreateBookingCommand c, Clock clock) {
        if (c.getStartAt() == null || !c.getStartAt().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Scheduled booking must start in the future");
        }
        return List.of(TimeSlotUtils.of(c.getStartAt(), c.getDurationMinutes()));
    }
}
