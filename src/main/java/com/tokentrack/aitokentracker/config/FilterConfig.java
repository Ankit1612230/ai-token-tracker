package com.tokentrack.aitokentracker.config;

import com.tokentrack.aitokentracker.filter.AdminAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public AdminAuthFilter adminAuthFilter() {
        return new AdminAuthFilter();
    }

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration(AdminAuthFilter adminAuthFilter) {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(adminAuthFilter);
        // Covers /v1/companies, /v1/companies/{id}/teams, /v1/companies/{id}/budgets,
        // /v1/companies/{id}/dashboard/summary, /v1/companies/{id}/api-keys - everything
        // except /v1/proxy/**, which stays public (protected by its own X-Api-Key check).
        registration.addUrlPatterns("/v1/companies", "/v1/companies/*");
        registration.setName("adminAuthFilter");
        return registration;
    }
}