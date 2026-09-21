package com.tokentrack.aitokentracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApiKeyRequest {
    @NotBlank(message = "name is required")
    private String name; // e.g. "prod-key", "test-key"
}