package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {

    List<UsageLog> findByCompanyId(UUID companyId);

    @Query("SELECT COALESCE(SUM(u.costUsd), 0) FROM UsageLog u WHERE u.company.id = :companyId")
    java.math.BigDecimal sumCostByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(u) FROM UsageLog u WHERE u.company.id = :companyId")
    long countByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COALESCE(SUM(u.tokensIn), 0) FROM UsageLog u WHERE u.company.id = :companyId")
    long sumTokensInByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COALESCE(SUM(u.tokensOut), 0) FROM UsageLog u WHERE u.company.id = :companyId")
    long sumTokensOutByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT u.feature, COALESCE(SUM(u.costUsd), 0), COUNT(u) " +
            "FROM UsageLog u WHERE u.company.id = :companyId GROUP BY u.feature")
    List<Object[]> aggregateByFeature(@Param("companyId") UUID companyId);
}