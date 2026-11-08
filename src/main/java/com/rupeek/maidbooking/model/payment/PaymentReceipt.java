package com.rupeek.maidbooking.model.payment;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
public final class PaymentReceipt {
    private final UUID providerPaymentId;
    private final PaymentMethod method;
    private final BigDecimal amount;
    private final BigDecimal refundedAmount;
    private final String idempotencyKey;

    @Builder
    public PaymentReceipt(
            UUID providerPaymentId,
            PaymentMethod method,
            BigDecimal amount,
            BigDecimal refundedAmount,
            String idempotencyKey
    ) {
        this.providerPaymentId = providerPaymentId;
        this.method = method;
        this.amount = amount;
        this.refundedAmount = refundedAmount;
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentReceipt withRefund(BigDecimal refund) {
        return PaymentReceipt.builder()
                .providerPaymentId(providerPaymentId)
                .method(method)
                .amount(amount)
                .refundedAmount(refundedAmount.add(refund))
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
