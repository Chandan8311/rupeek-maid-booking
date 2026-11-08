package com.rupeek.maidbooking.service;

import java.util.List;

import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;

public interface MaidSearchService {
    List<Maid> search(MaidSearchCriteria criteria);
}
