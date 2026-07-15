package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByCompanyId(UUID companyId);
    Optional<Budget> findByCompanyIdAndTeamId(UUID companyId, UUID teamId);
}