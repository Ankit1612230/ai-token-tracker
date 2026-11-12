package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.ChatRequest;
import com.tokentrack.aitokentracker.dto.ChatResponse;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.entity.Team;
import com.tokentrack.aitokentracker.entity.UsageLog;
import com.tokentrack.aitokentracker.exception.TeamNotFoundException;
import com.tokentrack.aitokentracker.repository.TeamRepository;
import com.tokentrack.aitokentracker.repository.UsageLogRepository;
import com.tokentrack.aitokentracker.service.ApiKeyAuthService;
import com.tokentrack.aitokentracker.service.BudgetService;
import com.tokentrack.aitokentracker.service.LlmProviderService;
import com.tokentrack.aitokentracker.service.PricingService;
import jakarta.validation.Valid;
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

    @Autowired
    private TeamRepository teamRepository;

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader(value = "X-Feature", required = false) String feature,
            @RequestHeader(value = "X-Team-Id", required = false) UUID teamId,
            @Valid @RequestBody ChatRequest request
    ) {
        Company company = apiKeyAuthService.resolveCompany(apiKey);
        pricingService.assertSupported(request.getModel());
        Team team = null;
        if (teamId != null) {
            team = teamRepository.findByIdAndCompanyId(teamId, company.getId())
                    .orElseThrow(() -> new TeamNotFoundException("Team not found for this company"));
        }
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
        log.setTeam(team);
        usageLogRepository.save(log);

        // Record spend AFTER successful call
        budgetService.recordSpend(company.getId(), teamId, cost);

        return new ChatResponse(
                result.content(), request.getModel(),
                result.tokensIn(), result.tokensOut(), cost, result.latencyMs()
        );
    }
}