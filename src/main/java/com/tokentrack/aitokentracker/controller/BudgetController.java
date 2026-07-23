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
import java.util.List;
import java.util.Optional;
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
    @GetMapping
    public List<Budget> getBudgets(@PathVariable UUID companyId) {
        return budgetRepository.findByCompanyId(companyId);
    }

    @PostMapping
    public Budget createBudget(
            @PathVariable UUID companyId,
            @RequestBody CreateBudgetRequest request
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if a budget already exists for this company/team combo
        Optional<Budget> existing;
        if (request.getTeamId() != null) {
            existing = budgetRepository.findByCompanyIdAndTeamId(companyId, request.getTeamId());
        } else {
            existing = budgetRepository.findByCompanyId(companyId).stream()
                    .filter(b -> b.getTeam() == null)
                    .findFirst();
        }

        if (existing.isPresent()) {
            Budget budget = existing.get();
            budget.setMonthlyLimitUsd(request.getMonthlyLimitUsd());
            return budgetRepository.save(budget); // update in place, don't create duplicate
        }

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