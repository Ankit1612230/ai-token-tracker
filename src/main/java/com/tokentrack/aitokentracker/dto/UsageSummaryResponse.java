package com.tokentrack.aitokentracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UsageSummaryResponse {
    private BigDecimal totalCostUsd;
    private long totalCalls;
    private long totalTokensIn;
    private long totalTokensOut;
    private List<FeatureBreakdown> byFeature;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FeatureBreakdown {
        private String feature;
        private BigDecimal costUsd;
        private long calls;
    }
}