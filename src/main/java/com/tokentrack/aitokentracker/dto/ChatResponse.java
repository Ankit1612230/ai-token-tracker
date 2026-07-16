package com.tokentrack.aitokentracker.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String content;       // the actual LLM reply text
    private String model;
    private Integer tokensIn;
    private Integer tokensOut;
    private BigDecimal costUsd;
    private Integer latencyMs;
}