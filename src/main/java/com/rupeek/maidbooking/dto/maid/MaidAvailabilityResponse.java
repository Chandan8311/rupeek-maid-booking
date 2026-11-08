package com.rupeek.maidbooking.dto.maid;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaidAvailabilityResponse {
    private DayOfWeek dayOfWeek;
    private LocalTime from;
    private LocalTime to;
}
