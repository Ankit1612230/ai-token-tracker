package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.dto.ChatRequest;
import com.tokentrack.aitokentracker.exception.LlmProviderException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmProviderService {

    private final ChatClient chatClient;

    public LlmProviderService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public LlmCallResult callGroq(ChatRequest request) {
        long start = System.currentTimeMillis();

        List<Message> messages = request.getMessages().stream()
                .map(this::toSpringAiMessage)
                .toList();

        Prompt prompt = new Prompt(messages,
                OpenAiChatOptions.builder().model(request.getModel()).build());

        ChatResponse response;
        try {
            response = chatClient.prompt(prompt).call().chatResponse();
        } catch (Exception e) {
            throw new LlmProviderException("LLM provider error: " + e.getMessage());
        }

        int latencyMs = (int) (System.currentTimeMillis() - start);
        var usage = response.getMetadata().getUsage();

        return new LlmCallResult(
                response.getResult().getOutput().getText(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                latencyMs);
    }

    private Message toSpringAiMessage(ChatRequest.Message m) {
        return switch (m.getRole().toLowerCase()) {
            case "system" -> new SystemMessage(m.getContent());
            case "assistant" -> new AssistantMessage(m.getContent());
            default -> new UserMessage(m.getContent());
        };
    }

    public record LlmCallResult(String content, Integer tokensIn, Integer tokensOut, Integer latencyMs) {
    }
}