package com.rupeek.maidbooking.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.dto.payment.PaymentResponse;
import com.rupeek.maidbooking.model.booking.OccurrenceStatus;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OccurrenceResponse {
    private UUID id;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal price;
    private OccurrenceStatus status;
    private PaymentResponse payment;
}
