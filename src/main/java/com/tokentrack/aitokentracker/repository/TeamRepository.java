package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByCompanyId(UUID companyId);
}