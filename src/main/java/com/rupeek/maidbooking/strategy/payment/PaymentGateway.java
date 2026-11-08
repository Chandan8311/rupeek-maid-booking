package com.rupeek.maidbooking.strategy.payment;
import java.math.BigDecimal;
import java.util.UUID;

import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentResult;
public interface PaymentGateway {
    PaymentMethod supportedMethod();
    PaymentResult charge(UUID occurrenceId, BigDecimal amount);
    void refund(UUID providerReference, BigDecimal amount);
}
