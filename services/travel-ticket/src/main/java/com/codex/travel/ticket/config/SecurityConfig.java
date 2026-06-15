package com.codex.travel.ticket.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] TRAVEL_READ_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "ROLE_TRAVEL_USER",
            "ROLE_TRAVEL_APPROVER",
            "ROLE_TRAVEL_AUDITOR",
            "travel:ticket:read",
            "travel:risk:read",
            "travel:ops:read"
    };

    private static final String[] TRAVEL_CREATE_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "ROLE_TRAVEL_USER",
            "travel:ticket:create"
    };

    private static final String[] TRAVEL_UPDATE_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "ROLE_TRAVEL_USER",
            "ROLE_TRAVEL_APPROVER",
            "travel:ticket:update"
    };

    private static final String[] TRAVEL_DELETE_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "travel:ticket:delete"
    };

    private static final String[] TRAVEL_APPROVE_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "ROLE_TRAVEL_APPROVER",
            "travel:ticket:approve"
    };

    private static final String[] TRAVEL_ADMIN_AUTHORITIES = {
            "ROLE_ADMIN",
            "ROLE_TRAVEL_ADMIN",
            "travel:search:reindex",
            "travel:ops:read"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reports/**")
                        .hasAnyAuthority(TRAVEL_READ_AUTHORITIES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**", "/api/v1/risk/**", "/api/v1/search/tickets")
                        .hasAnyAuthority(TRAVEL_READ_AUTHORITIES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets")
                        .hasAnyAuthority(TRAVEL_CREATE_AUTHORITIES)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**")
                        .hasAnyAuthority(TRAVEL_UPDATE_AUTHORITIES)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tickets/**")
                        .hasAnyAuthority(TRAVEL_DELETE_AUTHORITIES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/approvals/**")
                        .hasAnyAuthority(TRAVEL_APPROVE_AUTHORITIES)
                        .requestMatchers("/api/v1/search/tickets/reindex", "/api/v1/ops/**")
                        .hasAnyAuthority(TRAVEL_ADMIN_AUTHORITIES)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter())))
                .addFilterAfter(new TenantHeaderFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${platform.security.jwt.secret}") String secret,
            @Value("${platform.security.jwt.issuer}") String issuer) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("platform.security.jwt.secret must contain at least 32 bytes");
        }
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtConverter() {
        return jwt -> {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }
}
