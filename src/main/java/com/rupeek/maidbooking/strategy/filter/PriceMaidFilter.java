package com.rupeek.maidbooking.strategy.filter;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;

@Component
public final class PriceMaidFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        return criteria.getMaxHourlyRate() == null || maid.getHourlyRate().compareTo(criteria.getMaxHourlyRate()) <= 0;
    }
}
