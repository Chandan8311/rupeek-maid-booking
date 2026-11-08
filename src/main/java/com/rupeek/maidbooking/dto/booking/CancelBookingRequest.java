package com.rupeek.maidbooking.dto.booking;

import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.booking.CancellationScope;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CancelBookingRequest {
    @NotNull
    private UUID bookingId;
    @NotNull
    private CancellationScope scope;
    private UUID occurrenceId;
}
