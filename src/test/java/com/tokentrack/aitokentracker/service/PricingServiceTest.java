package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.exception.UnsupportedModelException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void calculatesCostCorrectlyForKnownModel() {
        BigDecimal cost = pricingService.calculateCost("openai/gpt-oss-20b", 1000, 500);
        // input: 1000 * $0.075/1M = 0.000075
        // output: 500 * $0.30/1M = 0.000150
        assertEquals(new BigDecimal("0.000225"), cost);
    }

    @Test
    void calculatesCostForLargerModel() {
        BigDecimal cost = pricingService.calculateCost("openai/gpt-oss-120b", 1_000_000, 0);
        assertEquals(new BigDecimal("0.150000"), cost);
    }

    @Test
    void throwsForUnknownModel() {
        assertThrows(UnsupportedModelException.class,
                () -> pricingService.calculateCost("some-unknown-model", 1000, 1000));
    }

    @Test
    void returnsZeroCostForZeroTokens() {
        BigDecimal cost = pricingService.calculateCost("openai/gpt-oss-20b", 0, 0);
        assertEquals(new BigDecimal("0.000000"), cost);
    }
}