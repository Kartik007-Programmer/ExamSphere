package com.examsphere.auth.Controller;

import com.examsphere.auth.DTOs.AuthResponse;
import com.examsphere.auth.DTOs.LoginRequest;
import com.examsphere.auth.DTOs.RegisterRequest;
import com.examsphere.auth.Service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(
            @AuthenticationPrincipal Jwt jwt) {

        return new CurrentUserResponse(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role")
        );
    }

    public record CurrentUserResponse(
            Long userId,
            String email,
            String role
    ) {}
}