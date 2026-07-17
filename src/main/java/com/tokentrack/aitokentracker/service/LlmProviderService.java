package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import com.tokentrack.aitokentracker.exception.LlmProviderException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmProviderService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    private final RestClient restClient = RestClient.create();

    public LlmCallResult callGroq(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("messages", request.getMessages());

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(groqApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new LlmProviderException("Groq rate limit reached. Please try again shortly.");
        } catch (HttpClientErrorException e) {
            throw new LlmProviderException("Groq API error: " + e.getStatusCode());
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        String content = (String) message.get("content");

        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        Integer tokensIn = (Integer) usage.get("prompt_tokens");
        Integer tokensOut = (Integer) usage.get("completion_tokens");

        return new LlmCallResult(content, tokensIn, tokensOut, (int) latencyMs);
    }

    public record LlmCallResult(String content, Integer tokensIn, Integer tokensOut, Integer latencyMs) {
    }
}