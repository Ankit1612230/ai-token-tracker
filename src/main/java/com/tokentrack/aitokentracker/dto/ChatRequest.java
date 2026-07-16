package com.tokentrack.aitokentracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {
    private String provider;   // e.g., "groq"
    private String model;      // e.g., "llama-3.1-70b-versatile"
    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {
        private String role;    // "user", "system", "assistant"
        private String content;
    }
}