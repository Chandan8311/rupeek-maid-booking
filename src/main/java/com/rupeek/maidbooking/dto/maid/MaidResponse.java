package com.rupeek.maidbooking.dto.maid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.maid.Gender;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaidResponse {
    private UUID id;
    private String name;
    private String locality;
    private Gender gender;
    private BigDecimal rating;
    private BigDecimal hourlyRate;
    private Set<String> skills;
    private List<MaidAvailabilityResponse> availability;
}
