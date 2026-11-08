package com.rupeek.maidbooking.mapper;

import java.util.List;

import com.rupeek.maidbooking.dto.maid.MaidAvailabilityResponse;
import com.rupeek.maidbooking.dto.maid.MaidOnboardRequest;
import com.rupeek.maidbooking.dto.maid.MaidResponse;
import com.rupeek.maidbooking.dto.maid.MaidSearchRequest;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.AvailabilityWindow;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;


public final class MaidMapper {
    private MaidMapper() { }

    public static List<AvailabilityWindow> toAvailability(MaidOnboardRequest request) {
        return request.getAvailability().stream()
                .map(window -> AvailabilityWindow.builder()
                        .dayOfWeek(window.getDayOfWeek())
                        .from(window.getFrom())
                        .to(window.getTo())
                        .build())
                .toList();
    }

    public static MaidSearchCriteria toCriteria(MaidSearchRequest request) {
        TimeSlot slot = TimeSlot.builder()
                .start(request.getStartAt())
                .end(request.getStartAt().plusMinutes(request.getDurationMinutes()))
                .build();
        return MaidSearchCriteria.builder()
                .locality(request.getLocality())
                .serviceType(request.getServiceType())
                .skill(request.getSkill())
                .gender(request.getGender())
                .minRating(request.getMinRating())
                .maxHourlyRate(request.getMaxHourlyRate())
                .slot(slot)
                .build();
    }

    public static MaidResponse toResponse(Maid maid) {
        return MaidResponse.builder()
                .id(maid.getId())
                .name(maid.getName())
                .locality(maid.getLocality())
                .gender(maid.getGender())
                .rating(maid.getRating())
                .hourlyRate(maid.getHourlyRate())
                .skills(maid.getSkills())
                .availability(maid.getAvailability().stream()
                        .map(window -> MaidAvailabilityResponse.builder()
                                .dayOfWeek(window.getDayOfWeek())
                                .from(window.getFrom())
                                .to(window.getTo())
                                .build())
                        .toList())
                .build();
    }
}
