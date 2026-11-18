package com.examsphere.exam.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Students must be able to read the attempt-info for an exam they will take
                        .requestMatchers(HttpMethod.GET, "/api/exams/*/attempt-info")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Students must be able to browse published exams
                        .requestMatchers(HttpMethod.GET, "/api/exams/published")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Teacher/admin management endpoints
                        .requestMatchers(HttpMethod.GET, "/api/exams/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .requestMatchers("/api/exams/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter))
                );

        return http.build();
    }
}