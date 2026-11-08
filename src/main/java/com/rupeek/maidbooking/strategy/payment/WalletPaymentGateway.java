package com.rupeek.maidbooking.strategy.payment;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.payment.PaymentMethod;

@Component
public final class WalletPaymentGateway extends AbstractMockPaymentGateway {
    public WalletPaymentGateway() { super(PaymentMethod.WALLET); }
}
