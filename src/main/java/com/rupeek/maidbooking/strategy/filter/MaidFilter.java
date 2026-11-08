package com.rupeek.maidbooking.strategy.filter;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;
public interface MaidFilter {
    boolean matches(Maid maid, MaidSearchCriteria criteria);
}
