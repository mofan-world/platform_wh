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
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN", "TRAVEL_USER", "TRAVEL_APPROVER", "TRAVEL_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**", "/api/v1/risk/**", "/api/v1/search/tickets")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN", "TRAVEL_USER", "TRAVEL_APPROVER", "TRAVEL_AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN", "TRAVEL_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN", "TRAVEL_USER", "TRAVEL_APPROVER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tickets/**")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/approvals/**")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN", "TRAVEL_APPROVER")
                        .requestMatchers("/api/v1/search/tickets/reindex", "/api/v1/ops/**")
                        .hasAnyRole("ADMIN", "TRAVEL_ADMIN")
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
