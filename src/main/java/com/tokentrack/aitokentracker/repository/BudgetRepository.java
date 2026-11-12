package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByCompanyId(UUID companyId);
    Optional<Budget> findByCompanyIdAndTeamId(UUID companyId, UUID teamId);
    @Modifying
    @Query("""
        UPDATE Budget b SET
            b.currentSpendUsd = CASE
                WHEN b.periodStart IS NULL OR b.periodStart < :period THEN :cost
                ELSE b.currentSpendUsd + :cost END,
            b.periodStart = :period,
            b.version = b.version + 1
        WHERE b.id = :id
        """)
    int addSpend(@Param("id") UUID id,
                 @Param("cost") BigDecimal cost,
                 @Param("period") LocalDate period);
}