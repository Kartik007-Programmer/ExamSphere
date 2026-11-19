package com.examsphere.attempt.Config;

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
                        .requestMatchers(HttpMethod.GET, "/api/attempts/PING").permitAll()
                        // Students perform attempts
                        .requestMatchers(HttpMethod.POST, "/api/attempts/exams/*/start")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/attempts/*/questions/*/answer")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/attempts/*/submit")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/attempts/*/result")
                        .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                        // Anything else is teacher/admin only
                        .requestMatchers("/api/attempts/**")
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