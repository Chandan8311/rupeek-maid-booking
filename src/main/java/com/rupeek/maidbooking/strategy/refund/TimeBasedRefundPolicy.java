package com.rupeek.maidbooking.strategy.refund;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.booking.BookingOccurrence;
@Component
public final class TimeBasedRefundPolicy implements RefundPolicy {
    public BigDecimal refundableAmount(BookingOccurrence occurrence, Clock clock) {
        if (occurrence.getPayment() == null) return BigDecimal.ZERO.setScale(2);
        Duration remaining = Duration.between(LocalDateTime.now(clock), occurrence.getSlot().getStart());
        BigDecimal fraction = remaining.compareTo(Duration.ofHours(24)) >= 0 ? BigDecimal.ONE
                : remaining.compareTo(Duration.ofHours(1)) >= 0 ? new BigDecimal("0.50") : BigDecimal.ZERO;
        return occurrence.getPayment().getAmount().multiply(fraction).setScale(2, RoundingMode.HALF_UP);
    }
}
