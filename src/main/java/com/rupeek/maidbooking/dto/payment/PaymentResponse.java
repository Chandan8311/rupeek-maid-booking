package com.rupeek.maidbooking.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.payment.PaymentMethod;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentResponse {
    private UUID providerPaymentId;
    private PaymentMethod method;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String idempotencyKey;
}
