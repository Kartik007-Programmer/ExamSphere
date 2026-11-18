package com.examsphere.auth.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import com.examsphere.auth.DTOs.AuthResponse;
import com.examsphere.auth.DTOs.LoginRequest;
import com.examsphere.auth.DTOs.RegisterRequest;
import com.examsphere.auth.Entity.Role;
import com.examsphere.auth.Entity.User;
import com.examsphere.auth.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationSeconds;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists");
        }

        User user = new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                Role.STUDENT
        );

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        Instant now = Instant.now(Clock.offset(Clock.systemUTC(), java.time.Duration.ofMinutes(330)));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                        JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                token,
                "Bearer",
                expirationSeconds
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}