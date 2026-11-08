package com.rupeek.maidbooking.strategy.payment;

import java.math.BigDecimal;
import java.util.UUID;

import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentResult;


public abstract class AbstractMockPaymentGateway implements PaymentGateway {
    private final PaymentMethod supportedMethod;

    protected AbstractMockPaymentGateway(PaymentMethod method) {
        this.supportedMethod = method;
    }

    @Override
    public PaymentMethod supportedMethod() { return supportedMethod; }

    @Override
    public PaymentResult charge(UUID occurrenceId, BigDecimal amount) {
        return PaymentResult.builder().providerReference(UUID.randomUUID()).build();
    }

    @Override
    public void refund(UUID providerReference, BigDecimal amount) {
        // Mocked successful refund. No actual payment gateway call.
    }
}
