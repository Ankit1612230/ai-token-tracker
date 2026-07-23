package com.tokentrack.aitokentracker.service;

import com.tokentrack.aitokentracker.entity.ApiKey;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.exception.InvalidApiKeyException;
import com.tokentrack.aitokentracker.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyAuthService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public Company resolveCompany(String rawKey) {
        String hashedKey = HashUtil.sha256(rawKey);
        ApiKey apiKey = apiKeyRepository.findByKeyHash(hashedKey)
                .orElseThrow(() -> new InvalidApiKeyException("Invalid API key"));
        return apiKey.getCompany();
    }
}