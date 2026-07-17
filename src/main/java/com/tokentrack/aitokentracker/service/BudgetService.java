package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.entity.Budget;
import com.tokentrack.aitokentracker.exception.BudgetExceededException;
import com.tokentrack.aitokentracker.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    /**
     * Checks if the company (or team) has room in their budget.
     * Throws if the budget is already exceeded.
     */
    public void checkBudget(UUID companyId, UUID teamId) {
        Optional<Budget> budgetOpt;

        if (teamId != null) {
            budgetOpt = budgetRepository.findByCompanyIdAndTeamId(companyId, teamId);
        } else {
            // company-wide budget - look for the one with no team set
            budgetOpt = budgetRepository.findByCompanyId(companyId).stream()
                    .filter(b -> b.getTeam() == null)
                    .findFirst();
        }

        if (budgetOpt.isEmpty()) {
            return; // no budget set = no limit, allow the call
        }

        Budget budget = budgetOpt.get();
        if (budget.getCurrentSpendUsd().compareTo(budget.getMonthlyLimitUsd()) >= 0) {
            throw new BudgetExceededException(
                    "Budget exceeded: $" + budget.getCurrentSpendUsd() + " / $" + budget.getMonthlyLimitUsd()
            );
        }
    }

    /**
     * Adds the cost of a completed call to the relevant budget's running total.
     */
    public void recordSpend(UUID companyId, UUID teamId, BigDecimal cost) {
        Optional<Budget> budgetOpt;

        if (teamId != null) {
            budgetOpt = budgetRepository.findByCompanyIdAndTeamId(companyId, teamId);
        } else {
            budgetOpt = budgetRepository.findByCompanyId(companyId).stream()
                    .filter(b -> b.getTeam() == null)
                    .findFirst();
        }

        budgetOpt.ifPresent(budget -> {
            budget.setCurrentSpendUsd(budget.getCurrentSpendUsd().add(cost));
            budgetRepository.save(budget);
        });
    }
}