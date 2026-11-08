package com.rupeek.maidbooking.strategy.pricing;
import java.math.BigDecimal;

import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.model.maid.Maid;
public interface PricingPolicy {
    BigDecimal price(Maid maid, TimeSlot slot);
}
