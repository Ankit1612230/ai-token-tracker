package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    List<UsageLog> findByCompanyId(UUID companyId);
}