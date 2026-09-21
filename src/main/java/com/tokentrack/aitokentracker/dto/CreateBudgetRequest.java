package com.tokentrack.aitokentracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateBudgetRequest {
    private UUID teamId;  // optional - null means company-wide
    @NotNull(message = "monthlyLimitUsd is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "monthlyLimitUsd must be greater than 0")
    private BigDecimal monthlyLimitUsd;
}