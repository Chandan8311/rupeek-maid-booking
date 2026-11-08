package com.rupeek.maidbooking.service;

import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.Maid;

public interface AvailabilityService {
    boolean isAvailable(Maid maid, TimeSlot slot);
}
