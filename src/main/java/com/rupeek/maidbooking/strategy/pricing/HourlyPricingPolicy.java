package com.rupeek.maidbooking.strategy.pricing;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.Maid;
@Component
public final class HourlyPricingPolicy implements PricingPolicy {
    public BigDecimal price(Maid maid, TimeSlot slot) {
        long minutes = Duration.between(slot.getStart(), slot.getEnd()).toMinutes();
        return maid.getHourlyRate().multiply(BigDecimal.valueOf(minutes))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
