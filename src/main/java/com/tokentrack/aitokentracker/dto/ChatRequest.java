package com.tokentrack.aitokentracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {
    @NotBlank(message = "provider is required")
    private String provider;   // e.g., "groq"

    @NotBlank(message = "model is required")
    private String model;

    @NotEmpty(message = "messages cannot be empty")
    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {
        @NotBlank(message = "role is required")
        private String role;    // "user", "system", "assistant"

        @NotBlank(message = "content is required")
        private String content;
    }
}