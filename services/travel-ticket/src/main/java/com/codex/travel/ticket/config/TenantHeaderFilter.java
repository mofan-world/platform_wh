package com.codex.travel.ticket.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantHeaderFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        Object uid = jwtAuthentication.getToken().getClaim("uid");
        if (uid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT uid claim is required");
            return;
        }

        filterChain.doFilter(new TenantRequest(request, String.valueOf(uid)), response);
    }

    private static final class TenantRequest extends HttpServletRequestWrapper {

        private final String tenantId;

        private TenantRequest(HttpServletRequest request, String tenantId) {
            super(request);
            this.tenantId = tenantId;
        }

        @Override
        public String getHeader(String name) {
            return TENANT_HEADER.equalsIgnoreCase(name) ? tenantId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return TENANT_HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(tenantId))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new ArrayList<>();
            Enumeration<String> source = super.getHeaderNames();
            if (source != null) {
                source.asIterator().forEachRemaining(names::add);
            }
            if (names.stream().noneMatch(TENANT_HEADER::equalsIgnoreCase)) {
                names.add(TENANT_HEADER);
            }
            return Collections.enumeration(names);
        }
    }
}
