package com.rupeek.maidbooking.model.maid;

import java.math.BigDecimal;

import lombok.Data;
import lombok.Builder;

import com.rupeek.maidbooking.model.booking.TimeSlot;

@Data
public final class MaidSearchCriteria {
    private final String locality;
    private final String serviceType;
    private final String skill;
    private final Gender gender;
    private final BigDecimal minRating;
    private final BigDecimal maxHourlyRate;
    private final TimeSlot slot;

    @Builder
    public MaidSearchCriteria(
            String locality,
            String serviceType,
            String skill,
            Gender gender,
            BigDecimal minRating,
            BigDecimal maxHourlyRate,
            TimeSlot slot
    ) {
        this.locality = locality;
        this.serviceType = serviceType;
        this.skill = skill;
        this.gender = gender;
        this.minRating = minRating;
        this.maxHourlyRate = maxHourlyRate;
        this.slot = slot;
    }

}
