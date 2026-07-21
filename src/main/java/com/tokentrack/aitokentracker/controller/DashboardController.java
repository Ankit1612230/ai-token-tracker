package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.UsageSummaryResponse;
import com.tokentrack.aitokentracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/companies/{companyId}/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public UsageSummaryResponse getSummary(@PathVariable UUID companyId) {
        return dashboardService.getSummary(companyId);
    }
}