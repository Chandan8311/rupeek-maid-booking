package com.rupeek.maidbooking.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.rupeek.maidbooking.constants.ApiPaths;
import com.rupeek.maidbooking.dto.maid.MaidOnboardRequest;
import com.rupeek.maidbooking.dto.maid.MaidResponse;
import com.rupeek.maidbooking.dto.maid.MaidSearchRequest;
import com.rupeek.maidbooking.mapper.MaidMapper;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.MaidSearchCriteria;
import com.rupeek.maidbooking.model.booking.TimeSlot;
import com.rupeek.maidbooking.service.MaidSearchService;
import com.rupeek.maidbooking.service.MaidService;

@RestController
@RequestMapping(ApiPaths.MAIDS)
@Validated
@RequiredArgsConstructor
public class MaidController {
    private final MaidService maidService;
    private final MaidSearchService maidSearchService;

    @PostMapping
    public ResponseEntity<MaidResponse> onboard(@Valid @RequestBody MaidOnboardRequest request) {
        var maid = maidService.onboard(request.getName(), request.getLocality(), request.getGender(),
                request.getRating(), request.getHourlyRate(), request.getSkills(), MaidMapper.toAvailability(request));
        return ResponseEntity.created(URI.create(ApiPaths.MAIDS + "/" + maid.getId()))
                .body(MaidMapper.toResponse(maid));
    }

    @GetMapping("/{maidId}")
    public MaidResponse get(@PathVariable UUID maidId) {
        return MaidMapper.toResponse(maidService.get(maidId));
    }


    @GetMapping()
    public List<MaidResponse> search(@RequestParam(required = false) String locality,
                                      @RequestParam(required = false) String serviceType,
                                      @RequestParam(required = false) String skill,
                                      @RequestParam(required = false) Gender gender,
                                      @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal minRating,
                                      @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = false) BigDecimal maxHourlyRate,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
                                      @RequestParam @Min(15) @Max(480) int durationMinutes) {
        TimeSlot slot = TimeSlot.builder()
                .start(startAt)
                .end(startAt.plusMinutes(durationMinutes))
                .build();

        MaidSearchCriteria criteria = MaidSearchCriteria.builder()
                .locality(locality)
                .serviceType(serviceType)
                .skill(skill)
                .gender(gender)
                .minRating(minRating)
                .maxHourlyRate(maxHourlyRate)
                .slot(slot)
                .build();
        return maidSearchService.search(criteria)
                .stream()
                .map(MaidMapper::toResponse)
                .toList();
    }
}
