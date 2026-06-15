package com.codex.travel.ticket.config;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
            Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
            Set<String> roleCodes = new LinkedHashSet<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                        .forEach(role -> {
                            roleCodes.add(role);
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        });
            }
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.stream()
                        .map(String::trim)
                        .filter(permission -> !permission.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            grantTravelPermissions(roleCodes, authorities);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    private void grantTravelPermissions(
            Set<String> roleCodes,
            Set<SimpleGrantedAuthority> authorities) {
        if (roleCodes.stream().anyMatch(role -> Set.of("ADMIN", "MANAGER", "TRAVEL_ADMIN").contains(role))) {
            addAuthorities(authorities,
                    "travel:ticket:read",
                    "travel:ticket:create",
                    "travel:ticket:update",
                    "travel:ticket:delete",
                    "travel:ticket:approve",
                    "travel:risk:read",
                    "travel:search:reindex",
                    "travel:ops:read");
        }
        if (roleCodes.contains("TRAVEL_USER")) {
            addAuthorities(authorities,
                    "travel:ticket:read",
                    "travel:ticket:create",
                    "travel:ticket:update",
                    "travel:risk:read");
        }
        if (roleCodes.contains("TRAVEL_APPROVER")) {
            addAuthorities(authorities,
                    "travel:ticket:read",
                    "travel:ticket:update",
                    "travel:ticket:approve",
                    "travel:risk:read");
        }
        if (roleCodes.contains("TRAVEL_AUDITOR")) {
            addAuthorities(authorities,
                    "travel:ticket:read",
                    "travel:risk:read",
                    "travel:ops:read");
        }
    }

    private void addAuthorities(Set<SimpleGrantedAuthority> authorities, String... values) {
        for (String value : values) {
            authorities.add(new SimpleGrantedAuthority(value));
        }
    }
}
