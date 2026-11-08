package com.rupeek.maidbooking.dto.maid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.rupeek.maidbooking.model.maid.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaidOnboardRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String locality;
    @NotNull
    private Gender gender;
    @NotNull @DecimalMin("0.0") @DecimalMax("5.0")
    private BigDecimal rating;
    @NotNull @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal hourlyRate;
    @NotEmpty
    private Set<@NotBlank String> skills;
    @NotEmpty
    private List<@NotNull @Valid MaidAvailabilityRequest> availability;
}
