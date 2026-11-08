package com.rupeek.maidbooking.dto.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.booking.BookingType;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BookingResponse {
    private UUID id;
    private UUID maidId;
    private String customerId;
    private String serviceType;
    private BookingType type;
    private LocalDateTime createdAt;
    private List<OccurrenceResponse> occurrences;
}
