package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.CreateApiKeyRequest;
import com.tokentrack.aitokentracker.dto.CreateCompanyRequest;
import com.tokentrack.aitokentracker.entity.ApiKey;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.repository.ApiKeyRepository;
import com.tokentrack.aitokentracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ApiKey createApiKey(
            @PathVariable UUID companyId,
            @RequestBody CreateApiKeyRequest request
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        ApiKey apiKey = new ApiKey();
        apiKey.setCompany(company);
        apiKey.setName(request.getName());
        apiKey.setKeyValue("tk_" + UUID.randomUUID().toString().replace("-", ""));

        return apiKeyRepository.save(apiKey);
    }

    @GetMapping
    public List<Company> listCompanies() {
        return companyRepository.findAll();
    }
}