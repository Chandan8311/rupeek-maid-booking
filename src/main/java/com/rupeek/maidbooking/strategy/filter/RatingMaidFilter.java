package com.rupeek.maidbooking.strategy.filter;

import org.springframework.stereotype.Component;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;

@Component
public final class RatingMaidFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        return criteria.getMinRating() == null || maid.getRating().compareTo(criteria.getMinRating()) >= 0;
    }
}
