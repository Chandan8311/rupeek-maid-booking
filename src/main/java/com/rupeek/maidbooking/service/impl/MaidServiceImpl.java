package com.rupeek.maidbooking.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.exception.BusinessException;
import com.rupeek.maidbooking.model.maid.AvailabilityWindow;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.repository.MaidRepository;
import com.rupeek.maidbooking.service.MaidService;

@Service
@RequiredArgsConstructor
public final class MaidServiceImpl implements MaidService {
    private final MaidRepository repository;

    @Override
    public Maid onboard(String name, String locality, Gender gender, BigDecimal rating,
                        BigDecimal hourlyRate, Set<String> skills, List<AvailabilityWindow> windows) {
        Maid maid = Maid.builder()
                .id(UUID.randomUUID())
                .name(name)
                .locality(locality)
                .gender(gender)
                .rating(rating)
                .hourlyRate(hourlyRate)
                .skills(skills)
                .availability(windows)
                .build();
        return repository.save(maid);
    }

    @Override
    public Maid get(UUID maidId) {
        return repository.findById(maidId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Maid not found"));
    }
}
