package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.dto.UsageSummaryResponse;
import com.tokentrack.aitokentracker.repository.UsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UsageLogRepository usageLogRepository;

    public UsageSummaryResponse getSummary(UUID companyId) {
        BigDecimal totalCost = usageLogRepository.sumCostByCompany(companyId);
        long totalCalls = usageLogRepository.countByCompany(companyId);
        long tokensIn = usageLogRepository.sumTokensInByCompany(companyId);
        long tokensOut = usageLogRepository.sumTokensOutByCompany(companyId);

        List<Object[]> rawFeatureData = usageLogRepository.aggregateByFeature(companyId);
        List<UsageSummaryResponse.FeatureBreakdown> byFeature = rawFeatureData.stream()
                .map(row -> new UsageSummaryResponse.FeatureBreakdown(
                        row[0] != null ? (String) row[0] : "untagged",
                        (BigDecimal) row[1],
                        (Long) row[2]
                ))
                .collect(Collectors.toList());

        return new UsageSummaryResponse(totalCost, totalCalls, tokensIn, tokensOut, byFeature);
    }
}