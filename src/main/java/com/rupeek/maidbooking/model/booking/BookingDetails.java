package com.rupeek.maidbooking.model.booking;

import java.util.List;

import lombok.Data;
import lombok.Builder;

@Data
public final class BookingDetails {
    private final Booking booking;
    private final List<BookingOccurrence> occurrences;

    @Builder
    public BookingDetails(Booking booking, List<BookingOccurrence> occurrences) {
        occurrences = List.copyOf(occurrences);
        this.booking = booking;
        this.occurrences = occurrences;
    }

}
