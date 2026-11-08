package com.rupeek.maidbooking.model.maid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Builder;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.booking.TimeSlot;

/** Immutable maid profile; business rules are enforced at construction. */
@Data
public final class Maid {
    private final UUID id;
    private final String name;
    private final String locality;
    private final Gender gender;
    private final BigDecimal rating;
    private final BigDecimal hourlyRate;
    private final Set<String> skills;
    private final List<AvailabilityWindow> availability;

    @Builder
    public Maid(UUID id, String name, String locality, Gender gender, BigDecimal rating,
                BigDecimal hourlyRate, Set<String> skills, List<AvailabilityWindow> availability) {
        if (id == null || name == null || name.isBlank() || locality == null || locality.isBlank()
                || gender == null || rating == null || hourlyRate == null || skills == null || skills.isEmpty()
                || availability == null || availability.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Maid profile is incomplete");
        }
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0
                || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Rating must be 0..5 and rate must be positive");
        }
        this.id = id;
        this.name = name.trim();
        this.locality = locality.trim();
        this.gender = gender;
        this.rating = rating;
        this.hourlyRate = hourlyRate;
        this.skills = skills.stream().map(s -> s == null ? "" : s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        if (this.skills.contains("")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Skills must not be blank");
        }
        this.availability = List.copyOf(availability);
        for (int i = 0; i < this.availability.size(); i++) {
            for (int j = i + 1; j < this.availability.size(); j++) {
                AvailabilityWindow a = this.availability.get(i);
                AvailabilityWindow b = this.availability.get(j);
                if (a.getDayOfWeek() == b.getDayOfWeek()
                        && a.getFrom().isBefore(b.getTo()) && b.getFrom().isBefore(a.getTo())) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST, "Overlapping availability windows");
                }
            }
        }
    }

    public boolean offers(String skill) {
        return skill != null && skills.contains(skill.trim().toLowerCase(Locale.ROOT));
    }

    public boolean worksDuring(TimeSlot slot) {
        return availability.stream().anyMatch(window -> window.covers(slot));
    }
}
