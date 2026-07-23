package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.CreateApiKeyRequest;
import com.tokentrack.aitokentracker.dto.CreateCompanyRequest;
import com.tokentrack.aitokentracker.entity.ApiKey;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.repository.ApiKeyRepository;
import com.tokentrack.aitokentracker.repository.CompanyRepository;
import com.tokentrack.aitokentracker.service.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/companies")
public class AdminController {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @PostMapping
    public Company createCompany(@RequestBody CreateCompanyRequest request) {
        Company company = new Company();
        company.setName(request.getName());
        return companyRepository.save(company);
    }

    @PostMapping("/{companyId}/api-keys")
    public Map<String, Object> createApiKey(
            @PathVariable UUID companyId,
            @RequestBody CreateApiKeyRequest request
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        String rawKey = "tk_" + UUID.randomUUID().toString().replace("-", "");

        ApiKey apiKey = new ApiKey();
        apiKey.setCompany(company);
        apiKey.setName(request.getName());
        apiKey.setKeyHash(HashUtil.sha256(rawKey));
        apiKeyRepository.save(apiKey);

        return Map.of(
                "id", apiKey.getId(),
                "name", apiKey.getName(),
                "apiKey", rawKey  // shown ONLY once, at creation
        );
    }

    @GetMapping
    public List<Company> listCompanies() {
        return companyRepository.findAll();
    }
}