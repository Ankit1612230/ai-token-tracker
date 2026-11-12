package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.entity.Budget;
import com.tokentrack.aitokentracker.exception.BudgetExceededException;
import com.tokentrack.aitokentracker.repository.BudgetRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import com.tokentrack.aitokentracker.exception.ConcurrentBudgetUpdateException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
        if (isStale(budget)) {
            return; // new month: previous spend no longer counts
        }
        if (budget.getCurrentSpendUsd().compareTo(budget.getMonthlyLimitUsd()) >= 0) {
            throw new BudgetExceededException(
                    "Budget exceeded: $" + budget.getCurrentSpendUsd() + " / $" + budget.getMonthlyLimitUsd()
            );
        }
    }

    @Transactional
    public void recordSpend(UUID companyId, UUID teamId, BigDecimal cost) {
        findBudget(companyId, teamId).ifPresent(budget ->
                budgetRepository.addSpend(budget.getId(), cost, currentPeriod()));
    }

    private Optional<Budget> findBudget(UUID companyId, UUID teamId) {
        if (teamId != null) {
            return budgetRepository.findByCompanyIdAndTeamId(companyId, teamId);
        }
        return budgetRepository.findByCompanyId(companyId).stream()
                .filter(b -> b.getTeam() == null)
                .findFirst();
    }
    private LocalDate currentPeriod() {
        return LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);
    }

    private boolean isStale(Budget budget) {
        return budget.getPeriodStart() == null
                || budget.getPeriodStart().isBefore(currentPeriod());
    }
}