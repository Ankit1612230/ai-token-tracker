package com.tokentrack.aitokentracker.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class PricingService {

    // Prices are $ per MILLION tokens (in, out) — sourced from Groq pricing page
    private static final Map<String, double[]> PRICING = Map.ofEntries(
            Map.entry("gpt-oss-20b", new double[]{0.075, 0.30}),
            Map.entry("gpt-oss-safeguard-20b", new double[]{0.075, 0.30}),
            Map.entry("gpt-oss-120b", new double[]{0.15, 0.60}),
            Map.entry("llama-4-scout-17b-16e-instruct", new double[]{0.11, 0.34}),
            Map.entry("qwen3-32b", new double[]{0.29, 0.59}),
            Map.entry("llama-3.3-70b-versatile", new double[]{0.59, 0.79}),
            Map.entry("llama-3.1-8b-instant", new double[]{0.05, 0.08}),
            Map.entry("qwen3.6-27b", new double[]{0.60, 3.00})
    );

    public BigDecimal calculateCost(String model, int tokensIn, int tokensOut) {
        double[] rates = PRICING.getOrDefault(model, new double[]{0.50, 0.50}); // fallback rate

        BigDecimal inCost = BigDecimal.valueOf(rates[0])
                .multiply(BigDecimal.valueOf(tokensIn))
                .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);

        BigDecimal outCost = BigDecimal.valueOf(rates[1])
                .multiply(BigDecimal.valueOf(tokensOut))
                .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);

        return inCost.add(outCost);
    }
}