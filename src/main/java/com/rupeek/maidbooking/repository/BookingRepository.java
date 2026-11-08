package com.rupeek.maidbooking.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingOccurrence;
public interface BookingRepository {
    void save(Booking booking, List<BookingOccurrence> occurrences);
    Optional<Booking> findBooking(UUID bookingId);
    Optional<BookingOccurrence> findOccurrence(UUID occurrenceId);
    List<BookingOccurrence> findOccurrencesForMaid(UUID maidId);
}
