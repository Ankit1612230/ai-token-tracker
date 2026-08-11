package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.entity.Budget;
import com.tokentrack.aitokentracker.exception.BudgetExceededException;
import com.tokentrack.aitokentracker.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public void checkBudget(UUID companyId, UUID teamId) {
        Optional<Budget> budgetOpt = findBudget(companyId, teamId);

        if (budgetOpt.isEmpty()) {
            return;
        }

        Budget budget = budgetOpt.get();
        if (budget.getCurrentSpendUsd().compareTo(budget.getMonthlyLimitUsd()) >= 0) {
            throw new BudgetExceededException(
                    "Budget exceeded: $" + budget.getCurrentSpendUsd() + " / $" + budget.getMonthlyLimitUsd()
            );
        }
    }

    public void recordSpend(UUID companyId, UUID teamId, BigDecimal cost) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Optional<Budget> budgetOpt = findBudget(companyId, teamId);
                if (budgetOpt.isEmpty()) {
                    return;
                }
                Budget budget = budgetOpt.get();
                budget.setCurrentSpendUsd(budget.getCurrentSpendUsd().add(cost));
                budgetRepository.save(budget);
                return; // success, exit
            } catch (OptimisticLockingFailureException e) {
                // another request updated the budget concurrently - retry with fresh data
                if (attempt == maxRetries - 1) {
                    throw e; // give up after max retries
                }
            }
        }
    }

    private Optional<Budget> findBudget(UUID companyId, UUID teamId) {
        if (teamId != null) {
            return budgetRepository.findByCompanyIdAndTeamId(companyId, teamId);
        }
        return budgetRepository.findByCompanyId(companyId).stream()
                .filter(b -> b.getTeam() == null)
                .findFirst();
    }
}