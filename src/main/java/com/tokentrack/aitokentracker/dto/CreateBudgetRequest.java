package com.tokentrack.aitokentracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateBudgetRequest {
    private UUID teamId;              // optional - null means company-wide
    private BigDecimal monthlyLimitUsd;
}