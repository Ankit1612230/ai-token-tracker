package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.CreateBudgetRequest;
import com.tokentrack.aitokentracker.entity.Budget;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.entity.Team;
import com.tokentrack.aitokentracker.repository.BudgetRepository;
import com.tokentrack.aitokentracker.repository.CompanyRepository;
import com.tokentrack.aitokentracker.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/v1/companies/{companyId}/budgets")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PostMapping
    public Budget createBudget(
            @PathVariable UUID companyId,
            @RequestBody CreateBudgetRequest request
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Budget budget = new Budget();
        budget.setCompany(company);
        budget.setMonthlyLimitUsd(request.getMonthlyLimitUsd());
        budget.setCurrentSpendUsd(BigDecimal.ZERO);

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            budget.setTeam(team);
        }

        return budgetRepository.save(budget);
    }
}