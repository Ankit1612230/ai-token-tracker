package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.ChatRequest;
import com.tokentrack.aitokentracker.dto.ChatResponse;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.entity.UsageLog;
import com.tokentrack.aitokentracker.repository.UsageLogRepository;
import com.tokentrack.aitokentracker.service.ApiKeyAuthService;
import com.tokentrack.aitokentracker.service.LlmProviderService;
import com.tokentrack.aitokentracker.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader(value = "X-Feature", required = false) String feature,
            @RequestBody ChatRequest request
    ) {
        // 1. Authenticate
        Company company = apiKeyAuthService.resolveCompany(apiKey);

        // 2. Call the LLM provider
        LlmProviderService.LlmCallResult result = llmProviderService.callGroq(request);

        // 3. Calculate cost
        BigDecimal cost = pricingService.calculateCost(
                request.getModel(), result.tokensIn(), result.tokensOut()
        );

        // 4. Save usage log
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

        // 5. Return response to client
        return new ChatResponse(
                result.content(),
                request.getModel(),
                result.tokensIn(),
                result.tokensOut(),
                cost,
                result.latencyMs()
        );
    }
}