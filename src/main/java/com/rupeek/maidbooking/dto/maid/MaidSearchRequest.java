package com.rupeek.maidbooking.dto.maid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class MaidSearchRequest {
    private String locality;
    private String serviceType;
    private String skill;
    private Gender gender;
    @DecimalMin("0.0") @DecimalMax("5.0")
    private BigDecimal minRating;
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal maxHourlyRate;
    @NotNull
    private LocalDateTime startAt;
    @NotNull @Min(15) @Max(480)
    private Integer durationMinutes;
}
