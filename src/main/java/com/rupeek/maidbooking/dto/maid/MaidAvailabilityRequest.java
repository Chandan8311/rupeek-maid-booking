package com.rupeek.maidbooking.dto.maid;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaidAvailabilityRequest {
    @NotNull
    private DayOfWeek dayOfWeek;
    @NotNull
    private LocalTime from;
    @NotNull
    private LocalTime to;
}
