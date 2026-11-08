package com.rupeek.maidbooking.strategy.filter;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;

@Component
public final class GenderMaidFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        return criteria.getGender() == null || maid.getGender() == criteria.getGender();
    }
}
