package com.rupeek.maidbooking.strategy.filter;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;

@Component
public final class LocalityMaidFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        return criteria.getLocality() == null || criteria.getLocality().isBlank() || maid.getLocality().equalsIgnoreCase(criteria.getLocality().trim());
    }
}
