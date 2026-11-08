package com.rupeek.maidbooking.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingOccurrence;
import com.rupeek.maidbooking.repository.BookingRepository;


@Repository
public final class InMemoryBookingRepository implements BookingRepository {
    private final Map<UUID, Booking> bookings = new ConcurrentHashMap<>();
    private final Map<UUID, BookingOccurrence> occurrences = new ConcurrentHashMap<>();
    private final Map<UUID, CopyOnWriteArrayList<BookingOccurrence>> maidOccurrences =
            new ConcurrentHashMap<>();

    @Override
    public void save(Booking booking, List<BookingOccurrence> items) {
        for (BookingOccurrence item : items) {
            occurrences.put(item.getId(), item);
        }
        maidOccurrences.computeIfAbsent(booking.getMaidId(), key -> new CopyOnWriteArrayList<>())
                .addAll(items);
        // Make the booking visible only after all its occurrences have been saved.
        bookings.put(booking.getId(), booking);
    }

    @Override
    public Optional<Booking> findBooking(UUID bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    @Override
    public Optional<BookingOccurrence> findOccurrence(UUID occurrenceId) {
        return Optional.ofNullable(occurrences.get(occurrenceId));
    }

    @Override
    public List<BookingOccurrence> findOccurrencesForMaid(UUID maidId) {
        CopyOnWriteArrayList<BookingOccurrence> found = maidOccurrences.get(maidId);
        return found == null ? List.of() : List.copyOf(found);
    }
}
