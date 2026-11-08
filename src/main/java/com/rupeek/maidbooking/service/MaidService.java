package com.rupeek.maidbooking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.rupeek.maidbooking.model.maid.AvailabilityWindow;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;

public interface MaidService {
    Maid onboard(String name, String locality, Gender gender, BigDecimal rating,
                 BigDecimal hourlyRate, Set<String> skills, List<AvailabilityWindow> windows);
    Maid get(UUID maidId);
}
