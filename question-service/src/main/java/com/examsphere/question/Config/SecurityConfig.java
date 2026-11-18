package com.examsphere.question.Config;

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
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/questions/internal/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/questions/student/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/questions","/api/questions/multi")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/questions/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/questions/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        .anyRequest()
                        .denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
