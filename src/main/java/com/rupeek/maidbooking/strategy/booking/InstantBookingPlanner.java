package com.rupeek.maidbooking.strategy.booking;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.utils.TimeSlotUtils;
@Component
public final class InstantBookingPlanner implements BookingPlanner {
    public BookingType supports() { return BookingType.INSTANT; }
    public List<TimeSlot> plan(CreateBookingCommand c, Clock clock) {
        return List.of(TimeSlotUtils.of(LocalDateTime.now(clock).withNano(0), c.getDurationMinutes()));
    }
}
