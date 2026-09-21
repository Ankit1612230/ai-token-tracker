package com.tokentrack.aitokentracker.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protects the admin/management endpoints (companies, teams, budgets, dashboard)
 * with a single shared secret, since these are not meant to be called by regular
 * clients - only whoever operates the platform.
 *
 * NOT annotated with @Component on purpose: registering it that way would make
 * Spring Boot apply it to every URL by default. It's registered explicitly, scoped
 * to /v1/companies/** only, in FilterConfig - so it never runs on the public-facing
 * /v1/proxy/** endpoint, which already has its own per-company API key check in
 * ApiKeyAuthService.
 */
public class AdminAuthFilter extends OncePerRequestFilter {

    @Value("${admin.api.key}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String providedKey = request.getHeader("X-Admin-Key");

        if (providedKey == null || !providedKey.equals(adminApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid X-Admin-Key header\"}");
            return; // stop here - do NOT call filterChain.doFilter(), request never reaches the controller
        }

        filterChain.doFilter(request, response);
    }
}