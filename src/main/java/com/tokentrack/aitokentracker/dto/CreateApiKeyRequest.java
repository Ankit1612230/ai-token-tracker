package com.tokentrack.aitokentracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApiKeyRequest {
    private String name; // e.g. "prod-key", "test-key"
}