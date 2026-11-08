package com.rupeek.maidbooking.model.payment;

import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
public final class PaymentResult {
    private final UUID providerReference;

    @Builder
    public PaymentResult(UUID providerReference) {
        this.providerReference = providerReference;
    }

}
