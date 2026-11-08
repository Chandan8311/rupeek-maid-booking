package com.rupeek.maidbooking.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;
import com.rupeek.maidbooking.repository.MaidRepository;
import com.rupeek.maidbooking.service.AvailabilityService;
import com.rupeek.maidbooking.service.MaidSearchService;
import com.rupeek.maidbooking.strategy.filter.MaidFilter;


@Service
public final class MaidSearchServiceImpl implements MaidSearchService {
    private final MaidRepository maidRepository;
    private final AvailabilityService availabilityService;
    private final List<MaidFilter> filters;
    private final Clock clock;

    @Autowired
    public MaidSearchServiceImpl(MaidRepository maidRepository,
                                 AvailabilityService availabilityService,
                                 List<MaidFilter> filters, Clock clock) {
        this.maidRepository = maidRepository;
        this.availabilityService = availabilityService;
        this.filters = List.copyOf(filters);
        this.clock = clock;
    }

    @Override
    public List<Maid> search(MaidSearchCriteria criteria) {
        if (criteria == null || criteria.getSlot() == null
                || criteria.getSlot().getStart().isBefore(LocalDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Search requires a current or future time slot");
        }
        return maidRepository.findAll().stream()
                .filter(maid -> filters.stream().allMatch(filter -> filter.matches(maid, criteria)))
                .filter(maid -> maidRepository.withMaidLock(maid.getId(),
                        () -> availabilityService.isAvailable(maid, criteria.getSlot())))
                .sorted(Comparator.comparing(Maid::getRating).reversed()
                        .thenComparing(Maid::getName))
                .toList();
    }
}
