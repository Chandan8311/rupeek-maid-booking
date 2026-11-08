package com.rupeek.maidbooking.strategy.refund;
import java.math.BigDecimal;
import java.time.Clock;

import com.rupeek.maidbooking.model.booking.BookingOccurrence;
public interface RefundPolicy {
    BigDecimal refundableAmount(BookingOccurrence occurrence, Clock clock);
}
