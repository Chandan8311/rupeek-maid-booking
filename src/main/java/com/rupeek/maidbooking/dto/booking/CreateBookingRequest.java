package com.rupeek.maidbooking.dto.booking;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.booking.BookingType;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateBookingRequest {
    @NotNull
    private UUID maidId;
    @NotBlank
    private String customerId;
    @NotBlank
    private String serviceType;
    @NotNull
    private BookingType type;
    private LocalDateTime startAt;
    @NotNull @Min(15) @Max(480)
    private Integer durationMinutes;
    private Set<DayOfWeek> repeatOn;
    private LocalDate repeatUntil;
}
