package com.tokentrack.aitokentracker.repository;

import com.tokentrack.aitokentracker.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}