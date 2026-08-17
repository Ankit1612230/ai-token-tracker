package com.tokentrack.aitokentracker.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void calculatesCostCorrectlyForKnownModel() {
        BigDecimal cost = pricingService.calculateCost("llama-3.1-8b-instant", 1000, 500);
        // input: 1000 tokens * $0.05/1M = 0.00005
        // output: 500 tokens * $0.08/1M = 0.00004
        // total = 0.00009
        assertEquals(new BigDecimal("0.000090"), cost);
    }

    @Test
    void fallsBackToDefaultRateForUnknownModel() {
        BigDecimal cost = pricingService.calculateCost("some-unknown-model", 1_000_000, 0);
        // fallback rate: $0.50/1M input
        assertEquals(new BigDecimal("0.500000"), cost);
    }

    @Test
    void returnsZeroCostForZeroTokens() {
        BigDecimal cost = pricingService.calculateCost("llama-3.1-8b-instant", 0, 0);
        assertEquals(new BigDecimal("0.000000"), cost);
    }
}