package com.rupeek.maidbooking.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.rupeek.maidbooking.model.booking.OccurrenceStatus;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.service.AvailabilityService;

@Service
@RequiredArgsConstructor
public final class AvailabilityServiceImpl implements AvailabilityService {
    private final BookingRepository bookings;

    @Override
    public boolean isAvailable(Maid maid, TimeSlot slot) {
        if (!maid.worksDuring(slot)) {
            return false;
        }
        return bookings.findOccurrencesForMaid(maid.getId()).stream()
                .noneMatch(existing -> existing.getStatus() == OccurrenceStatus.RESERVED
                        && existing.getSlot().overlaps(slot));
    }
}
