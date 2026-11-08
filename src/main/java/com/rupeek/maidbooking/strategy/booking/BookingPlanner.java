package com.rupeek.maidbooking.strategy.booking;
import java.time.Clock;
import java.util.List;

import com.rupeek.maidbooking.model.booking.BookingType;
import com.rupeek.maidbooking.model.booking.CreateBookingCommand;
import com.rupeek.maidbooking.model.booking.TimeSlot;
public interface BookingPlanner {
    BookingType supports();
    List<TimeSlot> plan(CreateBookingCommand command, Clock clock);
}
