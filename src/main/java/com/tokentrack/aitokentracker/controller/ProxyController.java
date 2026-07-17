package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.ChatRequest;
import com.tokentrack.aitokentracker.dto.ChatResponse;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.entity.UsageLog;
import com.tokentrack.aitokentracker.repository.UsageLogRepository;
import com.tokentrack.aitokentracker.service.ApiKeyAuthService;
import com.tokentrack.aitokentracker.service.BudgetService;
import com.tokentrack.aitokentracker.service.LlmProviderService;
import com.tokentrack.aitokentracker.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/v1/proxy")
public class ProxyController {

    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    @Autowired
    private LlmProviderService llmProviderService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private UsageLogRepository usageLogRepository;

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader(value = "X-Feature", required = false) String feature,
            @RequestHeader(value = "X-Team-Id", required = false) UUID teamId,
            @RequestBody ChatRequest request
    ) {
        Company company = apiKeyAuthService.resolveCompany(apiKey);

        // Check budget BEFORE calling Groq
        budgetService.checkBudget(company.getId(), teamId);

        LlmProviderService.LlmCallResult result = llmProviderService.callGroq(request);

        BigDecimal cost = pricingService.calculateCost(
                request.getModel(), result.tokensIn(), result.tokensOut()
        );

        UsageLog log = new UsageLog();
        log.setCompany(company);
        log.setFeature(feature);
        log.setProvider("groq");
        log.setModel(request.getModel());
        log.setTokensIn(result.tokensIn());
        log.setTokensOut(result.tokensOut());
        log.setCostUsd(cost);
        log.setLatencyMs(result.latencyMs());
        usageLogRepository.save(log);

        // Record spend AFTER successful call
        budgetService.recordSpend(company.getId(), teamId, cost);

        return new ChatResponse(
                result.content(), request.getModel(),
                result.tokensIn(), result.tokensOut(), cost, result.latencyMs()
        );
    }
}