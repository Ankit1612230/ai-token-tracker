package com.tokentrack.aitokentracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "monthly_limit_usd", precision = 12, scale = 6, nullable = false)
    private BigDecimal monthlyLimitUsd;

    @Column(name = "current_spend_usd", precision = 12, scale = 6)
    private BigDecimal currentSpendUsd = BigDecimal.ZERO;
}